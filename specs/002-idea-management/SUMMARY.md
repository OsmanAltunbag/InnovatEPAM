# Planning Summary: Idea Management System

**Branch**: `002-idea-management`  
**Date**: February 25, 2026  
**Status**: Phase 1 Design Complete ✅

---

## Deliverables Generated

### Specification Phase (Complete)
- ✅ [spec.md](spec.md) - User scenarios, requirements, success criteria
- ✅ [checklists/requirements.md](checklists/requirements.md) - Quality validation

### Planning Phase (Complete)
- ✅ **plan.md** - Technical implementation roadmap (5 phases)
- ✅ **data-model.md** - Database schema and JPA entities
- ✅ **contracts/API.md** - RESTful API contracts (7 endpoints)
- ✅ **quickstart.md** - Developer integration guide with code examples
- ✅ **SUMMARY.md** - This document

---

## Architecture Decision Summary

### Technology Stack
| Component | Technology | Version |
|-----------|-----------|---------|
| Backend | Spring Boot | 3.2.2 |
| Language | Java | 21 |
| Database | PostgreSQL | 13+ |
| ORM | JPA/Hibernate | (Spring default) |
| Authentication | JWT | 0.11.5 (JJWT) |
| Frontend | React | 18+ |
| UI Framework | Tailwind CSS | (existing) |
| HTTP Client | Axios | (existing) |
| Testing (Backend) | JUnit 5, Mockito, Testcontainers | (existing) |
| Testing (Frontend) | Vitest, MSW | (planned) |
| Code Coverage | JaCoCo | 0.8.12 (80% minimum) |

### Database Design
- **3 new tables**: `ideas`, `idea_evaluations`, `idea_attachments`
- **Design pattern**: Immutable audit trail for evaluations
- **Optimization**: Composite indexes for efficient querying
- **Constraints**: Cascade delete for attachments; ON DELETE RESTRICT for user references

### API Design
- **Style**: RESTful with proper HTTP semantics
- **Endpoints**: 7 primary operations (CRUD + state transitions)
- **Authentication**: JWT Bearer token (leverages existing system)
- **File Handling**: Multipart/form-data with server-side validation
- **Error Handling**: Standardized error response format with detailed messages

### Frontend Architecture
- **Component Structure**: Presentational components + Custom hooks
- **State Management**: React hooks (useState, useContext)
- **API Communication**: Axios with JWT interceptor
- **File Upload**: Drag-drop with client-side validation
- **Styling**: Tailwind CSS for rapid development

---

## Constitution Compliance

### Verified Principles ✅

| Principle | Status | Evidence |
|-----------|--------|----------|
| **Clean Code** | ✅ PASS | Small functions, descriptive naming, comment clarity |
| **SOLID** | ✅ PASS | Repository pattern, Dependency Inversion via @Autowired |
| **RESTful API** | ✅ PASS | Resource-oriented URLs, correct HTTP methods, proper status codes |
| **Repository Pattern** | ✅ PASS | IdeaRepository, IdeaEvaluationRepository abstractions |
| **Testing Pyramid** | ✅ PASS | Unit (70%) + Integration (20%) + E2E (10%), 80% coverage target |
| **Security** | ✅ PASS | JWT validation, file MIME type validation, @PreAuthorize role checks |
| **Version Control** | ✅ PASS | Branch: `002-idea-management`, conventional commits planned |

**Gate Status**: ✅ **PASSED** - Feature approved for implementation

---

## Implementation Phases

### Phase 1: Core Data Model & Backend Services (4 days)
**Deliverables**:
- Database migrations (Flyway V4-V6)
- JPA entities with lifecycle hooks
- Repository interfaces
- Service layer with business logic
- Unit + Integration tests

**Success**: Idea CRUD operations work; database persists data correctly

---

### Phase 2: REST API Controllers & DTOs (3 days)
**Deliverables**:
- IdeaController (7 endpoints)
- IdeaEvaluationController
- Request/Response DTOs
- Global exception handler
- API documentation

**Success**: All endpoints tested via Postman/curl; return correct HTTP status codes

---

### Phase 3: Frontend Components & Integration (3 days)
**Deliverables**:
- React components (IdeaForm, IdeaListing, IdeaDetail, FileUpload, EvaluationPanel)
- API service layer with Axios
- Custom hooks (useIdeas, useFileUpload)
- Component tests with Vitest + MSW
- Form validation

**Success**: UI components render correctly; form submission works end-to-end

---

### Phase 4: File Handling & Storage (1.5 days)
**Deliverables**:
- FileStorageService (local filesystem)
- File validation (MIME type, size)
- Secure file access via controller endpoint
- Tests for edge cases

**Success**: Files uploaded/downloaded correctly; invalid files rejected

---

### Phase 5: Deployment & Configuration (1 day)
**Deliverables**:
- Application properties configuration
- Database migration scripts
- Environment setup documentation
- CI/CD pipeline integration

**Success**: Feature deployed to staging; all tests pass

---

## Testing Strategy

### Backend Tests (Target: 80%+ coverage)

**Unit Tests** (70%):
- IdeaService: CRUD, status transitions, permission checks
- FileStorageService: Validation, storage, retrieval
- Validators: Edge cases, boundary conditions

**Integration Tests** (20%):
- IdeaRepositoryIntegrationTest (Testcontainers + PostgreSQL)
- IdeaControllerIntegrationTest (MockMvc + live database)
- File upload integration: Upload → validate → store → retrieve

**E2E Tests** (10%):
- API workflow: Create idea → List → Get detail → Update status → Add comment

### Frontend Tests (Target: 80%+ coverage)

**Unit Tests** (70%):
- Component rendering: IdeaForm, IdeaListing, EvaluationPanel
- Form validation: Required fields, file size checks
- State management: Handle loading/error states

**Integration Tests** (20%):
- MSW mocked API: Verify Axios calls match contract
- Form submission flow: Input → API call → success/error feedback
- Permission-based rendering: Submitter vs evaluator UI

**E2E Tests** (10%):
- Full user flow via Cypress (future, post-MVP)

### Coverage Enforcement
- **JaCoCo**: Enforces 80% minimum at build time
- **CI/CD**: Tests fail build if coverage drops
- **Reporting**: HTML reports at `target/site/jacoco/`

---

## Risk Mitigation

| Risk | Severity | Mitigation |
|------|----------|-----------|
| Concurrent status updates cause conflicts | Medium | Optimistic locking (version field) + application-level state validation |
| Large files consume disk space | Medium | 50MB max file size; cleanup service removes old files |
| Unauthorized file downloads | High | Permission check in file endpoint; no direct FS access |
| File corruption/loss | Low | PostgreSQL ACID guarantees; file path stored in DB |
| JWT token expiration | Low | Leverage existing auth system; frontend handles refresh |
| Database migration failures | Medium | Flyway rollback support; test migrations in CI before production |

---

## Success Criteria Mapping

| Spec Metric | Implementation Verification |
|------------|------------------------------|
| **SC-001**: Submission < 3 min | E2E test measures form completion time |
| **SC-002**: Listing loads < 5 sec | Load test with pagination; index optimization |
| **SC-003**: Status + comment < 1 min | Simple form, direct DB update; measured in tests |
| **SC-004**: 95% upload success rate | Client + server validation; tested with various file types |
| **SC-005**: Feedback within 24 hrs | Process metric; tracked via UI timestamp display |
| **SC-006**: 100% role enforcement | @PreAuthorize on all protected endpoints; unit tests verify |
| **SC-007**: Zero data loss | ACID transactions + persistent database; recovery tested |

---

## Key Design Decisions

### 1. Optimistic Locking for Concurrency Control
- **Why**: Prevents lost-update anomaly if two evaluators change status simultaneously
- **How**: JPA `@Version` field auto-incremented; OptimisticLockException caught by service
- **Alternative Rejected**: Pessimistic locking would create bottlenecks for listing

### 2. Immutable Evaluation History
- **Why**: Provides audit trail; prevents tampering with feedback
- **How**: IdeaEvaluation has no update/delete; only insert allowed
- **Result**: Evaluation comments never modified, supporting compliance

### 3. One Attachment Per Idea
- **Why**: Specification requires "single file attachment"
- **How**: UNIQUE constraint on idea_id in idea_attachments table
- **Future**: Remove constraint if multi-file support needed

### 4. Cascade Delete for File Attachments
- **Why**: File meaningless without idea; must be cleaned
- **How**: ON DELETE CASCADE at database level; application cleanup service removes file from storage
- **Safety**: Database enforces relationship integrity

### 5. JWT + Role-Based Access Control
- **Why**: Leverage existing authentication system; no duplication
- **How**: @PreAuthorize checks user role before allowing action
- **Coverage**: All protected endpoints enforced; tested in security tests

---

## Developer Onboarding

### Quick Start (30 minutes)

1. **Checkout branch**: `git checkout 002-idea-management`
2. **Setup database**: Apply Flyway migrations (automatic on startup)
3. **Run backend**: `mvn spring-boot:run`
4. **Run frontend**: `npm run dev`
5. **Test submission**: Use IdeaForm component to create test idea
6. **Read quickstart.md**: Detailed integration guide with code examples

### Documentation References

- **For Database Design**: See [data-model.md](data-model.md)
- **For API Contract**: See [contracts/API.md](contracts/API.md)
- **For Implementation Details**: See [plan.md](plan.md)
- **For Code Examples**: See [quickstart.md](quickstart.md)
- **For Business Requirements**: See [spec.md](spec.md)

---

## Repository Structure (Post-Implementation)

```
specs/002-idea-management/
├── spec.md                          # ✅ Requirements (created)
├── plan.md                          # ✅ Implementation plan (created)
├── data-model.md                    # ✅ Database schema (created)
├── quickstart.md                    # ✅ Developer guide (created)
├── contracts/
│   └── API.md                       # ✅ API contracts (created)
├── checklists/
│   └── requirements.md              # ✅ Quality checklist (created)
└── tasks.md                         # ⏳ Phase 2 (generated by /speckit.tasks)

backend/src/main/java/com/innovatepam/idea/
├── model/
│   ├── Idea.java                    # ⏳ JPA entity (to implement)
│   ├── IdeaEvaluation.java          # ⏳ JPA entity (to implement)
│   └── IdeaAttachment.java          # ⏳ JPA entity (to implement)
├── repository/
│   ├── IdeaRepository.java          # ⏳ Spring Data (to implement)
│   ├── IdeaEvaluationRepository.java # ⏳ Spring Data (to implement)
│   └── IdeaAttachmentRepository.java # ⏳ Spring Data (to implement)
├── service/
│   ├── IdeaService.java             # ⏳ Business logic (to implement)
│   ├── IdeaEvaluationService.java   # ⏳ Business logic (to implement)
│   └── FileStorageService.java      # ⏳ File handling (to implement)
├── controller/
│   ├── IdeaController.java          # ⏳ API endpoints (to implement)
│   └── IdeaEvaluationController.java # ⏳ API endpoints (to implement)
├── dto/
│   ├── CreateIdeaRequest.java       # ⏳ Request DTO (to implement)
│   ├── IdeaResponse.java            # ⏳ Response DTO (to implement)
│   ├── IdeaDetailResponse.java      # ⏳ Response DTO (to implement)
│   └── IdeaEvaluationResponse.java  # ⏳ Response DTO (to implement)
├── exception/
│   ├── IdeaNotFoundException.java    # ⏳ Custom exception (to implement)
│   ├── UnauthorizedAccessException.java # ⏳ Custom exception (to implement)
│   └── InvalidFileException.java    # ⏳ Custom exception (to implement)
└── util/
    ├── IdeaStatusValidator.java     # ⏳ State machine validation (to implement)
    └── FileValidator.java           # ⏳ File validation (to implement)

backend/src/main/resources/db/migration/
├── V4__create_ideas_table.sql       # ⏳ Flyway migration (to create)
├── V5__create_idea_evaluations_table.sql # ⏳ Flyway migration (to create)
└── V6__create_idea_attachments_table.sql # ⏳ Flyway migration (to create)

frontend/src/
├── components/
│   └── ideas/
│       ├── IdeaForm.jsx             # ⏳ Submission form (to implement)
│       ├── IdeaListing.jsx          # ⏳ Dashboard (to implement)
│       ├── IdeaDetail.jsx           # ⏳ Detail view (to implement)
│       ├── FileUpload.jsx           # ⏳ File upload (to implement)
│       └── EvaluationPanel.jsx      # ⏳ Evaluation controls (to implement)
├── pages/
│   └── ideas/
│       ├── IdeaSubmission.jsx       # ⏳ Submission page (to implement)
│       ├── IdeaListing.jsx          # ⏳ Listing page (to implement)
│       └── IdeaDetail.jsx           # ⏳ Detail page (to implement)
├── services/
│   └── ideaService.js              # ⏳ API client (to implement)
├── hooks/
│   ├── useIdeas.js                 # ⏳ Ideas state hook (to implement)
│   └── useFileUpload.js            # ⏳ Upload state hook (to implement)
└── test/
    ├── mocks/
    │   └── ideaHandlers.js         # ⏳ MSW handlers (to implement)
    ├── IdeaForm.test.jsx           # ⏳ Component tests (to implement)
    ├── IdeaListing.test.jsx        # ⏳ Component tests (to implement)
    └── ideaService.test.js         # ⏳ API tests (to implement)
```

---

## Next Actions

### Immediate (Next Meeting)
1. Review and approve plan.md architecture
2. Discuss timeline with team (estimate: 2 weeks for full implementation)
3. Create task cards in Jira (from tasks.md when generated)
4. Assign developers to phases

### Short-term (This Sprint)
1. Implement Phase 1-2 (data model + API controllers)
2. Write comprehensive tests
3. Conduct code review against constitution
4. Deploy to staging

### Medium-term (Next Sprint)
1. Implement Phase 3-4 (frontend components + file handling)
2. Integration testing across stack
3. Performance testing and optimization
4. User acceptance testing (UAT)

### Release Readiness
1. Documentation complete and reviewed
2. All tests passing (80%+ coverage)
3. Security scanning passes
4. Staging environment verification
5. Production deployment + monitoring

---

## Contact & Support

- **Architecture Questions**: Refer to [plan.md](plan.md) Technical Context section
- **Database Design**: Refer to [data-model.md](data-model.md)
- **API Usage**: Refer to [contracts/API.md](contracts/API.md)
- **Setup Issues**: Refer to [quickstart.md](quickstart.md) Troubleshooting section
- **Constitution Violations**: Refer to this document's Constitution Compliance section

---

**Plan Status**: ✅ **READY FOR IMPLEMENTATION**  
**Approval Date**: February 25, 2026  
**Approved By**: Speckit Planning System
