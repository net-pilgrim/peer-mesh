-- ====================================================================
-- PeerMesh: PostgreSQL Native Graph Queries & SQL:2023 PGQ
-- ====================================================================

-- --------------------------------------------------------------------
-- 1. PostgreSQL 18+ / SQL:2023 Property Graph Definition (PGQ)
-- Standardized Property Graph syntax supported natively in Postgres 18/19
-- --------------------------------------------------------------------
DO $$
BEGIN
    BEGIN
        EXECUTE $pgq$
        CREATE PROPERTY GRAPH IF NOT EXISTS professional_graph
            VERTEX TABLES (
                profiles
                    KEY (id)
                    LABEL Person
                    PROPERTIES (handle, display_name, headline),
                companies
                    KEY (id)
                    LABEL Company
                    PROPERTIES (handle, name, industry)
            )
            EDGE TABLES (
                graph_edges AS connects_edge
                    KEY (id)
                    SOURCE KEY (source_id) REFERENCES profiles(id)
                    DESTINATION KEY (target_id) REFERENCES profiles(id)
                    LABEL Connected
                    PROPERTIES (status, weight, created_at)
                    WHERE edge_type = 'CONNECTED_TO' AND status = 'ACCEPTED',
                graph_edges AS follows_edge
                    KEY (id)
                    SOURCE KEY (source_id) REFERENCES profiles(id)
                    DESTINATION KEY (target_id) REFERENCES profiles(id)
                    LABEL Follows
                    PROPERTIES (created_at)
                    WHERE edge_type = 'FOLLOWS',
                graph_edges AS works_at_edge
                    KEY (id)
                    SOURCE KEY (source_id) REFERENCES profiles(id)
                    DESTINATION KEY (target_id) REFERENCES companies(id)
                    LABEL WorksAt
                    PROPERTIES (created_at)
                    WHERE edge_type = 'WORKS_AT'
            );
        $pgq$;
        RAISE NOTICE 'PostgreSQL 18+ Property Graph "professional_graph" created successfully.';
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE 'PGQ Property Graph creation notice: %', SQLERRM;
    END;
END $$;

-- --------------------------------------------------------------------
-- 2. 1st-Degree Connections (Direct Mutuals)
-- --------------------------------------------------------------------
CREATE OR REPLACE FUNCTION get_first_degree_connections(p_user_id UUID)
RETURNS TABLE (
    connection_id UUID,
    handle VARCHAR,
    display_name VARCHAR,
    headline VARCHAR,
    avatar_url TEXT,
    matrix_handle VARCHAR,
    nostr_npub VARCHAR,
    simplex_address TEXT,
    session_id VARCHAR,
    connected_since TIMESTAMPTZ
) AS $$
BEGIN
    RETURN QUERY
    WITH direct_edges AS (
        -- Bidirectional connection check
        SELECT target_id AS peer_id, created_at
        FROM graph_edges
        WHERE source_id = p_user_id AND edge_type = 'CONNECTED_TO' AND status = 'ACCEPTED'
        UNION
        SELECT source_id AS peer_id, created_at
        FROM graph_edges
        WHERE target_id = p_user_id AND edge_type = 'CONNECTED_TO' AND status = 'ACCEPTED'
    )
    SELECT 
        p.id AS connection_id,
        p.handle,
        p.display_name,
        p.headline,
        p.avatar_url,
        p.matrix_handle,
        p.nostr_npub,
        p.simplex_address,
        p.session_id,
        de.created_at AS connected_since
    FROM direct_edges de
    JOIN profiles p ON p.id = de.peer_id
    ORDER BY p.display_name ASC;
END;
$$ LANGUAGE plpgsql STABLE;

-- --------------------------------------------------------------------
-- 3. 2nd-Degree Connections ("People You May Know" via Mutual Friends)
-- Optimized using CTEs with mutual connection counts
-- --------------------------------------------------------------------
CREATE OR REPLACE FUNCTION get_second_degree_connections(
    p_user_id UUID,
    p_limit INT DEFAULT 20
)
RETURNS TABLE (
    person_id UUID,
    handle VARCHAR,
    display_name VARCHAR,
    headline VARCHAR,
    avatar_url TEXT,
    mutual_count BIGINT
) AS $$
BEGIN
    RETURN QUERY
    WITH first_degree AS (
        SELECT target_id AS friend_id
        FROM graph_edges
        WHERE source_id = p_user_id AND edge_type = 'CONNECTED_TO' AND status = 'ACCEPTED'
        UNION
        SELECT source_id AS friend_id
        FROM graph_edges
        WHERE target_id = p_user_id AND edge_type = 'CONNECTED_TO' AND status = 'ACCEPTED'
    ),
    second_degree_raw AS (
        SELECT 
            CASE WHEN e.source_id = f.friend_id THEN e.target_id ELSE e.source_id END AS cand_id
        FROM first_degree f
        JOIN graph_edges e 
          ON (e.source_id = f.friend_id OR e.target_id = f.friend_id)
         AND e.edge_type = 'CONNECTED_TO' 
         AND e.status = 'ACCEPTED'
    )
    SELECT 
        p.id AS person_id,
        p.handle,
        p.display_name,
        p.headline,
        p.avatar_url,
        COUNT(s.cand_id) AS mutual_count
    FROM second_degree_raw s
    JOIN profiles p ON p.id = s.cand_id
    WHERE s.cand_id != p_user_id
      AND s.cand_id NOT IN (SELECT friend_id FROM first_degree)
    GROUP BY p.id, p.handle, p.display_name, p.headline, p.avatar_url
    ORDER BY mutual_count DESC, p.display_name ASC
    LIMIT p_limit;
END;
$$ LANGUAGE plpgsql STABLE;

-- --------------------------------------------------------------------
-- 4. Network Distance & Shortest Path Traversal
-- Traverses connections to find distance and path between user A and user B
-- --------------------------------------------------------------------
CREATE OR REPLACE FUNCTION get_shortest_connection_path(
    p_from_id UUID,
    p_to_id UUID,
    p_max_depth INT DEFAULT 4
)
RETURNS TABLE (
    depth INT,
    path UUID[]
) AS $$
BEGIN
    RETURN QUERY
    WITH RECURSIVE search_graph(current_id, depth, path, is_cycle) AS (
        SELECT 
            p_from_id, 
            0, 
            ARRAY[p_from_id], 
            false
        UNION ALL
        SELECT 
            CASE WHEN e.source_id = sg.current_id THEN e.target_id ELSE e.source_id END,
            sg.depth + 1,
            sg.path || CASE WHEN e.source_id = sg.current_id THEN e.target_id ELSE e.source_id END,
            CASE WHEN e.source_id = sg.current_id THEN e.target_id ELSE e.source_id END = ANY(sg.path)
        FROM search_graph sg
        JOIN graph_edges e 
          ON (e.source_id = sg.current_id OR e.target_id = sg.current_id)
         AND e.edge_type = 'CONNECTED_TO' 
         AND e.status = 'ACCEPTED'
        WHERE NOT sg.is_cycle 
          AND sg.depth < p_max_depth
          AND sg.current_id != p_to_id
    )
    SELECT sg.depth, sg.path
    FROM search_graph sg
    WHERE sg.current_id = p_to_id
    ORDER BY sg.depth ASC
    LIMIT 1;
END;
$$ LANGUAGE plpgsql STABLE;

-- --------------------------------------------------------------------
-- 5. Graph-Driven Feed Generation (No Algorithmic Manipulation)
-- Prioritizes: Direct 1st-degree connections and followed entities
-- --------------------------------------------------------------------
CREATE OR REPLACE FUNCTION get_user_graph_feed(
    p_user_id UUID,
    p_limit INT DEFAULT 25,
    p_offset INT DEFAULT 0
)
RETURNS TABLE (
    post_id UUID,
    author_id UUID,
    author_type VARCHAR,
    author_handle VARCHAR,
    author_display_name VARCHAR,
    author_avatar_url TEXT,
    title VARCHAR,
    body_markdown TEXT,
    link_url TEXT,
    link_title TEXT,
    link_description TEXT,
    link_image_url TEXT,
    link_domain TEXT,
    tags TEXT[],
    has_tables BOOLEAN,
    has_images BOOLEAN,
    has_links BOOLEAN,
    like_count INT,
    comment_count INT,
    is_liked_by_me BOOLEAN,
    feed_reason VARCHAR,
    created_at TIMESTAMPTZ
) AS $$
BEGIN
    RETURN QUERY
    WITH network_sources AS (
        -- Self
        SELECT p_user_id AS entity_id, 'YOU'::VARCHAR AS reason
        UNION
        -- 1st degree connections
        SELECT target_id AS entity_id, 'CONNECTION'::VARCHAR AS reason
        FROM graph_edges
        WHERE source_id = p_user_id AND edge_type = 'CONNECTED_TO' AND status = 'ACCEPTED'
        UNION
        SELECT source_id AS entity_id, 'CONNECTION'::VARCHAR AS reason
        FROM graph_edges
        WHERE target_id = p_user_id AND edge_type = 'CONNECTED_TO' AND status = 'ACCEPTED'
        UNION
        -- Followed people & companies
        SELECT target_id AS entity_id, 'FOLLOWING'::VARCHAR AS reason
        FROM graph_edges
        WHERE source_id = p_user_id AND edge_type = 'FOLLOWS'
    )
    SELECT 
        p.id AS post_id,
        p.author_id,
        p.author_type,
        CASE 
            WHEN p.author_type = 'COMPANY' THEN c.handle 
            ELSE pr.handle 
        END AS author_handle,
        CASE 
            WHEN p.author_type = 'COMPANY' THEN c.name 
            ELSE pr.display_name 
        END AS author_display_name,
        CASE 
            WHEN p.author_type = 'COMPANY' THEN c.logo_url 
            ELSE pr.avatar_url 
        END AS author_avatar_url,
        p.title,
        p.body_markdown,
        p.link_url,
        p.link_title,
        p.link_description,
        p.link_image_url,
        p.link_domain,
        p.tags,
        p.has_tables,
        p.has_images,
        p.has_links,
        p.like_count,
        p.comment_count,
        EXISTS(SELECT 1 FROM post_reactions r WHERE r.post_id = p.id AND r.user_id = p_user_id) AS is_liked_by_me,
        ns.reason AS feed_reason,
        p.created_at
    FROM network_sources ns
    JOIN posts p ON p.author_id = ns.entity_id
    LEFT JOIN profiles pr ON pr.id = p.author_id AND p.author_type = 'PERSON'
    LEFT JOIN companies c ON c.id = p.author_id AND p.author_type = 'COMPANY'
    ORDER BY p.created_at DESC
    LIMIT p_limit OFFSET p_offset;
END;
$$ LANGUAGE plpgsql STABLE;

-- --------------------------------------------------------------------
-- 6. Unified Search Across Markdown, Links, and Tags
-- --------------------------------------------------------------------
CREATE OR REPLACE FUNCTION search_posts(
    p_query TEXT,
    p_has_tables BOOLEAN DEFAULT NULL,
    p_has_images BOOLEAN DEFAULT NULL,
    p_has_links BOOLEAN DEFAULT NULL,
    p_limit INT DEFAULT 25,
    p_offset INT DEFAULT 0
)
RETURNS TABLE (
    post_id UUID,
    author_id UUID,
    author_type VARCHAR,
    author_handle VARCHAR,
    author_display_name VARCHAR,
    title VARCHAR,
    body_markdown TEXT,
    link_url TEXT,
    link_title TEXT,
    link_description TEXT,
    link_image_url TEXT,
    link_domain TEXT,
    tags TEXT[],
    has_tables BOOLEAN,
    has_images BOOLEAN,
    has_links BOOLEAN,
    rank REAL,
    created_at TIMESTAMPTZ
) AS $$
DECLARE
    parsed_query tsquery;
BEGIN
    IF p_query IS NULL OR trim(p_query) = '' THEN
        parsed_query := to_tsquery('english', '.*');
    ELSE
        parsed_query := websearch_to_tsquery('english', p_query);
    END IF;

    RETURN QUERY
    SELECT 
        p.id AS post_id,
        p.author_id,
        p.author_type,
        CASE WHEN p.author_type = 'COMPANY' THEN c.handle ELSE pr.handle END AS author_handle,
        CASE WHEN p.author_type = 'COMPANY' THEN c.name ELSE pr.display_name END AS author_display_name,
        p.title,
        p.body_markdown,
        p.link_url,
        p.link_title,
        p.link_description,
        p.link_image_url,
        p.link_domain,
        p.tags,
        p.has_tables,
        p.has_images,
        p.has_links,
        ts_rank_cd(p.search_vector, parsed_query) AS rank,
        p.created_at
    FROM posts p
    LEFT JOIN profiles pr ON pr.id = p.author_id AND p.author_type = 'PERSON'
    LEFT JOIN companies c ON c.id = p.author_id AND p.author_type = 'COMPANY'
    WHERE (p_query IS NULL OR trim(p_query) = '' OR p.search_vector @@ parsed_query)
      AND (p_has_tables IS NULL OR p.has_tables = p_has_tables)
      AND (p_has_images IS NULL OR p.has_images = p_has_images)
      AND (p_has_links IS NULL OR p.has_links = p_has_links)
    ORDER BY rank DESC, p.created_at DESC
    LIMIT p_limit OFFSET p_offset;
END;
$$ LANGUAGE plpgsql STABLE;

-- --------------------------------------------------------------------
-- 7. GDPR Compliance Queries
-- --------------------------------------------------------------------
-- Full Data Portability Export (JSON)
CREATE OR REPLACE FUNCTION export_user_gdpr_data(p_user_id UUID)
RETURNS JSONB AS $$
DECLARE
    v_profile JSONB;
    v_posts JSONB;
    v_edges JSONB;
    v_reactions JSONB;
BEGIN
    SELECT to_jsonb(p) INTO v_profile FROM profiles p WHERE p.id = p_user_id;
    SELECT coalesce(jsonb_agg(to_jsonb(po)), '[]'::jsonb) INTO v_posts FROM posts po WHERE po.author_id = p_user_id;
    SELECT coalesce(jsonb_agg(to_jsonb(ge)), '[]'::jsonb) INTO v_edges FROM graph_edges ge WHERE ge.source_id = p_user_id OR ge.target_id = p_user_id;
    SELECT coalesce(jsonb_agg(to_jsonb(pr)), '[]'::jsonb) INTO v_reactions FROM post_reactions pr WHERE pr.user_id = p_user_id;

    RETURN jsonb_build_object(
        'exported_at', NOW(),
        'compliance', 'GDPR Article 20 - Right to Data Portability',
        'profile', v_profile,
        'posts', v_posts,
        'network_edges', v_edges,
        'reactions', v_reactions
    );
END;
$$ LANGUAGE plpgsql STABLE;

-- Right to be Forgotten (Full Hard Cascade Deletion)
CREATE OR REPLACE FUNCTION purge_user_gdpr(p_user_id UUID)
RETURNS VOID AS $$
BEGIN
    DELETE FROM graph_edges WHERE source_id = p_user_id OR target_id = p_user_id;
    DELETE FROM post_reactions WHERE user_id = p_user_id;
    DELETE FROM post_comments WHERE author_id = p_user_id;
    DELETE FROM posts WHERE author_id = p_user_id;
    DELETE FROM profiles WHERE id = p_user_id;
END;
$$ LANGUAGE plpgsql;
