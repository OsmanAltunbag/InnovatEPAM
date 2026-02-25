# Data Model: Idea Management System

**Feature Branch**: `002-idea-management`  
**Date**: February 25, 2026  
**Version**: 1.0

## Overview

The Idea Management feature introduces three new database tables (`ideas`, `idea_evaluations`, `idea_attachments`) to support the innovation submission and evaluation workflow. These tables extend the existing user authentication system without modifying existing schemas.

## Database Schema

### 1. ideas table

Stores submitted innovation proposals.

```sql
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
    
    CONSTRAINT status_enum CHECK (status IN ('SUBMITTED', 'UNDER_REVIEW', 'ACCEPTED', 'REJECTED')),
    INDEX idx_status (status),
    INDEX idx_submitter_id (submitter_id),
    INDEX idx_created_at_desc (created_at DESC),
    UNIQUE INDEX idx_submitter_created (submitter_id, created_at)
);
```

**Fields**:
- `id`: Auto-incrementing primary key (BIGINT)
- `title`: Required, max 255 chars. Idea title/subject.
- `description`: Required, TEXT. Full description of the innovation.
- `category`: Required, VARCHAR(50). Classification category (e.g., "Process Improvement", "Cost Reduction", "Innovation")
- `status`: Enumeration (SUBMITTED, UNDER_REVIEW, ACCEPTED, REJECTED). Initial: SUBMITTED
- `submitter_id`: Foreign key to `users.id`. References the user who submitted the idea. ON DELETE RESTRICT to prevent orphaned ideas.
- `created_at`: Timestamp, auto-set to insertion time. Immutable after creation.
- `updated_at`: Timestamp, auto-updated on any modification. Tracks last change.
- `version`: Optional optimistic locking field (incremented on each update). Prevents lost-update anomaly in concurrent scenarios.

**Indexes**:
- `idx_status`: Enables fast filtering by status (dashboard views)
- `idx_submitter_id`: Enables fast retrieval of user's own ideas
- `idx_created_at_desc`: Enables ordered listing (most recent first)
- `idx_submitter_created`: Composite index for checking submitter's recent ideas

**Constraints**:
- `status_enum`: Ensures only valid status values
- Foreign key to users: Ensures referential integrity

---

### 2. idea_evaluations table

Stores evaluator feedback and status transitions for ideas.

```sql
CREATE TABLE idea_evaluations (
    id BIGSERIAL PRIMARY KEY,
    idea_id BIGINT NOT NULL REFERENCES ideas(id) ON DELETE CASCADE,
    evaluator_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    comment TEXT NOT NULL,
    status_snapshot VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT status_snapshot_enum CHECK (status_snapshot IS NULL OR status_snapshot IN ('UNDER_REVIEW', 'ACCEPTED', 'REJECTED')),
    INDEX idx_idea_id (idea_id),
    INDEX idx_evaluator_id (evaluator_id),
    INDEX idx_created_at_asc (created_at ASC)
);
```

**Fields**:
- `id`: Auto-incrementing primary key (BIGINT)
- `idea_id`: Foreign key to `ideas.id`. ON DELETE CASCADE ensures evaluation comments are deleted when idea is deleted.
- `evaluator_id`: Foreign key to `users.id`. References the evaluator/admin who provided feedback. ON DELETE RESTRICT prevents orphaning.
- `comment`: Required, TEXT. Evaluator's feedback or justification for rejection.
- `status_snapshot`: Optional, VARCHAR(50). The status the idea transitioned to with this comment (e.g., "ACCEPTED", "REJECTED"). NULL if comment was added without status change.
- `created_at`: Timestamp, auto-set. Immutable. Defines chronological order of evaluations.

**Indexes**:
- `idx_idea_id`: Fast retrieval of all evaluations for a specific idea
- `idx_evaluator_id`: Fast retrieval of evaluations by specific evaluator (optional, for analytics)
- `idx_created_at_asc`: Ensures sorted retrieval of evaluation history (oldest first)

**Constraints**:
- `status_snapshot_enum`: Only valid status transition values allowed
- Foreign keys: Maintain referential integrity

**Note**: Audit trail is implicit: evaluations can never be deleted (only new ones added), providing immutable history.

---

### 3. idea_attachments table

Stores metadata for file attachments associated with ideas.

```sql
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
    INDEX idx_idea_id (idea_id),
    UNIQUE (idea_id)
);
```

**Fields**:
- `id`: Auto-incrementing primary key (BIGINT)
- `idea_id`: Foreign key to `ideas.id`. One-to-one relationship (each idea has at most one attachment). ON DELETE CASCADE.
- `original_filename`: VARCHAR(255). Original filename as uploaded by user (for download display).
- `stored_filename`: VARCHAR(255). Renamed filename for secure storage (UUID-based to prevent path traversal).
- `file_type`: VARCHAR(20). Enumeration: PDF or PNG. Validated at upload time.
- `file_size`: BIGINT. File size in bytes. Useful for quota/limit checks.
- `storage_location`: VARCHAR(500). Filesystem path or S3 key where file is stored. e.g., `/uploads/ideas/1/1708953600000_proposal.pdf`
- `created_at`: Timestamp, auto-set. Upload time.

**Indexes**:
- `idx_idea_id`: Fast retrieval of attachment metadata for a specific idea
- `UNIQUE (idea_id)`: Enforces one attachment per idea (alternative: remove constraint if multiple attachments planned)

**Constraints**:
- `file_type_enum`: Whitelist of allowed file types
- `file_size_positive`: Prevents invalid file sizes
- Foreign key with CASCADE: Attachments deleted when idea is deleted

---

## JPA Entity Mappings

### Idea Entity

```java
@Entity
@Table(name = "ideas")
public class Idea {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
    @Column(nullable = false, length = 255)
    private String title;
    
    @NotBlank(message = "Description is required")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;
    
    @NotNull(message = "Category is required")
    @Column(nullable = false, length = 50)
    private String category;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private IdeaStatus status = IdeaStatus.SUBMITTED;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "submitter_id", nullable = false)
    private User submitter;
    
    @OneToOne(mappedBy = "idea", cascade = CascadeType.ALL, orphanRemoval = true)
    private IdeaAttachment attachment;
    
    @OneToMany(mappedBy = "idea", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<IdeaEvaluation> evaluations = new ArrayList<>();
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Version
    @Column(name = "version")
    private Integer version;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        status = IdeaStatus.SUBMITTED;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getters, Setters, Builder
}

enum IdeaStatus {
    SUBMITTED,
    UNDER_REVIEW,
    ACCEPTED,
    REJECTED
}
```

**Key Annotations**:
- `@Enumerated(EnumType.STRING)`: Stores enum as string in database
- `@OrderBy("createdAt ASC")`: Ensures evaluations are loaded in chronological order
- `@Version`: Optimistic locking field to detect concurrent updates
- `@PrePersist` / `@PreUpdate`: Lifecycle hooks for timestamp management

**Relationships**:
- Submitter (User): Many-to-one, eager loading (used in responses)
- Attachment (IdeaAttachment): One-to-one, cascade all (delete idea → delete attachment)
- Evaluations (List<IdeaEvaluation>): One-to-many, cascade all, orphan removal (delete evaluation when removed from list)

---

### IdeaEvaluation Entity

```java
@Entity
@Table(name = "idea_evaluations")
public class IdeaEvaluation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idea_id", nullable = false)
    private Idea idea;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "evaluator_id", nullable = false)
    private User evaluator;
    
    @NotBlank(message = "Comment is required")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String comment;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status_snapshot", length = 50)
    private IdeaStatus statusSnapshot;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Getters, Setters, Builder
}
```

**Key Annotations**:
- `@EagerLoading`: Evaluator and idea loaded immediately (small items, used in all responses)
- `statusSnapshot`: Nullable; captures what status was set to, if any

**Invariants**:
- Cannot be modified after creation (no @PreUpdate)
- Cannot be deleted (immutable audit trail)

---

### IdeaAttachment Entity

```java
@Entity
@Table(name = "idea_attachments")
public class IdeaAttachment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "idea_id", nullable = false, unique = true)
    private Idea idea;
    
    @NotBlank(message = "Original filename is required")
    @Column(nullable = false, length = 255)
    private String originalFilename;
    
    @NotBlank(message = "Stored filename is required")
    @Column(nullable = false, length = 255)
    private String storedFilename;
    
    @NotNull(message = "File type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false, length = 20)
    private FileType fileType;
    
    @NotNull(message = "File size is required")
    @Positive(message = "File size must be greater than 0")
    @Column(nullable = false)
    private Long fileSize;
    
    @NotBlank(message = "Storage location is required")
    @Column(nullable = false, length = 500)
    private String storageLocation;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Getters, Setters, Builder
}

enum FileType {
    PDF,
    PNG
}
```

**Key Annotations**:
- `@OneToOne`: Bidirectional relationship to Idea
- `unique = true`: Enforces one attachment per idea
- `@Positive`: Validates file size is greater than 0
- `@Enumerated(EnumType.STRING)`: Stores file type as string (PDF, PNG)

---

## Key Design Decisions

### 1. Optimistic Locking (Idea.version)

**Rationale**: Prevents lost-update anomaly if two evaluators attempt concurrent status changes.
- **How it works**: JPA increments version on each update; concurrent update fails with `OptimisticLockException`
- **Handling**: Service layer catches exception, logs conflict, returns error to UI
- **Alternative rejected**: Pessimistic locking (database row locks) would create bottlenecks for idea listing

### 2. One Attachment Per Idea (UNIQUE constraint)

**Rationale**: Specification requires "single file attachment per idea"
- **How it works**: `UNIQUE (idea_id)` on `idea_attachments` table prevents multiple rows for same idea
- **Behavior**: Updating attachment replaces old file (delete old row, insert new)
- **Future enhancement**: Remove UNIQUE constraint + add version field if multi-file support needed

### 3. Immutable Evaluation History (No deletion)

**Rationale**: Provides audit trail; disallows tampering with historical feedback
- **How it works**: IdeaEvaluation has no update/delete methods; only create allowed
- **Storage**: `created_at` timestamp is immutable; supports sorting
- **Status snapshot**: Captures what status was set to at time of evaluation

### 4. Cascade Delete for Attachments

**Rationale**: File is meaningless without idea; should be cleaned up automatically
- **How it works**: `ON DELETE CASCADE` on `idea_attachments.idea_id` foreign key
- **Behavior**: Deleting idea → attachment deleted → cleanup service deletes file from storage
- **Note**: Deletion is cascaded at database level; application code must call file cleanup service

### 5. ON DELETE RESTRICT for User Foreign Keys

**Rationale**: Prevent deletion of users that would orphan ideas or evaluations
- **How it works**: Attempt to delete user with submitted ideas fails with constraint violation
- **Implication**: User management system must handle user deactivation (soft delete) instead
- **Alternative**: ON DELETE SET NULL (not chosen; want to preserve original submitter identity)

---

## Repository Queries

### IdeaRepository

```java
public interface IdeaRepository extends JpaRepository<Idea, Long> {
    
    // Find by status
    List<Idea> findByStatus(IdeaStatus status);
    Page<Idea> findByStatus(IdeaStatus status, Pageable pageable);
    
    // Find by submitter
    List<Idea> findBySubmit
Id(UUID submitterId);
    Page<Idea> findBySubmitterId(UUID submitterId, Pageable pageable);
    
    // Find by category and status
    List<Idea> findByCategoryAndStatus(String category, IdeaStatus status);
    
    // Count by status
    long countByStatus(IdeaStatus status);
}
```

### IdeaEvaluationRepository

```java
public interface IdeaEvaluationRepository extends JpaRepository<IdeaEvaluation, Long> {
    
    // Find all evaluations for an idea (ordered by creation)
    @Query("SELECT e FROM IdeaEvaluation e WHERE e.idea.id = :ideaId ORDER BY e.createdAt ASC")
    List<IdeaEvaluation> findByIdeaIdOrderByCreatedAt(@Param("ideaId") Long ideaId);
    
    // Find evaluations by evaluator
    List<IdeaEvaluation> findByEvaluatorId(UUID evaluatorId);
}
```

### IdeaAttachmentRepository

```java
public interface IdeaAttachmentRepository extends JpaRepository<IdeaAttachment, Long> {
    
    // Find attachment for an idea
    Optional<IdeaAttachment> findByIdeaId(Long ideaId);
}
```

---

## Data Consistency Guarantees

| Scenario | Guarantee | Mechanism |
|----------|-----------|-----------|
| Idea deleted → attachment removed | Always | ON DELETE CASCADE at database |
| Idea deleted → evaluations removed | Always | ON DELETE CASCADE at database |
| User deleted → ideas/evaluations remain | Yes (enforced) | ON DELETE RESTRICT prevents deletion |
| Concurrent status update | Detected | Optimistic locking via version field |
| Evaluation comment deleted | Prevented | No delete method; immutable design |
| File orphaned | Prevented | Service layer links attachment to idea in transaction |
| Duplicate attachment per idea | Prevented | UNIQUE (idea_id) constraint |

---

## Migration Strategy (Flyway)

Three migration files created in sequence:

**V4__create_ideas_table.sql**:
- Creates `ideas` table with all columns and indexes
- Sets up submitter foreign key

**V5__create_idea_evaluations_table.sql**:
- Creates `idea_evaluations` table
- Establishes cascade delete relationship to ideas

**V6__create_idea_attachments_table.sql**:
- Creates `idea_attachments` table
- Adds unique constraint on idea_id

**Rollback Strategy**: 
- Flyway supports automatic rollback for failed migrations
- Manual rollback: Drop tables in reverse order (V6, V5, V4)
- No data loss for existing users/auth tables (not modified)

---

## DTO Projection for API Responses

### IdeaDto (for listing)

```java
public record IdeaDto(
    Long id,
    String title,
    String category,
    String status,
    String submitterName,
    LocalDateTime createdAt,
    boolean hasAttachment,
    long evaluationCount
) {}
```

### IdeaDetailDto (for detail view)

```java
public record IdeaDetailDto(
    Long id,
    String title,
    String description,
    String category,
    String status,
    String submitterName,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    IdeaAttachmentDto attachment,
    List<IdeaEvaluationDto> evaluations
) {}

public record IdeaAttachmentDto(
    Long id,
    String originalFilename,
    Long fileSize,
    LocalDateTime createdAt
) {}

public record IdeaEvaluationDto(
    Long id,
    String evaluatorName,
    String comment,
    String statusSnapshot,
    LocalDateTime createdAt
) {}
```

**Rationale**: DTOs decouple API contracts from internal JPA entities; shield sensitive fields (storedFilename, evaluatorId) from clients; allow lightweight responses (e.g., listing without full description)
