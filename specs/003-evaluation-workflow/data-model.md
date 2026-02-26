# Data Model: Evaluation Workflow

**Feature Branch**: `003-evaluation-workflow`  
**Date**: February 26, 2026  
**Version**: 1.0

## Overview

The Evaluation Workflow introduces evaluation tracking and comment functionality to the existing Idea Management System. This feature adds one new database table (`idea_evaluations`) to capture evaluator feedback, status transitions, and maintain an immutable audit trail of all evaluation actions.

## Database Schema

### 1. idea_evaluations table (NEW)

Stores evaluation actions, comments, and status transitions for ideas.

```sql
CREATE TABLE idea_evaluations (
    id BIGSERIAL PRIMARY KEY,
    idea_id BIGINT NOT NULL REFERENCES ideas(id) ON DELETE CASCADE,
    evaluator_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    comment TEXT NOT NULL,
    status_snapshot VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT status_snapshot_enum CHECK (
        status_snapshot IS NULL OR 
        status_snapshot IN ('UNDER_REVIEW', 'ACCEPTED', 'REJECTED')
    ),
    INDEX idx_idea_id (idea_id),
    INDEX idx_evaluator_id (evaluator_id),
    INDEX idx_created_at_asc (created_at ASC),
    INDEX idx_idea_created_composite (idea_id, created_at ASC)
);
```

**Fields**:
- `id`: Auto-incrementing primary key (BIGSERIAL/BIGINT)
- `idea_id`: Foreign key to `ideas.id`. Links evaluation to specific idea. ON DELETE CASCADE ensures evaluations are removed when idea is deleted.
- `evaluator_id`: Foreign key to `users.id`. References the user who performed the evaluation. ON DELETE RESTRICT prevents user deletion if they have evaluations.
- `comment`: Required TEXT field. Evaluator's feedback/reasoning. Max 5000 characters enforced at application layer.
- `status_snapshot`: Optional VARCHAR(50). Records what status the idea was transitioned to during this evaluation (UNDER_REVIEW, ACCEPTED, REJECTED). NULL for standalone comments without status change.
- `created_at`: Timestamp of when evaluation was recorded. Immutable. Used for chronological ordering.

**Indexes**:
- `idx_idea_id`: Fast retrieval of all evaluations for a specific idea
- `idx_evaluator_id`: Query evaluations by evaluator (for reporting/analytics)
- `idx_created_at_asc`: Chronological ordering for timeline views
- `idx_idea_created_composite`: Optimized composite index for retrieving idea's evaluation history in order

**Constraints**:
- `status_snapshot_enum`: Ensures only valid status values or NULL
- Foreign key to ideas: Maintains referential integrity, cascades deletes
- Foreign key to users: Prevents orphaned evaluations
- NOT NULL on comment: Every evaluation must have feedback text

**Audit Trail Design**:
- Records are immutable (no UPDATE or DELETE operations at application layer)
- All evaluation actions are logged with timestamp and evaluator identity
- Status transitions are captured via `status_snapshot` field
- Chronological history maintained by `created_at` ordering

---

### 2. ideas table (EXISTING - Extensions)

The existing `ideas` table already contains the `status` field needed for evaluation workflow. No schema changes required, but the status field is central to this feature.

**Relevant existing fields**:
```sql
-- from existing schema:
status VARCHAR(50) NOT NULL DEFAULT 'SUBMITTED',
CONSTRAINT status_enum CHECK (status IN ('SUBMITTED', 'UNDER_REVIEW', 'ACCEPTED', 'REJECTED'))
```

**Status Workflow**:
- **SUBMITTED**: Initial state when idea is created (default)
- **UNDER_REVIEW**: Evaluator has started reviewing
- **ACCEPTED**: Idea approved for implementation
- **REJECTED**: Idea declined with explanation

**Transitions** (enforced at application layer):
```
SUBMITTED
    ├→ UNDER_REVIEW (optional comment)
    ├→ ACCEPTED (requires comment)
    └→ REJECTED (requires comment)

UNDER_REVIEW
    ├→ ACCEPTED (requires comment)
    └→ REJECTED (requires comment)
```

**Terminal States**: ACCEPTED and REJECTED are terminal (no further transitions in MVP).

---

## JPA Entity Design

### IdeaEvaluation Entity (NEW)

```java
@Entity
@Table(name = "idea_evaluations")
public class IdeaEvaluation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idea_id", nullable = false)
    private Idea idea;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "evaluator_id", nullable = false)
    private User evaluator;

    @NotBlank(message = "Comment is required")
    @Size(max = 5000, message = "Comment cannot exceed 5000 characters")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String comment;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_snapshot", length = 50)
    private IdeaStatus statusSnapshot;  // NULL for standalone comments

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    // Getters and setters (no setCreatedAt to enforce immutability)
}
```

**Entity Relationships**:
- **IdeaEvaluation → Idea**: Many-to-One (many evaluations can belong to one idea)
- **IdeaEvaluation → User** (evaluator): Many-to-One (many evaluations by one user)
- **Idea → IdeaEvaluation**: One-to-Many (optional, for bi-directional navigation)

**Design Decisions**:
- `FetchType.LAZY` on idea relationship (avoid N+1 when loading evaluations)
- `FetchType.EAGER` on evaluator relationship (evaluator name needed for display)
- `statusSnapshot` is nullable (standalone comments don't change status)
- No setter for `createdAt` field (immutability enforced at code level)
- `updatable = false` on `createdAt` prevents accidental modification

---

### Idea Entity (EXISTING - Extension Points)

Add relationship to `IdeaEvaluation` for bi-directional navigation (optional):

```java
@Entity
@Table(name = "ideas")
public class Idea {
    // ... existing fields ...
    
    @OneToMany(mappedBy = "idea", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<IdeaEvaluation> evaluations = new ArrayList<>();
    
    // Helper method to add evaluation while maintaining relationship
    public void addEvaluation(IdeaEvaluation evaluation) {
        evaluations.add(evaluation);
        evaluation.setIdea(this);
    }
}
```

**Design Note**: The bi-directional relationship is optional. If not needed for queries, it can be omitted to keep entities decoupled.

---

## Data Flow

### Creating an Evaluation

1. Evaluator submits status change with comment via API
2. Service layer validates:
   - User has EVALUATOR or ADMIN role
   - Idea exists
   - Status transition is valid (workflow rules)
   - Comment is provided if required (ACCEPTED/REJECTED)
3. Transaction begins:
   - Create `IdeaEvaluation` record with comment and status snapshot
   - Update `ideas.status` to new status
   - Commit transaction (atomic: both succeed or both fail)
4. Evaluation history is queryable immediately

### Querying Evaluation History

1. Client requests `/api/v1/ideas/{id}/evaluations`
2. Service retrieves all `IdeaEvaluation` records for idea ID
3. Results ordered by `created_at ASC` (chronological timeline)
4. Each evaluation includes:
   - Evaluator name (from User join)
   - Comment text
   - Timestamp
   - Status change (if applicable)

---

## Validation Rules

### Database Level
- Foreign key constraints prevent orphaned records
- CHECK constraint ensures valid status values
- NOT NULL constraints enforce required fields

### Application Level
- Comment length: 1-5000 characters
- Status transitions validated against workflow state machine
- Comment required for ACCEPTED and REJECTED status changes
- Role-based access: Only EVALUATOR or ADMIN can create evaluations
- Submitter cannot evaluate their own ideas (business rule)

---

## Query Optimization

**Common Queries**:

1. **Get evaluation history for idea**:
   ```sql
   SELECT * FROM idea_evaluations 
   WHERE idea_id = ?
   ORDER BY created_at ASC;
   ```
   Optimized by: `idx_idea_created_composite`

2. **Get evaluations by evaluator**:
   ```sql
   SELECT * FROM idea_evaluations
   WHERE evaluator_id = ?
   ORDER BY created_at DESC;
   ```
   Optimized by: `idx_evaluator_id`, `idx_created_at_asc`

3. **Count evaluations for idea**:
   ```sql
   SELECT COUNT(*) FROM idea_evaluations WHERE idea_id = ?;
   ```
   Optimized by: `idx_idea_id`

**Performance Considerations**:
- Composite index on `(idea_id, created_at)` for most common query pattern
- EAGER fetch on evaluator relationship pre-loads evaluator name
- Consider pagination for ideas with many evaluations (future enhancement)

---

## Migration Strategy

### Flyway Migration Script

**File**: `V7__create_idea_evaluations_table.sql`

```sql
-- Create idea_evaluations table for evaluation workflow
CREATE TABLE idea_evaluations (
    id BIGSERIAL PRIMARY KEY,
    idea_id BIGINT NOT NULL,
    evaluator_id UUID NOT NULL,
    comment TEXT NOT NULL,
    status_snapshot VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_evaluation_idea 
        FOREIGN KEY (idea_id) 
        REFERENCES ideas(id) 
        ON DELETE CASCADE,
    
    CONSTRAINT fk_evaluation_evaluator 
        FOREIGN KEY (evaluator_id) 
        REFERENCES users(id) 
        ON DELETE RESTRICT,
    
    CONSTRAINT status_snapshot_enum CHECK (
        status_snapshot IS NULL OR 
        status_snapshot IN ('UNDER_REVIEW', 'ACCEPTED', 'REJECTED')
    )
);

-- Indexes for query performance
CREATE INDEX idx_idea_evaluations_idea_id ON idea_evaluations(idea_id);
CREATE INDEX idx_idea_evaluations_evaluator_id ON idea_evaluations(evaluator_id);
CREATE INDEX idx_idea_evaluations_created_at ON idea_evaluations(created_at ASC);
CREATE INDEX idx_idea_evaluations_idea_created ON idea_evaluations(idea_id, created_at ASC);

-- Add comment for documentation
COMMENT ON TABLE idea_evaluations IS 'Stores evaluation actions and comments for ideas with immutable audit trail';
```

**Rollback Strategy** (if needed):
```sql
DROP TABLE IF EXISTS idea_evaluations CASCADE;
```

---

## Data Integrity

### Constraints Summary

| Constraint | Purpose | Enforcement |
|------------|---------|-------------|
| Primary Key (id) | Unique identifier | Database |
| Foreign Key (idea_id) | Referential integrity | Database |
| Foreign Key (evaluator_id) | Referential integrity | Database |
| NOT NULL (comment) | Require evaluation feedback | Database |
| CHECK (status_snapshot) | Valid status values | Database |
| NOT NULL (created_at) | Audit timestamp | Database |
| Max length (comment) | Prevent abuse | Application (@Size) |
| Workflow validation | Valid status transitions | Application (Service layer) |
| Role-based access | Authorization | Application (@PreAuthorize) |

### Transaction Boundaries

All evaluation operations wrapped in transactions:
- Status update + evaluation creation = single transaction
- Rollback on validation failure
- Ensures atomicity (status and evaluation always in sync)

---

## Future Enhancements

### Out of Scope for MVP
- Edit evaluation comments (immutable in MVP)
- Delete evaluation comments (audit trail requirement)
- Evaluation versioning (track comment edits)
- Evaluation attachments (file uploads in comments)
- Evaluation templates (pre-defined feedback options)
- Multi-evaluator consensus (approval workflows)
- Evaluation scoring/rating (1-5 stars)

### Potential Schema Extensions

If future phases require additional features:
- Add `updated_at` if evaluation editing is allowed
- Add `is_deleted` soft delete flag instead of hard deletes
- Add `parent_evaluation_id` for comment threading
- Add `priority` or `severity` fields for categorization
- Add `evaluation_type` enum for different evaluation categories

---

**Version**: 1.0  
**Last Updated**: February 26, 2026  
**Status**: Ready for implementation
