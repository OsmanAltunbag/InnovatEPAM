CREATE TABLE idea_attachments (
    id BIGSERIAL PRIMARY KEY,
    idea_id BIGINT NOT NULL REFERENCES ideas(id) ON DELETE CASCADE,
    original_filename VARCHAR(255) NOT NULL,
    stored_filename VARCHAR(255) NOT NULL,
    file_type VARCHAR(20) NOT NULL,
    file_size BIGINT NOT NULL,
    storage_location VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT file_type_enum CHECK (file_type IN ('PDF', 'PNG')),
    CONSTRAINT file_size_positive CHECK (file_size > 0),
    CONSTRAINT idea_attachments_idea_unique UNIQUE (idea_id)
);

CREATE INDEX idx_idea_attachments_idea_id ON idea_attachments (idea_id);
