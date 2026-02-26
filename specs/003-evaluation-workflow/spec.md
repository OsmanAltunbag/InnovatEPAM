# Feature Specification: Evaluation Workflow

**Feature Branch**: `003-evaluation-workflow`  
**Created**: February 26, 2026  
**Status**: Draft  
**Input**: User description: "Implement the evaluation workflow for the InnovatEPAM Portal MVP. Users with 'evaluator' or 'admin' roles can view submitted idea details, update idea status through workflow transitions (SUBMITTED → UNDER_REVIEW → ACCEPTED or REJECTED), and must provide text comments when accepting or rejecting ideas. Submitters can view the current status and evaluation comments on their own ideas but cannot evaluate them."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Evaluator Views Submitted Ideas for Review (Priority: P1)

An evaluator needs to review ideas submitted by team members. They access the ideas listing page, browse submitted ideas, and open individual idea details to read the full proposal including attachments.

**Why this priority**: This is foundational for the evaluation workflow. Without the ability to view idea details, evaluators cannot perform their core function of assessing and providing feedback on ideas.

**Independent Test**: Can be tested by logging in as an evaluator, navigating to the ideas list, and verifying they can view all submitted ideas with full details including title, description, category, attachments, and submitter information.

**Acceptance Scenarios**:

1. **Given** a user with 'evaluator' role is logged in, **When** they navigate to the ideas listing page, **Then** they see all submitted ideas with basic information (title, submitter, status, submission date)
2. **Given** an evaluator is viewing the ideas list, **When** they click on an idea, **Then** they are taken to the idea detail page showing full information (title, description, category, attached files, current status, submitter name, submission date)
3. **Given** an evaluator is viewing an idea with an attachment, **When** they look at the attachment section, **Then** they can download or view the attached file
4. **Given** an evaluator is on the idea detail page, **When** they check for evaluation history, **Then** they see any previous evaluation comments and status changes (if any exist)
5. **Given** a user with 'admin' role is logged in, **When** they access the ideas listing, **Then** they have the same viewing capabilities as evaluators

---

### User Story 2 - Evaluator Updates Idea Status Through Workflow (Priority: P1)

An evaluator reviews an idea and needs to move it through the evaluation pipeline. They can transition the idea status from SUBMITTED to UNDER_REVIEW, and then to either ACCEPTED or REJECTED, following the defined workflow rules.

**Why this priority**: This is the core evaluation mechanism that enables ideas to progress through the innovation pipeline. Status transitions provide visibility into where each idea stands in the evaluation process.

**Independent Test**: Can be tested by logging in as an evaluator, selecting an idea with SUBMITTED status, changing it to UNDER_REVIEW, and verifying the status is updated. Then test transitions to ACCEPTED or REJECTED.

**Acceptance Scenarios**:

1. **Given** an evaluator is viewing an idea with status "SUBMITTED", **When** they access status controls, **Then** they see options to change status to "UNDER_REVIEW", "ACCEPTED", or "REJECTED"
2. **Given** an evaluator selects "UNDER_REVIEW" status for a submitted idea, **When** they save the change, **Then** the idea's status is updated to "UNDER_REVIEW" and the change is persisted
3. **Given** an evaluator is viewing an idea with status "UNDER_REVIEW", **When** they access status controls, **Then** they can transition to "ACCEPTED" or "REJECTED"
4. **Given** an evaluator attempts to change status from "ACCEPTED" back to "UNDER_REVIEW", **When** they try to save, **Then** the system should prevent backward status transitions (optional enforcement based on business rules)
5. **Given** an evaluator changes an idea's status, **When** the change is saved, **Then** the status update is logged with timestamp and evaluator identity

---

### User Story 3 - Evaluator Provides Feedback via Comments (Priority: P1)

An evaluator needs to document their assessment and provide feedback to the submitter. They must enter a text comment when accepting or rejecting an idea, and can optionally add comments at other stages.

**Why this priority**: Comments are essential for closing the feedback loop. They provide transparency, document decision rationale, and help submitters understand the evaluation outcome and improve future submissions.

**Independent Test**: Can be tested by attempting to accept or reject an idea without a comment (should fail), then providing a comment and successfully completing the status change.

**Acceptance Scenarios**:

1. **Given** an evaluator is changing an idea's status to "ACCEPTED", **When** they attempt to save without entering a comment, **Then** the system displays a validation error requiring a comment
2. **Given** an evaluator is changing an idea's status to "REJECTED", **When** they attempt to save without entering a comment, **Then** the system displays a validation error requiring a comment
3. **Given** an evaluator is providing feedback on an accepted idea, **When** they enter a comment like "Excellent innovation with clear ROI. Approved for Phase 2.", **Then** the comment is saved and associated with the status change
4. **Given** an evaluator is rejecting an idea, **When** they enter a detailed rejection reason, **Then** the comment is saved and will be visible to the submitter
5. **Given** an evaluator is changing status to "UNDER_REVIEW", **When** they provide an optional comment, **Then** the comment is saved but not required for this transition
6. **Given** multiple evaluators add comments to an idea, **When** viewing the evaluation history, **Then** all comments are displayed chronologically with evaluator names and timestamps

---

### User Story 4 - Submitter Views Evaluation Status and Feedback (Priority: P1)

A submitter wants to track the progress of their submitted idea and understand the evaluation outcome. They can view the current status of their ideas and read evaluation comments left by evaluators.

**Why this priority**: This completes the feedback loop and provides transparency to submitters. Without this visibility, submitters would be left in the dark about their ideas' progress, reducing engagement and trust in the system.

**Independent Test**: Can be tested by submitting an idea, having an evaluator add comments and change status, then logging in as the submitter and verifying they can see the updated status and comments.

**Acceptance Scenarios**:

1. **Given** a submitter is viewing their submitted idea, **When** an evaluator has changed the status, **Then** the submitter sees the updated status clearly displayed
2. **Given** a submitter is on their idea detail page, **When** an evaluator has left comments, **Then** the submitter can read all evaluation comments with evaluator names and timestamps
3. **Given** a submitter's idea has been rejected with a comment, **When** they view the idea, **Then** they see the "REJECTED" status and the evaluator's explanation
4. **Given** a submitter's idea has been accepted, **When** they view it, **Then** they see the "ACCEPTED" status and positive feedback from the evaluator
5. **Given** a submitter is viewing their idea, **When** they look for evaluation controls, **Then** they do NOT see options to change status or evaluate (read-only view)

---

### User Story 5 - Role-Based Access Control for Evaluation (Priority: P1)

The system enforces role-based permissions ensuring only evaluators and admins can perform evaluation actions, while submitters can only view their own ideas' status and feedback.

**Why this priority**: Security and data integrity are critical. Preventing unauthorized users from evaluating ideas ensures the evaluation process maintains credibility and prevents manipulation.

**Independent Test**: Can be tested by attempting to access evaluation endpoints as different user roles and verifying appropriate authorization checks.

**Acceptance Scenarios**:

1. **Given** a user with 'submitter' role only, **When** they attempt to change an idea's status via the UI, **Then** they see no evaluation controls on the page
2. **Given** a user with 'submitter' role attempts to call the status update API directly, **When** the request reaches the backend, **Then** the system returns a 403 Forbidden error
3. **Given** a user with 'evaluator' role is logged in, **When** they access an idea detail page, **Then** they see evaluation controls (status selector, comment field)
4. **Given** a user with 'admin' role is logged in, **When** they access any idea, **Then** they have full evaluation capabilities
5. **Given** an unauthenticated user attempts to access idea details, **When** they make the request, **Then** they are redirected to login or receive a 401 Unauthorized error

---

### Edge Cases

- What happens when an evaluator tries to evaluate an idea they themselves submitted?
- What happens if two evaluators try to change the same idea's status simultaneously?
- How does the system handle when an evaluator's account is deleted but their comments are still referenced?
- What happens if a submitter's role is upgraded to evaluator - can they then evaluate their own previously submitted ideas?
- What happens if status transition validation fails due to invalid workflow state?
- How are evaluation comments with special characters or very long text handled?
- What happens when an evaluator starts writing a comment but their session expires before submitting?
- Can an evaluator edit or delete their own evaluation comments after submission?
- What happens when an idea is deleted while an evaluator is writing a comment for it?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST authenticate all users via existing JWT token system before allowing any evaluation actions
- **FR-002**: System MUST restrict evaluation functionality to users with 'evaluator' or 'admin' roles
- **FR-003**: System MUST allow evaluators to view complete details of all submitted ideas
- **FR-004**: System MUST display idea details including title, description, category, status, submitter name, submission date, and attached files
- **FR-005**: System MUST support status transitions following the workflow: SUBMITTED → UNDER_REVIEW → (ACCEPTED | REJECTED)
- **FR-006**: System MUST require a text comment when transitioning an idea to "ACCEPTED" status
- **FR-007**: System MUST require a text comment when transitioning an idea to "REJECTED" status
- **FR-008**: System MUST allow optional comments when transitioning to "UNDER_REVIEW" status
- **FR-009**: System MUST persist all status changes with metadata (evaluator ID, timestamp, new status, comment)
- **FR-010**: System MUST display evaluation history chronologically showing all status changes and comments
- **FR-011**: System MUST prevent users with 'submitter' role from accessing evaluation controls
- **FR-012**: System MUST allow submitters to view the current status of their own ideas
- **FR-013**: System MUST allow submitters to read evaluation comments on their own ideas
- **FR-014**: System MUST prevent submitters from modifying evaluation data (status or comments)
- **FR-015**: System MUST log each evaluation action with evaluator identity, timestamp, and action details
- **FR-016**: System MUST validate that idea exists before allowing status update
- **FR-017**: System MUST return appropriate error messages for invalid status transitions
- **FR-018**: System MUST handle concurrent evaluation attempts with proper conflict resolution

### Non-Functional Requirements

- **NFR-001**: Status change operations MUST complete within 2 seconds under normal load
- **NFR-002**: Evaluation comment text MUST support up to 5000 characters
- **NFR-003**: System MUST maintain audit trail of all evaluation actions for compliance
- **NFR-004**: UI MUST clearly distinguish between different status states with visual indicators (colors, icons)
- **NFR-005**: System MUST handle at least 50 concurrent evaluators without performance degradation
- **NFR-006**: Evaluation history MUST be immutable (no deletion or modification of historical records)

### Key Entities

- **IdeaEvaluation**: Represents an evaluation action on an idea. Contains:
  - `id` (Long): Primary key, auto-generated
  - `ideaId` (Long): Reference to the idea being evaluated
  - `evaluatorId` (UUID): Reference to user performing evaluation
  - `comment` (String): Evaluation feedback text (required for ACCEPTED/REJECTED, optional otherwise)
  - `statusSnapshot` (Enum): The status set during this evaluation (UNDER_REVIEW, ACCEPTED, REJECTED)
  - `createdAt` (Timestamp): When this evaluation was recorded

- **Idea** (extended): Existing entity with relevant fields:
  - `id` (Long): Unique identifier
  - `status` (Enum): Current workflow status (SUBMITTED, UNDER_REVIEW, ACCEPTED, REJECTED)
  - `submitterId` (UUID): Reference to user who submitted the idea
  - `evaluations` (List<IdeaEvaluation>): Collection of all evaluation records

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Evaluators can view any idea's full details (including attachments) in under 3 seconds
- **SC-002**: Evaluators can complete a status change with comment in under 1 minute
- **SC-003**: 100% of status transitions must be validated against workflow rules (no invalid transitions permitted)
- **SC-004**: 100% of ACCEPTED and REJECTED status changes must have associated comments (validation enforcement)
- **SC-005**: Submitters can view their ideas' evaluation status and comments in real-time without refresh delays
- **SC-006**: Zero unauthorized access to evaluation functions (100% role-based access control enforcement)
- **SC-007**: All evaluation actions are logged with complete audit trail (evaluator, timestamp, action)
- **SC-008**: Evaluation comment submission has 99%+ success rate for valid inputs

## Assumptions

- **Authentication System**: The existing JWT authentication system is fully functional with proper role assignment
- **Idea Management**: Ideas exist in the system (submitted via the Idea Management feature) before evaluation can occur
- **User Roles**: Users are properly assigned 'submitter', 'evaluator', or 'admin' roles via user management system
- **Database Transactions**: The database supports transactions to ensure data consistency during status updates
- **Concurrency**: Basic optimistic locking or transaction isolation prevents most race conditions
- **File Storage**: Attached files are accessible to evaluators (file storage from Idea Management feature is operational)
- **Time Zones**: All timestamps are stored in UTC and displayed in user's local timezone
- **Comment Length**: 5000 character limit is sufficient for evaluation feedback in MVP

## Dependencies

- **Depends On**: 
  - User Authentication System (JWT with role management) - REQUIRED
  - Idea Management System (ideas must exist to be evaluated) - REQUIRED
- **Related To**: 
  - Future notification system (could notify submitters when status changes)
  - Future reporting system (evaluation metrics and analytics)
- **Blocked By**: None identified

## Out of Scope

- Email or push notifications when status changes
- Multi-evaluator approval workflows (requiring consensus from multiple evaluators)
- Scoring or rating systems (1-5 stars, etc.)
- Evaluation assignment (routing ideas to specific evaluators)
- Comment threading or replies
- Comment editing or deletion after submission
- File attachments in evaluation comments
- Batch evaluation operations (evaluate multiple ideas at once)
- Advanced filtering and search in evaluation dashboard
- Evaluation templates or pre-defined comment options
- Anonymous evaluation (blind review)
- Evaluation deadline or SLA tracking
- Idea resubmission after rejection

## Technical Considerations

### Workflow State Machine

The status workflow follows these rules:

```
SUBMITTED
    ↓
    → UNDER_REVIEW
         ↓
         ├→ ACCEPTED (requires comment)
         └→ REJECTED (requires comment)
```

**Business Rules**:
- Initial status for all ideas: SUBMITTED
- SUBMITTED can transition to: UNDER_REVIEW, ACCEPTED, REJECTED
- UNDER_REVIEW can transition to: ACCEPTED, REJECTED
- ACCEPTED and REJECTED are terminal states (no further transitions in MVP)
- Comments are mandatory for ACCEPTED and REJECTED transitions
- Comments are optional for UNDER_REVIEW transitions

### API Design Considerations

- Use PATCH method for status updates (partial update)
- Include validation for workflow state transitions
- Return 400 Bad Request for invalid transitions
- Return 403 Forbidden for unauthorized role access
- Return 404 Not Found if idea doesn't exist
- Return detailed error messages for missing required comments

### Data Consistency

- Use database transactions for status updates with comment creation
- Implement optimistic locking (version field on idea) to handle concurrent updates
- Log all evaluation actions before committing transaction
- Ensure atomicity: status change and comment must both succeed or both fail

### Security

- Validate JWT token on all evaluation endpoints
- Check user roles server-side (never trust client-side role checks)
- Prevent submitters from evaluating their own ideas (business rule)
- Sanitize comment input to prevent XSS attacks
- Rate limit evaluation API to prevent abuse

## Testing Strategy

### Unit Tests

- Test workflow validation logic (valid and invalid transitions)
- Test comment requirement validation (ACCEPTED/REJECTED require comments)
- Test role-based authorization checks
- Test evaluation entity creation and persistence
- Test concurrent update handling (optimistic locking)

### Integration Tests

- Test end-to-end evaluation flow (login → view idea → change status → add comment → save)
- Test role-based access control across API endpoints
- Test evaluation history retrieval
- Test submitter view (read-only access to evaluation data)
- Test database transaction rollback on partial failure

### Manual Test Scenarios

- Evaluator logs in, views ideas, evaluates multiple ideas with different outcomes
- Submitter logs in, views their ideas, verifies they cannot evaluate
- Admin logs in, verifies full evaluation capabilities
- Attempt to evaluate without authentication (should fail)
- Attempt to skip required comments (should fail validation)

---

**Version**: 1.0  
**Last Updated**: February 26, 2026  
**Author**: InnovatEPAM Development Team
