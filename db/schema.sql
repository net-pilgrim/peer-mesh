-- ====================================================================
-- PeerMesh: Privacy-First Professional Network
-- PostgreSQL 18+ (18/19) Schema with Native Graph & Unified Search
-- ====================================================================

-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- --------------------------------------------------------------------
-- 1. Profiles (People) - Minimal PII, GDPR Compliant by Design
-- --------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    handle VARCHAR(64) UNIQUE NOT NULL,
    display_name VARCHAR(128) NOT NULL,
    headline VARCHAR(256),
    bio_markdown TEXT DEFAULT '',
    avatar_url TEXT DEFAULT '',
    banner_url TEXT DEFAULT '',
    skills TEXT[] DEFAULT '{}',
    -- Decentralized Messaging Protocols (No in-site chat storage)
    matrix_handle VARCHAR(128) DEFAULT '',       -- e.g. @alice:matrix.org
    nostr_npub VARCHAR(128) DEFAULT '',          -- e.g. npub1...
    simplex_address TEXT DEFAULT '',             -- e.g. simplex:https://simplex.chat/contact#/?...
    session_id VARCHAR(128) DEFAULT '',          -- e.g. 05...
    email_contact VARCHAR(256) DEFAULT '',       -- optional public contact
    pgp_fingerprint VARCHAR(128) DEFAULT '',     -- optional OpenPGP key
    -- GitHub Integration
    github_username VARCHAR(128) DEFAULT '',
    github_content_repo VARCHAR(256) DEFAULT '',
    -- Location (City/Country only, no GPS tracking, privacy preserving)
    location VARCHAR(128) DEFAULT '',
    -- Auditing & Lifecycle
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_profiles_handle ON profiles(handle);
CREATE INDEX IF NOT EXISTS idx_profiles_skills ON profiles USING gin(skills);

-- --------------------------------------------------------------------
-- 2. Companies / Organizations
-- --------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS companies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    handle VARCHAR(64) UNIQUE NOT NULL,
    name VARCHAR(128) NOT NULL,
    tagline VARCHAR(256) DEFAULT '',
    description_markdown TEXT DEFAULT '',
    website_url TEXT DEFAULT '',
    logo_url TEXT DEFAULT '',
    industry VARCHAR(128) DEFAULT '',
    headquarters VARCHAR(128) DEFAULT '',        -- City/Country only (no precise tracking)
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_companies_handle ON companies(handle);
CREATE INDEX IF NOT EXISTS idx_companies_industry ON companies(industry);

-- --------------------------------------------------------------------
-- 3. Native Graph Edges (Connections, Follows, Affiliations)
-- --------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS graph_edges (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    source_id UUID NOT NULL,
    source_type VARCHAR(16) NOT NULL CHECK (source_type IN ('PERSON', 'COMPANY')),
    target_id UUID NOT NULL,
    target_type VARCHAR(16) NOT NULL CHECK (target_type IN ('PERSON', 'COMPANY')),
    edge_type VARCHAR(32) NOT NULL CHECK (edge_type IN ('CONNECTED_TO', 'FOLLOWS', 'WORKS_AT', 'FOUNDED', 'ADVISES')),
    status VARCHAR(16) NOT NULL DEFAULT 'ACCEPTED' CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED', 'BLOCKED')),
    properties JSONB NOT NULL DEFAULT '{}'::jsonb,
    weight FLOAT NOT NULL DEFAULT 1.0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_graph_edge UNIQUE (source_id, target_id, edge_type)
);

CREATE INDEX IF NOT EXISTS idx_graph_edges_source ON graph_edges(source_id, edge_type, status);
CREATE INDEX IF NOT EXISTS idx_graph_edges_target ON graph_edges(target_id, edge_type, status);
CREATE INDEX IF NOT EXISTS idx_graph_edges_bidi ON graph_edges(source_id, target_id, edge_type);

-- --------------------------------------------------------------------
-- 4. Unified Posts (Posts + Links + Markdown + Tables + Images)
-- --------------------------------------------------------------------
-- No distinction between "link" and "post". Everything is unified,
-- searchable, supports full Markdown, rich embeds, tables, and media.
CREATE TABLE IF NOT EXISTS posts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    author_id UUID NOT NULL,
    author_type VARCHAR(16) NOT NULL DEFAULT 'PERSON' CHECK (author_type IN ('PERSON', 'COMPANY')),
    title VARCHAR(256),
    body_markdown TEXT NOT NULL,
    -- Unified Link metadata (optional rich card attached or extracted)
    link_url TEXT,
    link_title TEXT,
    link_description TEXT,
    link_image_url TEXT,
    link_domain TEXT,
    -- Tags
    tags TEXT[] DEFAULT '{}',
    -- Computed content indicators for instant filtering
    has_tables BOOLEAN GENERATED ALWAYS AS (body_markdown ~ '\|[^\n]+\|\s*\n\|[\s:-|]+\|') STORED,
    has_images BOOLEAN GENERATED ALWAYS AS (body_markdown ~ '!\[.*?\]\(.*?\)') STORED,
    has_links  BOOLEAN GENERATED ALWAYS AS (link_url IS NOT NULL OR body_markdown ~ 'https?://') STORED,
    -- PostgreSQL Unified Full-Text Search Vector
    search_vector tsvector GENERATED ALWAYS AS (
        setweight(to_tsvector('english', coalesce(title, '')), 'A') ||
        setweight(to_tsvector('english', coalesce(body_markdown, '')), 'B') ||
        setweight(to_tsvector('english', coalesce(link_title, '') || ' ' || coalesce(link_description, '') || ' ' || coalesce(link_url, '')), 'C') ||
        setweight(to_tsvector('english', coalesce(array_to_string(tags, ' '), '')), 'A')
    ) STORED,
    like_count INT NOT NULL DEFAULT 0,
    comment_count INT NOT NULL DEFAULT 0,
    -- GitHub Self-Hosting Provenance
    github_repo VARCHAR(256),
    github_path VARCHAR(256),
    github_commit_sha VARCHAR(64),
    is_self_hosted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_posts_search ON posts USING gin(search_vector);
CREATE INDEX IF NOT EXISTS idx_posts_author ON posts(author_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_posts_created_at ON posts(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_posts_tags ON posts USING gin(tags);
CREATE INDEX IF NOT EXISTS idx_posts_filters ON posts(has_tables, has_images, has_links);

-- --------------------------------------------------------------------
-- 5. Reactions & Comments
-- --------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS post_reactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id UUID NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    reaction_type VARCHAR(16) NOT NULL DEFAULT 'LIKE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_post_reaction UNIQUE (post_id, user_id)
);

CREATE TABLE IF NOT EXISTS post_comments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id UUID NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    author_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    body_markdown TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_post_comments_post ON post_comments(post_id, created_at ASC);

-- --------------------------------------------------------------------
-- 6. Trigger for reaction and comment counts (zero server compute)
-- --------------------------------------------------------------------
CREATE OR REPLACE FUNCTION update_post_metrics() RETURNS TRIGGER AS $$
BEGIN
    IF TG_TABLE_NAME = 'post_reactions' THEN
        IF TG_OP = 'INSERT' THEN
            UPDATE posts SET like_count = like_count + 1 WHERE id = NEW.post_id;
        ELSIF TG_OP = 'DELETE' THEN
            UPDATE posts SET like_count = GREATEST(0, like_count - 1) WHERE id = OLD.post_id;
        END IF;
    ELSIF TG_TABLE_NAME = 'post_comments' THEN
        IF TG_OP = 'INSERT' THEN
            UPDATE posts SET comment_count = comment_count + 1 WHERE id = NEW.post_id;
        ELSIF TG_OP = 'DELETE' THEN
            UPDATE posts SET comment_count = GREATEST(0, comment_count - 1) WHERE id = OLD.post_id;
        END IF;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_reaction_count ON post_reactions;
CREATE TRIGGER trg_reaction_count
AFTER INSERT OR DELETE ON post_reactions
FOR EACH ROW EXECUTE FUNCTION update_post_metrics();

DROP TRIGGER IF EXISTS trg_comment_count ON post_comments;
CREATE TRIGGER trg_comment_count
AFTER INSERT OR DELETE ON post_comments
FOR EACH ROW EXECUTE FUNCTION update_post_metrics();

-- --------------------------------------------------------------------
-- 7. Work Experiences (Career History & Clinical Affiliations)
-- --------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS work_experiences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    profile_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    title VARCHAR(128) NOT NULL,
    company_name VARCHAR(128) NOT NULL,
    company_id UUID REFERENCES companies(id) ON DELETE SET NULL,
    location VARCHAR(128) DEFAULT '',
    start_date VARCHAR(32) NOT NULL,
    end_date VARCHAR(32) DEFAULT '',
    is_current BOOLEAN NOT NULL DEFAULT FALSE,
    description_markdown TEXT DEFAULT '',
    skills TEXT[] DEFAULT '{}',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_work_exp_profile ON work_experiences(profile_id, start_date DESC);
CREATE INDEX IF NOT EXISTS idx_work_exp_company ON work_experiences(company_id);

-- --------------------------------------------------------------------
-- 8. Educations (Academic Degrees, Residencies & Universities)
-- --------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS educations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    profile_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    institution_name VARCHAR(160) NOT NULL,
    degree VARCHAR(128) NOT NULL,
    field_of_study VARCHAR(128) DEFAULT '',
    start_date VARCHAR(32) NOT NULL,
    end_date VARCHAR(32) DEFAULT '',
    is_current BOOLEAN NOT NULL DEFAULT FALSE,
    grade VARCHAR(64) DEFAULT '',
    activities_markdown TEXT DEFAULT '',
    skills TEXT[] DEFAULT '{}',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_educations_profile ON educations(profile_id, start_date DESC);
CREATE INDEX IF NOT EXISTS idx_educations_institution ON educations(institution_name);

-- --------------------------------------------------------------------
-- 9. Job Announcements (Sovereign Opportunities & Object Storage Posts)
-- --------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS job_announcements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id UUID REFERENCES companies(id) ON DELETE SET NULL,
    company_name VARCHAR(128) NOT NULL,
    company_logo_url TEXT DEFAULT '',
    title VARCHAR(160) NOT NULL,
    location VARCHAR(128) DEFAULT '',
    employment_type VARCHAR(32) NOT NULL DEFAULT 'FULL_TIME',
    department VARCHAR(64) NOT NULL DEFAULT 'Engineering',
    experience_level VARCHAR(64) DEFAULT 'Senior',
    salary_range VARCHAR(64) DEFAULT '',
    description_markdown TEXT NOT NULL,
    skills TEXT[] DEFAULT '{}',
    apply_contact VARCHAR(256) DEFAULT '',
    storage_key VARCHAR(128) DEFAULT '',
    featured BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_jobs_dept ON job_announcements(department);
CREATE INDEX IF NOT EXISTS idx_jobs_emp_type ON job_announcements(employment_type);
CREATE INDEX IF NOT EXISTS idx_jobs_comp ON job_announcements(company_id);
CREATE INDEX IF NOT EXISTS idx_jobs_created ON job_announcements(created_at DESC);


