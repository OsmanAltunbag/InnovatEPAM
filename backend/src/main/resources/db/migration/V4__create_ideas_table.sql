CREATE TABLE ideas (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'SUBMITTED',
    submitter_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version INT DEFAULT 0,
    CONSTRAINT status_enum CHECK (status IN ('SUBMITTED', 'UNDER_REVIEW', 'ACCEPTED', 'REJECTED'))
);

CREATE INDEX idx_ideas_status ON ideas (status);
CREATE INDEX idx_ideas_submitter_id ON ideas (submitter_id);
CREATE INDEX idx_ideas_created_at_desc ON ideas (created_at DESC);
CREATE UNIQUE INDEX idx_ideas_submitter_created ON ideas (submitter_id, created_at);
