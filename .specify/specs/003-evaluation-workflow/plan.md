# Implementation Plan: Evaluation Workflow

**Branch**: `003-evaluation-workflow` | **Date**: February 26, 2026 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/003-evaluation-workflow/spec.md`

## Summary

The Evaluation Workflow enables users with 'evaluator' or 'admin' roles to review submitted ideas, update idea status through defined workflow transitions (SUBMITTED → UNDER_REVIEW → ACCEPTED/REJECTED), and provide mandatory text comments when accepting or rejecting ideas. Submitters can view the current status and evaluation comments on their own ideas in read-only mode. This feature builds upon the existing Idea Management System (which provides the basic `status` field on ideas) and JWT authentication with role-based access control.

## Technical Context

**Language/Version**: Java 21, Spring Boot 3.2.2  
**Primary Dependencies**: Spring Data JPA, Spring Security, JJWT (JWT), PostgreSQL, Flyway; Frontend: React 18, Vite, Tailwind CSS, Axios  
**Storage**: PostgreSQL database - will ADD new `idea_evaluations` table  
**Testing**: JUnit 5, Mockito, Testcontainers (PostgreSQL), Spring Security Test; Frontend: Vitest, React Testing Library, MSW  
**Target Platform**: Web application (Spring Boot backend REST API + React frontend)  
**Project Type**: Monorepo with Java/Spring backend + React frontend  
**Performance Goals**: Status update operations complete within 2 seconds, evaluation history loads in under 3 seconds  
**Constraints**: Mandatory comments for ACCEPTED/REJECTED transitions, 5000 character limit on comments, 100% role-based access control enforcement  
**Scale/Scope**: Initial MVP supporting 50+ concurrent evaluators without performance degradation

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### Verification Against Constitution Principles

| Principle | Status | Justification |
|-----------|--------|---------------|
| Clean Code Principles | ✅ PASS | Small focused methods, descriptive naming, comments explain business rules (status transitions, comment requirements); Follows existing codebase patterns |
| SOLID Principles | ✅ PASS | Repository pattern for data access, Service layer encapsulates business logic, Controllers are thin adapters; @Transactional ensures atomicity; Dependency injection throughout |
| RESTful API Design | ✅ PASS | Resource-oriented endpoints (`PATCH /api/v1/ideas/{id}/status`, `POST /api/v1/ideas/{id}/comments`, `GET /api/v1/ideas/{id}/evaluations`); Proper HTTP methods (PATCH for partial update, POST for create, GET for read); Standard status codes (200, 201, 400, 403, 404) |
| Repository Pattern | ✅ PASS | IdeaEvaluationRepository abstraction; Services inject repositories; Controllers remain decoupled from persistence layer |
| Testing Pyramid | ✅ PASS | 80% coverage target via JaCoCo; Unit tests for validators and services; Integration tests with Testcontainers; Component tests for React UI |
| Security & Secrets | ✅ PASS | JWT validation on all endpoints; Role-based access via @PreAuthorize; Server-side validation (no trust in client); Sanitized comment input; Audit trail (immutable evaluations) |
| Version Control | ✅ PASS | Feature branch `003-evaluation-workflow`; Conventional commit format; Atomic changesets; Descriptive commit messages |

**Gate Status**: ✅ **PASSED** - All principles satisfied; design aligns with project constitution and existing patterns.

## Project Structure

### Documentation (this feature)

```text
specs/003-evaluation-workflow/
├── spec.md                    # Feature specification (requirements & user stories) ✅ COMPLETE
├── plan.md                    # This file (technical approach & implementation phases)
├── data-model.md              # Database schema and JPA entities ✅ COMPLETE
├── contracts/                 # API contract definitions
│   └── API.md                 # API endpoints and DTOs ✅ COMPLETE
├── quickstart.md              # Integration guide and usage examples (Phase 1 output)
└── tasks.md                   # Step-by-step execution roadmap (to be generated)
```

### Source Code (TO BE IMPLEMENTED)

```text
backend/
├── src/
│   ├── main/
│   │   ├── java/com/innovatepam/idea/
│   │   │   ├── model/
│   │   │   │   ├── Idea.java                      ✅ EXISTS (has basic status field)
│   │   │   │   ├── IdeaStatus.java                ✅ EXISTS (enum: SUBMITTED, UNDER_REVIEW, ACCEPTED, REJECTED)
│   │   │   │   └── IdeaEvaluation.java            ⚠️ TO CREATE - new entity
│   │   │   ├── repository/
│   │   │   │   ├── IdeaRepository.java            ✅ EXISTS
│   │   │   │   └── IdeaEvaluationRepository.java  ⚠️ TO CREATE - new repository
│   │   │   ├── service/
│   │   │   │   ├── IdeaService.java               ⚠️ TO ENHANCE - add updateStatus method
│   │   │   │   └── IdeaEvaluationService.java     ⚠️ TO CREATE - new service
│   │   │   ├── controller/
│   │   │   │   ├── IdeaController.java            ✅ EXISTS
│   │   │   │   └── IdeaEvaluationController.java  ⚠️ TO CREATE - new controller
│   │   │   ├── dto/
│   │   │   │   ├── IdeaResponse.java              ⚠️ TO ENHANCE - add evaluationCount
│   │   │   │   ├── UpdateIdeaStatusRequest.java   ⚠️ TO CREATE - new DTO
│   │   │   │   ├── AddCommentRequest.java         ⚠️ TO CREATE - new DTO
│   │   │   │   ├── IdeaEvaluationResponse.java    ⚠️ TO CREATE - new DTO
│   │   │   │   └── EvaluationHistoryResponse.java ⚠️ TO CREATE - new DTO
│   │   │   ├── exception/
│   │   │   │   ├── IdeaNotFoundException.java     ✅ EXISTS
│   │   │   │   ├── UnauthorizedAccessException.java ✅ EXISTS
│   │   │   │   └── InvalidStatusTransitionException.java ⚠️ TO CREATE - new exception
│   │   │   └── util/
│   │   │       └── IdeaStatusValidator.java       ⚠️ TO CREATE - workflow validation logic
│   │   └── resources/
│   │       └── db/migration/
│   │           └── V7__create_idea_evaluations_table.sql ⚠️ TO CREATE - Flyway migration
│   │
│   └── test/
│       └── java/com/innovatepam/idea/
│           ├── model/
│           │   └── IdeaEvaluationTest.java        ⚠️ TO CREATE - entity tests
│           ├── service/
│           │   ├── IdeaEvaluationServiceTest.java ⚠️ TO CREATE - unit tests
│           │   └── IdeaEvaluationServiceIntegrationTest.java ⚠️ TO CREATE - integration tests
│           ├── controller/
│           │   └── IdeaEvaluationControllerIntegrationTest.java ⚠️ TO CREATE - API tests
│           ├── repository/
│           │   └── IdeaEvaluationRepositoryTest.java ⚠️ TO CREATE - repository tests
│           └── util/
│               └── IdeaStatusValidatorTest.java   ⚠️ TO CREATE - validation tests

frontend/
├── src/
│   ├── components/
│   │   └── EvaluationPanel.jsx            ⚠️ TO CREATE - status update and comment UI
│   ├── pages/
│   │   ├── IdeaDetail.jsx                 ⚠️ TO ENHANCE - integrate EvaluationPanel
│   │   └── IdeaListing.jsx                ✅ EXISTS (already shows status)
│   ├── services/
│   │   └── ideaService.js                 ⚠️ TO ENHANCE - add evaluation endpoints
│   ├── hooks/
│   │   └── useIdeas.js                    ⚠️ TO ENHANCE - add evaluation state management
│   ├── utils/
│   │   └── statusUtils.js                 ⚠️ TO CREATE - status transition logic, colors, icons
│   └── test/
│       ├── EvaluationPanel.test.jsx       ⚠️ TO CREATE - component tests
│       ├── ideaService.test.js            ⚠️ TO ENHANCE - add evaluation endpoint tests
│       └── mocks/
│           └── evaluationHandlers.js      ⚠️ TO CREATE - MSW handlers
```

**Current State**: 
- ✅ Basic `ideas` table exists with `status` field (from Feature 002)
- ✅ User authentication and role management exists (from Feature 001)
- ✅ Frontend idea listing shows status (from Feature 002)
- ⚠️ **NEW IN THIS FEATURE**: IdeaEvaluation entity, evaluation APIs, evaluation UI components

## Implementation Phases

### Phase 0: Architecture Documentation ✅ COMPLETE

**Duration**: ~0.5 days | **Status**: COMPLETE

#### Deliverables ✅

- [x] `data-model.md` - IdeaEvaluation entity design, database schema
- [x] `contracts/API.md` - API endpoint specifications, request/response formats
- [ ] `quickstart.md` - Usage guide with integration examples (TO DO in Phase 1)

---

### Phase 1: Backend Database & Core Entities

**Duration**: ~1.5 days | **Deliverables**: Database migration, JPA entities, repositories

#### 1.1 Database Migration

**Create Flyway migration**: `V7__create_idea_evaluations_table.sql`

**Tasks**:
- [ ] Create `idea_evaluations` table with all fields (id, idea_id, evaluator_id, comment, status_snapshot, created_at)
- [ ] Add foreign key constraints to `ideas` and `users` tables
- [ ] Add CHECK constraint for valid status_snapshot values
- [ ] Create indexes: idx_idea_id, idx_evaluator_id, idx_created_at, idx_idea_created_composite
- [ ] Add table comment for documentation
- [ ] Test migration with `mvn flyway:migrate`
- [ ] Verify rollback script works

**Success Criteria**:
- Migration runs successfully on clean database
- Referential integrity enforced (cascades work correctly)
- Indexes created and queryable

#### 1.2 JPA Entity Creation

**Create `IdeaEvaluation` entity**:

**Tasks**:
- [ ] Create class with @Entity, @Table annotations
- [ ] Define all fields with proper column mappings
- [ ] Add @ManyToOne relationship to Idea (FetchType.LAZY)
- [ ] Add @ManyToOne relationship to User/evaluator (FetchType.EAGER)
- [ ] Add @NotBlank, @Size validation annotations on comment
- [ ] Add @PrePersist hook to set createdAt timestamp
- [ ] Make createdAt field immutable (updatable = false)
- [ ] Add getters/setters (no setCreatedAt)
- [ ] Add equals/hashCode based on id

**Extend `Idea` entity** (optional bi-directional relationship):
- [ ] Add @OneToMany relationship to evaluations
- [ ] Add @OrderBy("createdAt ASC") for chronological order
- [ ] Add helper method `addEvaluation()` to maintain relationship

**Testing**:
- [ ] Create `IdeaEvaluationTest` for entity validation
- [ ] Test @PrePersist sets timestamp
- [ ] Test validation annotations trigger errors

#### 1.3 Repository Layer

**Create `IdeaEvaluationRepository`**:

**Tasks**:
- [ ] Create interface extending `JpaRepository<IdeaEvaluation, Long>`
- [ ] Add query method: `List<IdeaEvaluation> findByIdeaIdOrderByCreatedAtAsc(Long ideaId)`
- [ ] Add query method: `List<IdeaEvaluation> findByEvaluatorId(UUID evaluatorId)` (for future analytics)
- [ ] Add query method: `long countByIdeaId(Long ideaId)` (for evaluation count)

**Testing**:
- [ ] Create `IdeaEvaluationRepositoryTest` with Testcontainers
- [ ] Test findByIdeaIdOrderByCreatedAtAsc returns correct order
- [ ] Test cascade delete (deleting idea removes evaluations)
- [ ] Test count query accuracy

**Success Criteria**:
- All repository methods tested and working
- Query performance acceptable (< 100ms for typical queries)

---

### Phase 2: Backend Service Layer & Business Logic

**Duration**: ~2 days | **Deliverables**: Services with business logic, validation, transactions

#### 2.1 Status Workflow Validation

**Create `IdeaStatusValidator` utility class**:

**Tasks**:
- [ ] Create static method `boolean isValidTransition(IdeaStatus from, IdeaStatus to)`
- [ ] Implement workflow rules (SUBMITTED → UNDER_REVIEW/ACCEPTED/REJECTED, etc.)
- [ ] Create static method `boolean isCommentRequired(IdeaStatus status)` (true for ACCEPTED/REJECTED)
- [ ] Add descriptive error messages for invalid transitions

**Testing**:
- [ ] Create `IdeaStatusValidatorTest`
- [ ] Test all valid transitions return true
- [ ] Test all invalid transitions return false
- [ ] Test comment requirement logic
- [ ] Test edge cases (null statuses, same status transition)

#### 2.2 Evaluation Service

**Create `IdeaEvaluationService`**:

**Tasks**:
- [ ] Inject IdeaRepository and IdeaEvaluationRepository
- [ ] Implement `addComment(Long ideaId, User evaluator, String comment)`:
  - Validate idea exists
  - Create IdeaEvaluation with comment only (no status change)
  - Save and return evaluation
- [ ] Implement `addStatusEvaluation(Idea idea, User evaluator, String comment, IdeaStatus newStatus)`:
  - Create IdeaEvaluation with comment and status snapshot
  - Internal method called by IdeaService
  - Save and return evaluation
- [ ] Implement `getEvaluationHistory(Long ideaId)`:
  - Validate idea exists
  - Return all evaluations ordered chronologically
- [ ] Add @Transactional annotations on write methods
- [ ] Add proper exception handling (IdeaNotFoundException)

**Enhance `IdeaService`**:

**Tasks**:
- [ ] Inject IdeaEvaluationService
- [ ] Inject IdeaStatusValidator
- [ ] Implement `updateStatus(Long ideaId, IdeaStatus newStatus, User evaluator, String comment)`:
  - Validate idea exists
  - Validate user has EVALUATOR/ADMIN role
  - Validate status transition with IdeaStatusValidator
  - Validate comment provided if required
  - Update idea status
  - Call IdeaEvaluationService.addStatusEvaluation()
  - All in single transaction
  - Return updated idea
- [ ] Add proper error handling and validation

**Exception Handling**:
- [ ] Create `InvalidStatusTransitionException` extending RuntimeException
- [ ] Update global exception handler to map to 400 Bad Request

**Testing**:
- [ ] Create `IdeaEvaluationServiceTest` (unit tests with mocks)
  - Test addComment success
  - Test addComment with non-existent idea (throws exception)
  - Test getEvaluationHistory returns correct order
- [ ] Create `IdeaServiceTest` (unit tests for updateStatus)
  - Test valid status transitions
  - Test invalid transitions throw exception
  - Test missing comment for ACCEPTED/REJECTED throws exception
  - Test successful status update calls evaluation service
- [ ] Create `IdeaEvaluationServiceIntegrationTest` (with Testcontainers)
  - Test end-to-end: Create idea → Add evaluations → Verify persistence
  - Test transactional rollback on failure
- [ ] Create `IdeaServiceIntegrationTest` (enhance existing)
  - Test updateStatus with database persistence
  - Test transactional atomicity (status + evaluation both succeed or fail)

**Success Criteria**:
- All service methods tested (80%+ coverage)
- Transactions work correctly (rollback on failure)
- Workflow validation enforced

---

### Phase 3: Backend REST API Controllers

**Duration**: ~1.5 days | **Deliverables**: REST endpoints, DTOs, API integration tests

#### 3.1 Data Transfer Objects (DTOs)

**Create Request DTOs**:

**Tasks**:
- [ ] `UpdateIdeaStatusRequest` record:
  - Field: `IdeaStatus newStatus` (@NotNull)
  - Field: `String comment` (validated in service layer)
- [ ] `AddCommentRequest` record:
  - Field: `String comment` (@NotBlank, @Size(max=5000))

**Create Response DTOs**:

**Tasks**:
- [ ] `IdeaEvaluationResponse` record:
  - Fields: id, ideaId, evaluatorName, evaluatorId, comment, statusSnapshot, createdAt
  - Static factory method: `from(IdeaEvaluation evaluation)`
- [ ] `EvaluationHistoryResponse` record:
  - Fields: ideaId, List<IdeaEvaluationResponse> evaluations

**Enhance existing DTOs**:
- [ ] `IdeaResponse` - add `int evaluationCount` field
- [ ] Update `from(Idea)` method to include evaluation count

#### 3.2 Evaluation Controller

**Create `IdeaEvaluationController`**:

**Tasks**:
- [ ] Add @RestController and @RequestMapping("/api/v1/ideas")
- [ ] Inject IdeaService, IdeaEvaluationService, UserRepository
- [ ] Implement `PATCH /{id}/status`:
  - @PreAuthorize("hasAnyRole('EVALUATOR', 'ADMIN')")
  - Extract current user from Authentication
  - Call ideaService.updateStatus()
  - Return 200 OK with IdeaResponse
  - Handle exceptions (400, 403, 404)
- [ ] Implement `POST /{id}/comments`:
  - @PreAuthorize("hasAnyRole('EVALUATOR', 'ADMIN')")
  - Extract current user from Authentication
  - Call evaluationService.addComment()
  - Return 201 Created with IdeaEvaluationResponse
  - Handle exceptions
- [ ] Implement `GET /{id}/evaluations`:
  - @PreAuthorize("isAuthenticated()")
  - Call evaluationService.getEvaluationHistory()
  - Return 200 OK with EvaluationHistoryResponse
  - Handle exceptions

**Exception Handling**:
- [ ] Ensure global exception handler covers all new exceptions
- [ ] Return proper HTTP status codes with descriptive messages

**Testing**:
- [ ] Create `IdeaEvaluationControllerIntegrationTest` (with MockMvc + Testcontainers)
  - Test PATCH /ideas/{id}/status:
    - Success: Valid transition with comment returns 200
    - Validation: Missing comment for REJECTED returns 400
    - Validation: Invalid transition returns 400
    - Authorization: Submitter role returns 403
    - Authorization: Evaluator role returns 200
    - Not found: Non-existent idea returns 404
  - Test POST /ideas/{id}/comments:
    - Success: Valid comment returns 201
    - Validation: Empty comment returns 400
    - Authorization: Submitter role returns 403
    - Authorization: Evaluator role returns 201
  - Test GET /ideas/{id}/evaluations:
    - Success: Returns evaluation history in chronological order
    - Success: Empty history returns empty array
    - Not found: Non-existent idea returns 404
    - Authorization: Any authenticated user can access

**Success Criteria**:
- All endpoints return correct status codes
- Authorization enforced (@PreAuthorize works)
- Validation errors return 400 with descriptive messages
- Integration tests cover happy path and error scenarios

---

### Phase 4: Backend Testing & Quality

**Duration**: ~1 day | **Deliverables**: Comprehensive test coverage, JaCoCo report

#### 4.1 Test Coverage Audit

**Tasks**:
- [ ] Run JaCoCo coverage report: `mvn clean test jacoco:report`
- [ ] Verify 80%+ line coverage on evaluation-related classes:
  - IdeaEvaluation entity
  - IdeaEvaluationRepository
  - IdeaEvaluationService
  - IdeaService (updateStatus method)
  - IdeaEvaluationController
  - IdeaStatusValidator
- [ ] Add missing tests to reach coverage target
- [ ] Document any uncovered edge cases

#### 4.2 Integration Testing

**Tasks**:
- [ ] End-to-end evaluation workflow test:
  - Create idea as submitter
  - Login as evaluator
  - Update status to UNDER_REVIEW (optional comment)
  - Add standalone comment
  - Update status to ACCEPTED (required comment)
  - Login as submitter, verify can view evaluations
- [ ] Concurrent update test (if optimistic locking implemented)
- [ ] Performance test: Verify status update < 500ms

#### 4.3 Documentation

**Create `quickstart.md`**:

**Tasks**:
- [ ] Overview of evaluation workflow
- [ ] Step-by-step guide: How evaluators use the API
- [ ] Step-by-step guide: How submitters view evaluations
- [ ] Example curl commands for all endpoints
- [ ] Common error scenarios and troubleshooting
- [ ] Integration with frontend (component overview)

**Success Criteria**:
- Quickstart guide is clear and executable
- All curl examples work
- Coverage reports show 80%+ coverage

---

### Phase 5: Frontend Evaluation UI Components

**Duration**: ~2 days | **Deliverables**: React components, status update form, evaluation timeline

#### 5.1 Status Utilities

**Create `utils/statusUtils.js`**:

**Tasks**:
- [ ] Function `getAllowedNextStatuses(currentStatus)` - returns valid transitions
- [ ] Function `getStatusLabel(status)` - returns display label
- [ ] Function `getStatusColor(status)` - returns Tailwind color classes
- [ ] Function `getStatusIcon(status)` - returns icon name/component
- [ ] Function `isCommentRequired(status)` - returns boolean
- [ ] Export constants for status values

**Testing**:
- [ ] Unit tests for all utility functions
- [ ] Test edge cases (null status, invalid status)

#### 5.2 Evaluation Panel Component

**Create `components/EvaluationPanel.jsx`**:

**Tasks**:
- [ ] Component accepts `idea` as prop
- [ ] Hook into `useAuth()` to get current user and role
- [ ] Hook into `useIdeas()` for API calls
- [ ] Fetch evaluation history on mount
- [ ] Conditional rendering:
  - If user is evaluator/admin: Show status update form + comment form
  - If user is submitter: Show evaluation history read-only
- [ ] Status update form:
  - Dropdown with allowed next statuses (from statusUtils)
  - Textarea for comment
  - Validation: Show error if comment required but missing
  - Submit button with loading state
  - Success/error feedback
- [ ] Standalone comment form:
  - Textarea for comment
  - Submit button
  - Character counter (X / 5000)
- [ ] Evaluation history timeline:
  - List evaluations chronologically (oldest first)
  - Show evaluator name, timestamp, comment, status change
  - Visual indicators for status transitions (icons, colors)
  - Empty state: "No evaluations yet"
- [ ] Error handling: Display API errors to user

**Styling**:
- [ ] Use Tailwind CSS consistent with existing components
- [ ] Responsive design (mobile-friendly)
- [ ] Accessible (ARIA labels, keyboard navigation)

#### 5.3 Integrate into Idea Detail Page

**Enhance `pages/IdeaDetail.jsx`**:

**Tasks**:
- [ ] Import EvaluationPanel component
- [ ] Add EvaluationPanel to page layout (below idea description)
- [ ] Pass `idea` prop to EvaluationPanel
- [ ] Refresh idea data after status change (to update displayed status)

**Testing**:
- [ ] Manual test: View idea detail as evaluator, see evaluation controls
- [ ] Manual test: View idea detail as submitter, see read-only history
- [ ] Manual test: Update status, verify UI updates

#### 5.4 API Service Integration

**Enhance `services/ideaService.js`**:

**Tasks**:
- [ ] Add function `updateIdeaStatus(ideaId, { newStatus, comment })` - PATCH /ideas/{id}/status
- [ ] Add function `addEvaluationComment(ideaId, comment)` - POST /ideas/{id}/comments
- [ ] Add function `getEvaluationHistory(ideaId)` - GET /ideas/{id}/evaluations
- [ ] Ensure JWT token included in Authorization header
- [ ] Error handling: Catch and format API errors

**Enhance `hooks/useIdeas.js`**:

**Tasks**:
- [ ] Add state for `evaluations` array
- [ ] Add `updateStatus` function calling ideaService
- [ ] Add `addComment` function calling ideaService
- [ ] Add `fetchEvaluations` function calling ideaService
- [ ] Add loading/error states for evaluation operations

**Success Criteria**:
- API service methods successfully call backend endpoints
- JWT tokens automatically included
- Error responses handled gracefully

---

### Phase 6: Frontend Testing

**Duration**: ~1.5 days | **Deliverables**: Component tests, API mock tests, coverage report

#### 6.1 Component Tests

**Create `test/EvaluationPanel.test.jsx`**:

**Tasks**:
- [ ] Test: Renders evaluation history for submitter (no controls)
- [ ] Test: Renders status update form for evaluator
- [ ] Test: Renders comment form for evaluator
- [ ] Test: Status dropdown shows only valid transitions
- [ ] Test: Submitting status update calls API
- [ ] Test: Validation error shown when comment required but missing
- [ ] Test: Loading state shown during API call
- [ ] Test: Success message after status update
- [ ] Test: Error message on API failure
- [ ] Test: Evaluation timeline displays chronologically
- [ ] Test: Empty state when no evaluations

**Enhance `test/IdeaDetail.test.jsx`**:

**Tasks**:
- [ ] Test: EvaluationPanel renders on page
- [ ] Test: Role-based rendering (evaluator vs submitter)

#### 6.2 API Service Tests

**Enhance `test/ideaService.test.js`**:

**Tasks**:
- [ ] Mock `PATCH /api/v1/ideas/{id}/status` endpoint
- [ ] Test updateIdeaStatus success returns 200
- [ ] Test updateIdeaStatus validation error returns 400
- [ ] Mock `POST /api/v1/ideas/{id}/comments` endpoint
- [ ] Test addEvaluationComment success returns 201
- [ ] Mock `GET /api/v1/ideas/{id}/evaluations` endpoint
- [ ] Test getEvaluationHistory returns array

**Create `test/mocks/evaluationHandlers.js`**:

**Tasks**:
- [ ] MSW handler for PATCH /ideas/:id/status
- [ ] MSW handler for POST /ideas/:id/comments
- [ ] MSW handler for GET /ideas/:id/evaluations
- [ ] Mock validation errors (missing comment, invalid transition)
- [ ] Mock authorization errors (403 Forbidden)

#### 6.3 Coverage Report

**Tasks**:
- [ ] Run `npm run test:coverage`
- [ ] Verify 80%+ coverage on evaluation-related files:
  - EvaluationPanel.jsx
  - statusUtils.js
  - ideaService.js (evaluation methods)
  - useIdeas.js (evaluation methods)
- [ ] Add missing tests to reach target

**Success Criteria**:
- All component tests pass
- API mocks work correctly
- Coverage target met (80%+)

---

### Phase 7: UI/UX Polish & Documentation

**Duration**: ~1 day | **Deliverables**: Polished UI, accessibility improvements, final documentation

#### 7.1 UI Enhancements

**Tasks**:
- [ ] Status badges with consistent colors:
  - SUBMITTED: Blue/gray
  - UNDER_REVIEW: Yellow/orange
  - ACCEPTED: Green
  - REJECTED: Red
- [ ] Status icons (checkmark, X, clock, in-progress)
- [ ] Evaluation timeline visual improvements:
  - Connecting lines between evaluations
  - Highlight status transitions vs. comments
  - Responsive layout for mobile
- [ ] Form UX improvements:
  - Disable submit button during API call
  - Auto-scroll to new evaluation after submission
  - Character counter on comment textarea
  - Clear form after successful submission

#### 7.2 Accessibility

**Tasks**:
- [ ] Add ARIA labels to all form inputs
- [ ] Ensure keyboard navigation works (Tab order)
- [ ] Screen reader announcements for status changes
- [ ] Color contrast meets WCAG AA standards
- [ ] Focus visible on interactive elements

#### 7.3 Error Handling Polish

**Tasks**:
- [ ] Map API error codes to user-friendly messages
- [ ] Display validation errors inline (next to form fields)
- [ ] Toast notifications for success/error
- [ ] Clear error messages on form re-submission

#### 7.4 Final Documentation

**Tasks**:
- [ ] Update README with evaluation workflow overview
- [ ] Add screenshots/GIFs of evaluation UI to quickstart guide
- [ ] Document environment setup for development/testing
- [ ] Create CHANGELOG entry for this feature

**Success Criteria**:
- UI is polished and professional
- Accessibility audit passes
- Documentation is complete and accurate

---

## Complexity Tracking

| Item | Status | Notes |
|------|--------|-------|
| Constitution Principles | ✅ MET | All principles satisfied by design |
| Test Coverage (80% minimum) | 🔄 IN PROGRESS | Backend and frontend tests planned in phases |
| Database Migration | 🔄 PLANNED | Flyway script for idea_evaluations table |
| JPA Entity Relationships | 🔄 PLANNED | IdeaEvaluation -> Idea, IdeaEvaluation -> User |
| Status Workflow Validation | 🔄 PLANNED | IdeaStatusValidator enforces state machine |
| Role-Based Access Control | 🔄 PLANNED | @PreAuthorize on all evaluation endpoints |
| Comment Requirement Logic | 🔄 PLANNED | Service layer enforces mandatory comments |
| Frontend Evaluation UI | 🔄 PLANNED | EvaluationPanel component with form and timeline |
| API Integration | 🔄 PLANNED | Frontend service methods call backend APIs |

---

## Risk Mitigations

| Risk | Severity | Mitigation |
|------|----------|-----------|
| Concurrent status updates | Medium | Consider optimistic locking (@Version on Idea entity); Integration tests verify behavior |
| Comment length edge cases | Low | @Size validation (5000 chars); Frontend character counter warns user |
| Unauthorized evaluation attempts | High | @PreAuthorize + server-side role validation enforced; Integration tests verify 403 responses |
| Backward status transitions | Medium | IdeaStatusValidator prevents invalid transitions; Unit tests cover all edge cases |
| Missing evaluation history | Low | Empty array handled gracefully in UI; "No evaluations yet" message |
| JWT token expiration | Low | Frontend interceptor refreshes token (existing auth system functionality) |
| N+1 query problem | Medium | EAGER fetch evaluator in IdeaEvaluation; Composite indexes optimize queries |

---

## Success Criteria Mapping

| Spec Success Criterion | Implementation Verification |
|------------------------|------------------------------|
| SC-001: View details < 3 sec | Integration test measures response time; Database indexes optimize queries |
| SC-002: Status change < 1 min | Simple form with direct API call; Measured in integration test (target < 500ms) |
| SC-003: 100% workflow validation | Unit tests cover all valid/invalid transitions; Service layer enforces rules |
| SC-004: 100% comment enforcement | Validation tests verify 400 error when comment missing for ACCEPTED/REJECTED |
| SC-005: Real-time status view | Frontend refetches after evaluation action; No caching issues |
| SC-006: Zero unauthorized access | Integration tests verify 403 for unauthorized roles; @PreAuthorize enforced |
| SC-007: Complete audit trail | IdeaEvaluation records immutable; All fields logged (evaluator, timestamp, action) |
| SC-008: 99%+ comment success rate | Integration tests verify successful save; Error handling for edge cases |

---

## Testing Summary

### Backend Tests

**Unit Tests** (JUnit 5 + Mockito):
- IdeaEvaluationTest: 5 test cases (entity validation)
- IdeaStatusValidatorTest: 10 test cases (workflow logic)
- IdeaEvaluationServiceTest: 10 test cases (business logic, mocked repos)
- IdeaServiceTest: 8 test cases (updateStatus method, mocked dependencies)
- **Total**: ~33 test cases

**Integration Tests** (Testcontainers):
- IdeaEvaluationRepositoryTest: 6 test cases (queries, cascades)
- IdeaEvaluationServiceIntegrationTest: 8 test cases (database persistence, transactions)
- IdeaServiceIntegrationTest: 6 test cases (status updates with DB)
- IdeaEvaluationControllerIntegrationTest: 15 test cases (all endpoints with various scenarios)
- **Total**: ~35 test cases

**Coverage Target**: 80% line coverage across evaluation classes

### Frontend Tests

**Component Tests** (Vitest + React Testing Library):
- EvaluationPanel.test.jsx: 15 test cases (rendering, forms, validation, role-based display)
- IdeaDetail.test.jsx: 4 test cases (integration with EvaluationPanel)
- statusUtils.test.js: 8 test cases (utility functions)
- **Total**: ~27 test cases

**API Service Tests** (MSW):
- ideaService.test.js: 10 test cases (evaluation endpoints)
- **Total**: ~10 test cases

**Coverage Target**: 80% line coverage for evaluation components/services

---

## Implementation Checklist

### ✅ Phase 0: Documentation (COMPLETE)
- [x] Create data-model.md
- [x] Create contracts/API.md
- [ ] Create quickstart.md (Phase 4)

### Phase 1: Backend Database & Entities
- [ ] Create Flyway migration V7__create_idea_evaluations_table.sql
- [ ] Create IdeaEvaluation entity
- [ ] Extend Idea entity with evaluations relationship
- [ ] Create IdeaEvaluationRepository
- [ ] Write entity tests
- [ ] Write repository integration tests

### Phase 2: Backend Services & Business Logic
- [ ] Create IdeaStatusValidator utility
- [ ] Write IdeaStatusValidatorTest
- [ ] Create IdeaEvaluationService
- [ ] Enhance IdeaService with updateStatus method
- [ ] Create InvalidStatusTransitionException
- [ ] Write IdeaEvaluationServiceTest
- [ ] Write IdeaServiceTest (updateStatus tests)
- [ ] Write integration tests

### Phase 3: Backend REST APIs
- [ ] Create UpdateIdeaStatusRequest DTO
- [ ] Create AddCommentRequest DTO
- [ ] Create IdeaEvaluationResponse DTO
- [ ] Create EvaluationHistoryResponse DTO
- [ ] Enhance IdeaResponse with evaluationCount
- [ ] Create IdeaEvaluationController
- [ ] Write controller integration tests

### Phase 4: Backend Testing & Documentation
- [ ] Run JaCoCo coverage report (verify 80%)
- [ ] End-to-end workflow test
- [ ] Performance test
- [ ] Create quickstart.md

### Phase 5: Frontend UI Components
- [ ] Create statusUtils.js
- [ ] Create EvaluationPanel component
- [ ] Enhance IdeaDetail page
- [ ] Enhance ideaService.js
- [ ] Enhance useIdeas hook

### Phase 6: Frontend Testing
- [ ] Create EvaluationPanel.test.jsx
- [ ] Enhance IdeaDetail.test.jsx
- [ ] Enhance ideaService.test.js
- [ ] Create evaluationHandlers.js (MSW mocks)
- [ ] Run coverage report (verify 80%)

### Phase 7: Polish & Final Documentation
- [ ] UI enhancements (colors, icons, timeline)
- [ ] Accessibility improvements
- [ ] Error handling polish
- [ ] Update README
- [ ] Add screenshots to quickstart
- [ ] Create CHANGELOG entry

---

## Deployment Notes

**Database Migration**: Run `mvn flyway:migrate` to create `idea_evaluations` table

**Configuration**: No additional configuration needed; existing JWT and database config sufficient

**Rollout**: 
- Feature is non-breaking; existing ideas can immediately be evaluated
- No data migration needed (evaluations start empty for all ideas)

**Monitoring**: Consider tracking:
- Evaluation action frequency (ideas evaluated per day)
- Average time from submission to first evaluation
- Status transition patterns
- API response times

---

**Version**: 1.0  
**Last Updated**: February 26, 2026  
**Status**: Ready for implementation - Phases 1-7 to execute
