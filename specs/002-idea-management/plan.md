# Implementation Plan: Idea Management System

**Branch**: `002-idea-management` | **Date**: February 25, 2026 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/002-idea-management/spec.md`

## Summary

The Idea Management System enables submitters to propose innovations through a structured form (Title, Description, Category, optional file attachment), provides a dashboard for viewing all submissions, and implements an evaluation workflow where evaluators can transition ideas through status states (Submitted → Under Review → Accepted/Rejected) with feedback comments. This builds directly on the existing JWT authentication system and role-based access control (submitter/evaluator/admin).

## Technical Context

**Language/Version**: Java 21, Spring Boot 3.2.2  
**Primary Dependencies**: Spring Data JPA, Spring Security, JJWT (JWT), PostgreSQL, Flyway  
**Storage**: PostgreSQL database with new tables: `ideas`, `idea_evaluations`, `idea_attachments`  
**Testing**: JUnit 5, Mockito, Testcontainers (PostgreSQL), Spring Security Test; Frontend: Vitest, MSW  
**Target Platform**: Web application (Spring Boot backend REST API + React frontend)  
**Project Type**: Web service (monorepo with Java/Spring backend + React frontend)  
**Performance Goals**: Sub-second response times for idea listing, file upload completion within 30 seconds
**Constraints**: Single file attachment per idea (≤50MB), file types limited to PDF/PNG, 80% test coverage mandatory
**Scale/Scope**: Initial MVP for small team (< 500 ideas per sprint), single deployment instance

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### Verification Against Constitution Principles

| Principle | Status | Justification |
|-----------|--------|---------------|
| Clean Code Principles | ✅ PASS | Small functions, descriptive naming, comments explain WHY (JWT token validation, file type checking logic) |
| SOLID Principles | ✅ PASS | Repository pattern enforced for data access, Services layer for business logic, Controllers as thin adapters; Dependency Inversion via @Autowired abstractions |
| RESTful API Design | ✅ PASS | Resource-oriented endpoints (`POST /api/v1/ideas`, `GET /api/v1/ideas/{id}`, `PATCH /api/v1/ideas/{id}/status`); status codes used correctly; no verb-based endpoints |
| Repository Pattern | ✅ PASS | IdeaRepository, IdeaEvaluationRepository, IdeaAttachmentRepository abstractions; Controllers inject repositories via dependency injection; service layer orchestrates business logic |
| Testing Pyramid | ✅ PASS | 80% coverage target enforced by JaCoCo; Unit tests for validators, services; Integration tests using Testcontainers; E2E tests for API workflows |
| Security & Secrets | ✅ PASS | No secrets in code; JWT tokens validated on every request via filter; file uploads validated for type and size; @PreAuthorize prevents unauthorized access |
| Version Control | ✅ PASS | Commits follow conventional format; feature branch `002-idea-management` used; atomic changes with clear scope |

**Gate Status**: ✅ **PASSED** - All principles satisfied; design aligns with constitution.

## Project Structure

### Documentation (this feature)

```text
specs/002-idea-management/
├── spec.md                    # Feature specification (requirements & user stories)
├── plan.md                    # This file (technical approach & implementation phases)
├── data-model.md              # Database schema and JPA entities (Phase 1 output)
├── contracts/                 # API contract definitions (Phase 1 output)
│   ├── CreateIdeaRequest.md
│   ├── IdeaResponse.md
│   └── EvaluationCommentRequest.md
├── quickstart.md              # Integration guide (Phase 1 output)
└── checklists/
    └── requirements.md        # Specification quality checklist
```

### Source Code (repository root)

```text
backend/
├── src/
│   ├── main/
│   │   ├── java/com/innovatepam/
│   │   │   ├── auth/          # Existing authentication (reused)
│   │   │   └── idea/          # NEW - Idea Management feature
│   │   │       ├── model/
│   │   │       │   ├── Idea.java
│   │   │       │   ├── IdeaEvaluation.java
│   │   │       │   └── IdeaAttachment.java
│   │   │       ├── repository/
│   │   │       │   ├── IdeaRepository.java
│   │   │       │   ├── IdeaEvaluationRepository.java
│   │   │       │   └── IdeaAttachmentRepository.java
│   │   │       ├── service/
│   │   │       │   ├── IdeaService.java
│   │   │       │   ├── IdeaEvaluationService.java
│   │   │       │   └── FileStorageService.java
│   │   │       ├── controller/
│   │   │       │   ├── IdeaController.java
│   │   │       │   └── IdeaEvaluationController.java
│   │   │       ├── dto/
│   │   │       │   ├── CreateIdeaRequest.java
│   │   │       │   ├── IdeaResponse.java
│   │   │       │   ├── EvaluationCommentRequest.java
│   │   │       │   └── IdeaEvaluationResponse.java
│   │   │       ├── exception/
│   │   │       │   ├── IdeaNotFoundException.java
│   │   │       │   ├── UnauthorizedAccessException.java
│   │   │       │   └── InvalidFileException.java
│   │   │       └── util/
│   │   │           ├── IdeaStatusValidator.java
│   │   │           └── FileValidator.java
│   │   └── resources/
│   │       ├── db/migration/
│   │       │   ├── V4__create_ideas_table.sql
│   │       │   ├── V5__create_idea_evaluations_table.sql
│   │       │   └── V6__create_idea_attachments_table.sql
│   │       └── application.yml (add idea upload config)
│   │
│   └── test/
│       ├── java/com/innovatepam/idea/
│       │   ├── model/
│       │   │   ├── IdeaTest.java
│       │   │   └── IdeaEvaluationTest.java
│       │   ├── service/
│       │   │   ├── IdeaServiceTest.java (unit)
│       │   │   ├── IdeaServiceIntegrationTest.java (integration)
│       │   │   └── FileStorageServiceTest.java
│       │   ├── controller/
│       │   │   ├── IdeaControllerTest.java (unit)
│       │   │   └── IdeaControllerIntegrationTest.java (integration, Testcontainers)
│       │   ├── repository/
│       │   │   └── IdeaRepositoryIntegrationTest.java (Testcontainers)
│       │   └── util/
│       │       └── FileValidatorTest.java
│       └── resources/
│           └── application-test.yml

frontend/
├── src/
│   ├── components/
│   │   ├── IdeaForm.jsx              # NEW - Submission form (Tailwind CSS)
│   │   ├── FileUpload.jsx            # NEW - File upload component
│   │   └── EvaluationPanel.jsx       # NEW - Status/comment panel
│   ├── pages/
│   │   ├── IdeaSubmission.jsx        # NEW - Submission page
│   │   ├── IdeaListing.jsx           # NEW - Dashboard page
│   │   └── IdeaDetail.jsx            # NEW - Detail view with evaluation
│   ├── services/
│   │   ├── ideaService.js            # NEW - API calls (Axios, multipart/form-data)
│   │   └── fileService.js            # NEW - File upload utilities
│   ├── hooks/
│   │   ├── useIdeas.js               # NEW - Ideas data management
│   │   └── useFileUpload.js          # NEW - File upload state
│   ├── test/
│   │   ├── IdeaForm.test.jsx         # NEW - Component tests
│   │   ├── IdeaListing.test.jsx      # NEW - Component tests
│   │   ├── ideaService.test.js       # NEW - API mock tests (MSW)
│   │   └── mocks/
│   │       └── ideaHandlers.js       # NEW - MSW mock handlers
│   └── utils/
│       ├── fileUploadUtils.js        # NEW - Validation, formatting utils
│       └── statusUtils.js            # NEW - Status transition logic

```

**Structure Decision**: Standard layered architecture with clean separation:
- **Backend**: Multi-package approach under `com.innovatepam.idea` (mirroring existing `auth` package)
  - Model (JPA entities)
  - Repository (data access abstraction)
  - Service (business logic)
  - Controller (HTTP endpoints)
  - DTO (request/response contracts)
  - Utility classes for cross-cutting concerns
- **Frontend**: Feature-organized structure with separate services, hooks, and components
  - Components are presentational (Tailwind CSS styling)
  - Services handle all API communication
  - Hooks manage state and side effects
  - Tests colocated with source code

## Implementation Phases

### Phase 1: Core Data Model & Backend Services

**Duration**: ~4 days | **Deliverables**: Database schema, JPA entities, repositories, services

#### 1.1 Database Schema (Flyway migrations)

Create three new Flyway migration files:

- **V4__create_ideas_table.sql**: Schema for ideas with status tracking, timestamps, submitter reference
- **V5__create_idea_evaluations_table.sql**: Schema for evaluation comments linked to ideas and evaluators
- **V6__create_idea_attachments_table.sql**: Schema for file metadata and storage location

**Design Constraints**:
- Foreign keys to existing `users` table
- Composite index on `(idea_id, created_at)` for efficient evaluation history retrieval
- Status enum column validated by NOT NULL constraint
- Orphan attachment cleanup via ON DELETE CASCADE

#### 1.2 JPA Entities

Create entity classes with proper annotations:

- **Idea.java**: Core entity with Title, Description, Category, Status, SubmitterId, submission timestamp, relationships to User and IdeaAttachment
- **IdeaEvaluation.java**: Represents evaluator feedback with Comment, EvaluatorId, Timestamp, StatusSnapshot (what status was set to)
- **IdeaAttachment.java**: File metadata entity with FileName, FileType, FileSize, StorageLocation, UploadTimestamp

**Entity Relationships**:
- Idea → User (many-to-one, submitter)
- Idea → IdeaAttachment (one-to-many, evaluated, cascade delete)
- IdeaEvaluation → Idea (many-to-one)
- IdeaEvaluation → User (many-to-one, evaluator)

#### 1.3 Repository Interfaces

Create Spring Data JPA repository interfaces using Repository pattern:

- **IdeaRepository**: `findByCategoryAndStatus()`, `findBySubmitterId()`, `findAll()` with pagination
- **IdeaEvaluationRepository**: `findByIdeaId()` (ordered by creation time), `findByEvaluatorId()`
- **IdeaAttachmentRepository**: `findByIdeaId()`, `deleteByIdeaId()` (cascade handled by JPA)

#### 1.4 Service Layer

Implement business logic with proper error handling:

- **IdeaService**:
  - `createIdea(CreateIdeaRequest, User)`: Validate input, persist, return response
  - `getIdeaById(Long, User)`: Retrieve with permission check
  - `getAllIdeas(pageable)`: List all ideas with pagination
  - `updateIdeaStatus(IdeaId, NewStatus, User)`: Validate transitions, persist
  - Permission logic: Only idea submitter or admin can view/edit

- **IdeaEvaluationService**:
  - `addEvaluationComment(IdeaId, Comment, User)`: Validate user is evaluator/admin, persist
  - `getEvaluationHistory(IdeaId)`: Return all comments ordered by creation

- **FileStorageService**:
  - `saveFile(MultipartFile)`: Validate type/size, save to disk, return path
  - `getFile(fileId)`: Retrieve file from storage
  - `deleteFile(fileId)`: Remove from disk
  - Validation: Only PDF/PNG, max 50MB

#### 1.5 Unit & Integration Tests (Backend)

**Unit Tests** (mocked dependencies):
- IdeaService: Test CRUD operations, status transitions, permission logic
- FileStorageService: Test file validation (PDF/PNG only), size limits
- Validation utilities: Test boundary conditions

**Integration Tests** (Testcontainers + PostgreSQL):
- IdeaRepositoryIntegrationTest: Test queries, cascading deletes
- IdeaControllerIntegrationTest: Test API endpoints with live database
- File upload integration: Upload → validate → store → retrieve

**Coverage Target**: 80% line coverage (enforced by JaCoCo)

---

### Phase 2: REST API Controllers & Request/Response DTOs

**Duration**: ~3 days | **Deliverables**: API endpoints, DTOs, validation, error handling

#### 2.1 Controller Endpoints

Create `IdeaController` and `IdeaEvaluationController` with proper HTTP semantics:

**IdeaController** (role-based access via @PreAuthorize):
- `POST /api/v1/ideas` (@PreAuthorize("hasRole('SUBMITTER')")) - Create idea with file upload (multipart/form-data)
- `GET /api/v1/ideas` - List all ideas (paginated)
- `GET /api/v1/ideas/{id}` - Retrieve single idea detail
- `GET /api/v1/ideas/{id}/attachments` - Get file metadata

**IdeaEvaluationController** (role-based access):
- `PATCH /api/v1/ideas/{id}/status` (@PreAuthorize("hasRole('EVALUATOR') or hasRole('ADMIN')")) - Update status with required comment for rejection
- `POST /api/v1/ideas/{id}/comments` (@PreAuthorize("hasRole('EVALUATOR') or hasRole('ADMIN')")) - Add evaluation comment
- `GET /api/v1/ideas/{id}/evaluations` - Get all evaluation comments for an idea

#### 2.2 DTOs (Request/Response Contracts)

Create lightweight DTOs for serialization:

**Request DTOs**:
- `CreateIdeaRequest`: Title, Description, Category, file (MultipartFile)
- `UpdateIdeaStatusRequest`: NewStatus, Comment (required for rejection)
- `AddEvaluationCommentRequest`: Comment, optional StatusChange

**Response DTOs**:
- `IdeaResponse`: Id, Title, Description, Category, Status, SubmitterName, CreatedAt, HasAttachment, EvaluationCount
- `IdeaDetailResponse`: All fields from IdeaResponse + Description detail + Attachment metadata + Evaluation history
- `IdeaEvaluationResponse`: Id, EvaluatorName, Comment, CreatedAt, StatusChangeTo
- `FileMetadataResponse`: FileName, FileType, FileSize, UploadTimestamp

#### 2.3 Validation & Error Handling

- Input validation: @Valid on DTOs, custom validators for status transitions
- Global exception handler: Map custom exceptions to HTTP responses (400, 403, 404, 500)
- Exception classes:
  - `IdeaNotFoundException` (404)
  - `UnauthorizedAccessException` (403)
  - `InvalidStatusTransitionException` (400)
  - `InvalidFileException` (400)
  - `FileSizeLimitExceededException` (413)

#### 2.4 JWT Integration

Ensure all endpoints protected by existing JWT filter:
- Extract user from JWT token
- Validate token on every protected request
- Pass User object to service layer for permission checks

---

### Phase 3: Frontend Components & Integration

**Duration**: ~3 days | **Deliverables**: React components, API client, form handling

#### 3.1 React Components (Tailwind CSS)

**IdeaSubmissionPage**:
- Form with Title input, Description textarea, Category dropdown, file upload input
- File preview with validation feedback (type/size warnings)
- Submit button with loading state, success/error feedback
- Redirect to listing on successful submission

**IdeaListingPage**:
- Table/card grid showing all ideas: Title, Submitter, Status (color-coded), CreatedAt, Category
- Pagination controls
- Click to detail view
- Filter by status (optional enhancement, out of scope for MVP)

**IdeaDetailPage**:
- Display full idea: Title, Description, Category, Status, Submitter, CreatedAt
- Download attachment link (if exists)
- Evaluation history section: All comments in chronological order
- Status update panel (only for evaluators):
  - Dropdown to change status with validation
  - Comment field (required for rejection)
  - Submit button with loading state

**FileUploadComponent**:
- Reusable drag-drop or click-to-select file input
- File type/size validation on client-side (before server request)
- Progress indicator during upload
- Error message display for invalid files

**EvaluationPanel**:
- Display current idea status
- List of all evaluation comments with evaluator name, timestamp, comment text
- Comment form for evaluators only
- Status transition controls (Submitted → Under Review → Accepted/Rejected)

#### 3.2 API Service (Axios)

Create `ideaService.js` with typed requests:

```javascript
// pseudo-code examples
ideaService.createIdea(formData) // multipart/form-data for file upload
ideaService.getAllIdeas(page, pageSize)
ideaService.getIdeaById(id)
ideaService.updateIdeaStatus(id, newStatus, comment)
ideaService.addEvaluationComment(id, comment)
```

**Implementation Details**:
- Axios instance with JWT token in Authorization header (extracted from localStorage/context)
- Error handling: Catch API errors, display user-friendly messages
- Retry logic for transient failures (optional, post-MVP)
- Request/response interceptors for token refresh (leverage existing auth system)

#### 3.3 Frontend Tests (Vitest + MSW)

**Unit Tests** (component snapshot & behavior):
- IdeaForm: Test form submission, validation feedback, file selection
- IdeaListing: Test rendering of idea list, pagination
- EvaluationPanel: Test status dropdown, required comment validation

**Integration Tests** (MSW mocked API):
- Mock `/api/v1/ideas` endpoint with sample responses
- Test form submission → API call → success/error handling
- Test loading states during API calls
- Test permission-based UI rendering (submitter vs evaluator view)

**Coverage Target**: 80% line coverage (vitest coverage report)

---

### Phase 4: File Handling & Storage Strategy

**Duration**: ~1.5 days | **Deliverables**: Local file storage service, security validation

#### 4.1 File Storage Implementation

**FileStorageService** strategy for MVP:
- **Storage Location**: Local filesystem at `/uploads/ideas/` (configurable via `application.yml`)
- **File Organization**: `{ideaId}/{timestamp}_{originalFileName}` to avoid collisions
- **Validation**:
  - File type whitelist: PDF, PNG only (check MIME type + file extension)
  - File size limit: 50MB max
  - Virus scanning: Optional (out of scope for MVP, can add ClamAV in future)
- **Security**:
  - Rename files to UUID to prevent path traversal attacks
  - Store original filename separately in database for display
  - Serve files via controller endpoint with permission check (only authenticated users)

**File Retrieval**:
- `GET /api/v1/ideas/{id}/attachments/{attachmentId}` endpoint
- Return file with proper Content-Type, Content-Disposition headers
- Permission check: Only submitter or admin can download

#### 4.2 Multipart Form-Data Handling

- Controller method accepts `@RequestPart("file") MultipartFile file`
- Configuration in `application.yml`: `spring.servlet.multipart.max-file-size=50MB`
- Handle exception `FileSizeLimitExceededException` in global exception handler

---

### Phase 5: Deployment & Configuration

**Duration**: ~1 day | **Deliverables**: Configuration files, documentation

#### 5.1 Application Configuration

**Backend** (`application.yml`):
```yaml
spring.datasource.url=jdbc:postgresql://localhost:5432/innovatepam
spring.jpa.hibernate.ddl-auto=validate # Let Flyway manage schema

# File upload
idea.upload.directory=/uploads/ideas
idea.upload.max-file-size=52428800  # 50MB in bytes
idea.upload.allowed-types=application/pdf,image/png

# JWT (reuse existing config)
app.jwtSecret=[existing secret]
app.jwtExpirationMs=86400000
```

**Frontend** (`.env`):
```
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

#### 5.2 Database Migrations

Flyway scripts handle all schema creation:
- V4, V5, V6 migration files created in Phase 1
- No manual SQL needed; run via Maven: `mvn flyway:migrate`

---

## Complexity Tracking

| Item | Status | Notes |
|------|--------|-------|
| All Constitution Principles | ✅ MET | No violations; all principles satisfied by design |
| Test Coverage (80% minimum) | ✅ PLANNED | Unit + Integration tests in each phase; JaCoCo enforces minimum |
| Testcontainers for Integration | ✅ PLANNED | Already in pom.xml; used for IdeaRepositoryIntegrationTest, ControllerIntegrationTest |
| MSW for Frontend Mocking | ✅ PLANNED | Setup MSW handlers for all idea API endpoints; used in component tests |
| File Upload Security | ✅ ADDRESSED | MIME type validation, size limits, file renaming, permission checks |
| Status Transition Logic | ✅ ADDRESSED | Service layer enforces state machine: Submitted → Under Review → (Accepted OR Rejected) |

---

## Risk Mitigations

| Risk | Severity | Mitigation |
|------|----------|-----------|
| Concurrent status updates | Medium | Database constraint on optimistic locking (version field in Idea entity); application-level validation |
| Large file uploads | Medium | Size limit (50MB) enforced; progress indicator on frontend; async handling (Spring async executor) |
| Unauthorized file access | High | Permission check in file download endpoint; no direct filesystem access from frontend |
| Missing file attachment | Low | IdeaAttachment relationship is nullable; API response indicates attachment presence; querying designed to handle missing files |
| JWT token expiration | Low | Leverage existing auth system; frontend interceptor handles token refresh |

---

## Success Criteria Mapping

| Spec Success Criterion | Implementation Verification |
|------------------------|------------------------------|
| SC-001: Submission < 3 min | Measure via E2E test; UI provides instant feedback |
| SC-002: Listing loads < 5 sec | Pagination + database query optimization; load test |
| SC-003: Status + comment < 1 min | Simple form, direct API call; measure in integration test |
| SC-004: 95% file upload success | Server-side validation + client-side validation (PDF/PNG, 50MB) |
| SC-005: Feedback within 24hrs | Process metric, not system metric; business process |
| SC-006: 100% role enforcement | @PreAuthorize on all protected endpoints; test coverage |
| SC-007: Zero data loss | ACID properties of PostgreSQL + transactional service methods |
