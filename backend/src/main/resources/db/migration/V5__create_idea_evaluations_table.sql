CREATE TABLE idea_evaluations (
    id BIGSERIAL PRIMARY KEY,
    idea_id BIGINT NOT NULL REFERENCES ideas(id) ON DELETE CASCADE,
    evaluator_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    comment TEXT NOT NULL,
    status_snapshot VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT status_snapshot_enum CHECK (status_snapshot IS NULL OR status_snapshot IN ('UNDER_REVIEW', 'ACCEPTED', 'REJECTED'))
);

CREATE INDEX idx_idea_evaluations_idea_id ON idea_evaluations (idea_id);
CREATE INDEX idx_idea_evaluations_evaluator_id ON idea_evaluations (evaluator_id);
CREATE INDEX idx_idea_evaluations_created_at_asc ON idea_evaluations (created_at ASC);
