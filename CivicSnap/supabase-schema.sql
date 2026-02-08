-- CivicSnap Supabase Schema (for reference - JPA ddl-auto=update creates tables automatically)
-- Run this only if you need to create tables manually

CREATE TABLE IF NOT EXISTS communities (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT
);

CREATE TABLE IF NOT EXISTS authority_users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    unique_code VARCHAR(255) NOT NULL UNIQUE,
    assigned_community_id BIGINT NOT NULL REFERENCES communities(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS issues (
    id BIGSERIAL PRIMARY KEY,
    image_url TEXT,
    description TEXT,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    road VARCHAR(255),
    city VARCHAR(255),
    country VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_id VARCHAR(255) NOT NULL,
    community_id BIGINT REFERENCES communities(id),
    status VARCHAR(50) DEFAULT 'pending',
    delivery_confirmed BOOLEAN DEFAULT true,
    tags JSONB,
    upvote_count INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS issue_upvotes (
    id BIGSERIAL PRIMARY KEY,
    issue_id BIGINT NOT NULL REFERENCES issues(id),
    user_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(issue_id, user_id)
);

CREATE TABLE IF NOT EXISTS issue_comments (
    id BIGSERIAL PRIMARY KEY,
    issue_id BIGINT NOT NULL REFERENCES issues(id),
    user_id VARCHAR(255) NOT NULL,
    comment_text TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS news (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    image_url TEXT,
    link VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create Supabase Storage bucket named "issues" in Dashboard and set it to PUBLIC
-- Storage URL format: https://{project}.supabase.co/storage/v1/object/public/issues/{path}
