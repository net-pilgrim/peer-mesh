-- ====================================================================
-- PeerMesh: Flyway Migration V1 - Core Relational & Graph Schema
-- Supports both PostgreSQL 18+ and H2 in PostgreSQL Mode
-- ====================================================================

-- 1. Profiles Table (People, GDPR-compliant minimal PII, decentralized protocols)
CREATE TABLE IF NOT EXISTS profiles (
    id UUID PRIMARY KEY,
    handle VARCHAR(64) UNIQUE NOT NULL,
    display_name VARCHAR(128) NOT NULL,
    headline VARCHAR(256),
    bio_markdown TEXT DEFAULT '',
    avatar_url TEXT DEFAULT '',
    banner_url TEXT DEFAULT '',
    skills VARCHAR(512) DEFAULT '',
    matrix_handle VARCHAR(128) DEFAULT '',
    nostr_npub VARCHAR(128) DEFAULT '',
    simplex_address TEXT DEFAULT '',
    session_id VARCHAR(128) DEFAULT '',
    email_contact VARCHAR(256) DEFAULT '',
    pgp_fingerprint VARCHAR(128) DEFAULT '',
    github_username VARCHAR(128) DEFAULT '',
    github_content_repo VARCHAR(256) DEFAULT '',
    location VARCHAR(128) DEFAULT '',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_profiles_handle ON profiles(handle);

-- 2. Companies / Organizations Table
CREATE TABLE IF NOT EXISTS companies (
    id UUID PRIMARY KEY,
    handle VARCHAR(64) UNIQUE NOT NULL,
    name VARCHAR(128) NOT NULL,
    tagline VARCHAR(256) DEFAULT '',
    description_markdown TEXT DEFAULT '',
    website_url TEXT DEFAULT '',
    logo_url TEXT DEFAULT '',
    industry VARCHAR(128) DEFAULT '',
    headquarters VARCHAR(128) DEFAULT '',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_companies_handle ON companies(handle);
CREATE INDEX IF NOT EXISTS idx_companies_industry ON companies(industry);

-- 3. Native Graph Edges Table (Connections, Follows, Affiliations, Alumni)
CREATE TABLE IF NOT EXISTS graph_edges (
    id UUID PRIMARY KEY,
    source_id UUID NOT NULL,
    source_type VARCHAR(16) NOT NULL,
    target_id UUID NOT NULL,
    target_type VARCHAR(16) NOT NULL,
    edge_type VARCHAR(32) NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'ACCEPTED',
    properties TEXT DEFAULT '{}',
    weight DOUBLE PRECISION DEFAULT 1.0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_graph_edge UNIQUE (source_id, target_id, edge_type)
);

CREATE INDEX IF NOT EXISTS idx_graph_edges_source ON graph_edges(source_id, edge_type);
CREATE INDEX IF NOT EXISTS idx_graph_edges_target ON graph_edges(target_id, edge_type);

-- 4. Unified Posts Table (Posts + Links + Markdown + Tables + Images)
CREATE TABLE IF NOT EXISTS posts (
    id UUID PRIMARY KEY,
    author_id UUID NOT NULL,
    author_type VARCHAR(16) NOT NULL DEFAULT 'PERSON',
    title VARCHAR(256),
    body_markdown TEXT NOT NULL,
    link_url TEXT,
    link_title TEXT,
    link_description TEXT,
    link_image_url TEXT,
    link_domain TEXT,
    tags VARCHAR(512) DEFAULT '',
    has_tables BOOLEAN DEFAULT FALSE,
    has_images BOOLEAN DEFAULT FALSE,
    has_links BOOLEAN DEFAULT FALSE,
    like_count INT DEFAULT 0,
    comment_count INT DEFAULT 0,
    github_repo VARCHAR(256),
    github_path VARCHAR(256),
    github_commit_sha VARCHAR(64),
    is_self_hosted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_posts_author ON posts(author_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_posts_created ON posts(created_at DESC);

-- 5. Post Reactions Table
CREATE TABLE IF NOT EXISTS post_reactions (
    id UUID PRIMARY KEY,
    post_id UUID NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    reaction_type VARCHAR(16) NOT NULL DEFAULT 'LIKE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_post_reaction UNIQUE (post_id, user_id)
);

-- 6. Post Comments Table
CREATE TABLE IF NOT EXISTS post_comments (
    id UUID PRIMARY KEY,
    post_id UUID NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    author_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    body_markdown TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_post_comments_post ON post_comments(post_id, created_at ASC);

-- 7. Work Experiences Table (Career History)
CREATE TABLE IF NOT EXISTS work_experiences (
    id UUID PRIMARY KEY,
    profile_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    title VARCHAR(128) NOT NULL,
    company_name VARCHAR(128) NOT NULL,
    company_id UUID REFERENCES companies(id) ON DELETE SET NULL,
    location VARCHAR(128) DEFAULT '',
    start_date VARCHAR(32) NOT NULL,
    end_date VARCHAR(32) DEFAULT '',
    is_current BOOLEAN NOT NULL DEFAULT FALSE,
    description_markdown TEXT DEFAULT '',
    skills VARCHAR(512) DEFAULT '',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_work_exp_profile ON work_experiences(profile_id, start_date DESC);
CREATE INDEX IF NOT EXISTS idx_work_exp_company ON work_experiences(company_id);

-- 8. Educations Table (Academic Credentials, Degrees, Universities)
CREATE TABLE IF NOT EXISTS educations (
    id UUID PRIMARY KEY,
    profile_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    institution_name VARCHAR(160) NOT NULL,
    degree VARCHAR(128) NOT NULL,
    field_of_study VARCHAR(128) DEFAULT '',
    start_date VARCHAR(32) NOT NULL,
    end_date VARCHAR(32) DEFAULT '',
    is_current BOOLEAN NOT NULL DEFAULT FALSE,
    grade VARCHAR(64) DEFAULT '',
    activities_markdown TEXT DEFAULT '',
    skills VARCHAR(512) DEFAULT '',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_educations_profile ON educations(profile_id, start_date DESC);
CREATE INDEX IF NOT EXISTS idx_educations_institution ON educations(institution_name);
