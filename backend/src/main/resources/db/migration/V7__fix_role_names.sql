-- V7__fix_role_names.sql
-- Fix role naming to comply with Spring Security authority naming rules
-- "evaluator/admin" contains "/" which is invalid in Spring Security authorities
-- ROLE_EVALUATOR/ADMIN becomes invalid, causing all authorization checks to fail

-- Rename 'evaluator/admin' to 'admin' (admin role includes evaluator permissions)
UPDATE roles SET name = 'admin' WHERE name = 'evaluator/admin';

-- Add 'evaluator' as a separate role for fine-grained access control in future
INSERT INTO roles (id, name, created_at) VALUES
    ('00000000-0000-0000-0000-000000000003', 'evaluator', CURRENT_TIMESTAMP);
