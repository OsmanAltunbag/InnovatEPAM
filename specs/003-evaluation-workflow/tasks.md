---

description: "Task list for evaluation workflow implementation"
---

# Tasks: Evaluation Workflow

**Input**: Design documents from `/specs/003-evaluation-workflow/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), data-model.md, contracts/API.md

**Tests**: Mandatory 80% test coverage following Testing Pyramid (70% unit, 20% integration, 10% component/E2E). Backend: JUnit 5, Mockito, Testcontainers. Frontend: Vitest, React Testing Library, MSW.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

---

## Phase 1: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [ ] T001 Create Flyway migration V7__create_idea_evaluations_table.sql in backend/src/main/resources/db/migration/
- [ ] T002 Run migration and verify table creation: `mvn flyway:migrate`
- [ ] T003 Create IdeaEvaluation JPA entity in backend/src/main/java/com/innovatepam/idea/model/IdeaEvaluation.java
- [ ] T004 Extend Idea entity with evaluations relationship (optional, bi-directional) in backend/src/main/java/com/innovatepam/idea/model/Idea.java
- [ ] T005 Create IdeaEvaluationRepository interface in backend/src/main/java/com/innovatepam/idea/repository/IdeaEvaluationRepository.java
- [ ] T006 [P] Create IdeaStatusValidator utility in backend/src/main/java/com/innovatepam/idea/util/IdeaStatusValidator.java
- [ ] T007 [P] Create InvalidStatusTransitionException in backend/src/main/java/com/innovatepam/idea/exception/InvalidStatusTransitionException.java
- [ ] T008 [P] Create UpdateIdeaStatusRequest DTO in backend/src/main/java/com/innovatepam/idea/dto/UpdateIdeaStatusRequest.java
- [ ] T009 [P] Create AddCommentRequest DTO in backend/src/main/java/com/innovatepam/idea/dto/AddCommentRequest.java
- [ ] T010 [P] Create IdeaEvaluationResponse DTO in backend/src/main/java/com/innovatepam/idea/dto/IdeaEvaluationResponse.java
- [ ] T011 [P] Create EvaluationHistoryResponse DTO in backend/src/main/java/com/innovatepam/idea/dto/EvaluationHistoryResponse.java
- [ ] T012 Enhance IdeaResponse DTO with evaluationCount field in backend/src/main/java/com/innovatepam/idea/dto/IdeaResponse.java
- [ ] T013 Create IdeaEvaluationService in backend/src/main/java/com/innovatepam/idea/service/IdeaEvaluationService.java
- [ ] T014 Enhance IdeaService with updateStatus method in backend/src/main/java/com/innovatepam/idea/service/IdeaService.java
- [ ] T015 Create IdeaEvaluationController in backend/src/main/java/com/innovatepam/idea/controller/IdeaEvaluationController.java
- [ ] T016 Update global exception handler to cover new exceptions in backend/src/main/java/com/innovatepam/idea/exception/IdeaExceptionHandler.java
- [ ] T017 [P] Create statusUtils.js utility in frontend/src/utils/statusUtils.js
- [ ] T018 [P] Create quickstart.md guide in specs/003-evaluation-workflow/quickstart.md

**Checkpoint**: Foundation ready - user story implementation can now begin

---

## Phase 2: User Story 1 - Evaluator Views Submitted Ideas for Review (Priority: P1) 🎯 MVP

**Goal**: Evaluators can browse and access full idea details including attachments

**Independent Test**: Login as evaluator, navigate to ideas list, click on idea, verify full details displayed with attachment access

### Backend Tasks for User Story 1

- [ ] T019 [US1] Verify IdeaController GET /api/v1/ideas/{id} endpoint returns full idea details in backend/src/main/java/com/innovatepam/idea/controller/IdeaController.java
- [ ] T020 [US1] Verify IdeaService getIdeaById includes attachment and submitter info in backend/src/main/java/com/innovatepam/idea/service/IdeaService.java
- [ ] T021 [US1] Ensure @PreAuthorize allows evaluator/admin roles access to idea details in backend/src/main/java/com/innovatepam/idea/controller/IdeaController.java

### Frontend Tasks for User Story 1

- [ ] T022 [P] [US1] Verify IdeaDetail page displays full idea information in frontend/src/pages/IdeaDetail.jsx
- [ ] T023 [P] [US1] Verify IdeaListing page shows ideas with status badges in frontend/src/pages/IdeaListing.jsx
- [ ] T024 [US1] Ensure role-based rendering (evaluators see all ideas) in frontend/src/pages/IdeaDetail.jsx

**Checkpoint**: User Story 1 is functional and independently testable

---

## Phase 3: User Story 2 - Evaluator Updates Idea Status Through Workflow (Priority: P1) 🎯 MVP

**Goal**: Evaluators can transition idea status through defined workflow

**Independent Test**: Login as evaluator, select submitted idea, change to UNDER_REVIEW, verify status persisted; try invalid transition, verify error

### Backend Tasks for User Story 2

- [ ] T025 [US2] Implement IdeaStatusValidator.isValidTransition method in backend/src/main/java/com/innovatepam/idea/util/IdeaStatusValidator.java
- [ ] T026 [US2] Implement IdeaService.updateStatus method with validation in backend/src/main/java/com/innovatepam/idea/service/IdeaService.java
- [ ] T027 [US2] Implement PATCH /api/v1/ideas/{id}/status endpoint in backend/src/main/java/com/innovatepam/idea/controller/IdeaEvaluationController.java
- [ ] T028 [US2] Add @PreAuthorize("hasAnyRole('EVALUATOR', 'ADMIN')") to status endpoint in backend/src/main/java/com/innovatepam/idea/controller/IdeaEvaluationController.java
- [ ] T029 [US2] Ensure status update and evaluation creation in single @Transactional in backend/src/main/java/com/innovatepam/idea/service/IdeaService.java

### Frontend Tasks for User Story 2

- [ ] T030 [P] [US2] Create EvaluationPanel component with status dropdown in frontend/src/components/EvaluationPanel.jsx
- [ ] T031 [US2] Implement status update form submission logic in frontend/src/components/EvaluationPanel.jsx
- [ ] T032 [US2] Add updateIdeaStatus API call in frontend/src/services/ideaService.js
- [ ] T033 [US2] Add updateStatus method to useIdeas hook in frontend/src/hooks/useIdeas.js
- [ ] T034 [US2] Implement statusUtils functions (getAllowedNextStatuses, getStatusColor, getStatusLabel) in frontend/src/utils/statusUtils.js
- [ ] T035 [US2] Integrate EvaluationPanel into IdeaDetail page in frontend/src/pages/IdeaDetail.jsx

**Checkpoint**: User Stories 1 and 2 are functional and independently testable

---

## Phase 4: User Story 3 - Evaluator Provides Feedback via Comments (Priority: P1) 🎯 MVP

**Goal**: Evaluators must provide comments when accepting/rejecting ideas

**Independent Test**: Attempt to accept idea without comment (should fail), provide comment and accept (should succeed)

### Backend Tasks for User Story 3

- [ ] T036 [US3] Implement IdeaStatusValidator.isCommentRequired method in backend/src/main/java/com/innovatepam/idea/util/IdeaStatusValidator.java
- [ ] T037 [US3] Add comment validation in IdeaService.updateStatus in backend/src/main/java/com/innovatepam/idea/service/IdeaService.java
- [ ] T038 [US3] Implement IdeaEvaluationService.addStatusEvaluation method in backend/src/main/java/com/innovatepam/idea/service/IdeaEvaluationService.java
- [ ] T039 [US3] Implement IdeaEvaluationService.addComment method (standalone comments) in backend/src/main/java/com/innovatepam/idea/service/IdeaEvaluationService.java
- [ ] T040 [US3] Implement POST /api/v1/ideas/{id}/comments endpoint in backend/src/main/java/com/innovatepam/idea/controller/IdeaEvaluationController.java
- [ ] T041 [US3] Add @Size(max=5000) validation on comment field in backend/src/main/java/com/innovatepam/idea/dto/AddCommentRequest.java

### Frontend Tasks for User Story 3

- [ ] T042 [P] [US3] Add comment textarea to status update form in EvaluationPanel in frontend/src/components/EvaluationPanel.jsx
- [ ] T043 [P] [US3] Implement client-side validation (comment required for ACCEPTED/REJECTED) in frontend/src/components/EvaluationPanel.jsx
- [ ] T044 [P] [US3] Add standalone comment form to EvaluationPanel in frontend/src/components/EvaluationPanel.jsx
- [ ] T045 [P] [US3] Add character counter (X / 5000) to comment textarea in frontend/src/components/EvaluationPanel.jsx
- [ ] T046 [US3] Add addEvaluationComment API call in frontend/src/services/ideaService.js
- [ ] T047 [US3] Add addComment method to useIdeas hook in frontend/src/hooks/useIdeas.js
- [ ] T048 [US3] Implement isCommentRequired utility function in frontend/src/utils/statusUtils.js

**Checkpoint**: User Stories 1, 2, and 3 are functional and independently testable

---

## Phase 5: User Story 4 - Submitter Views Evaluation Status and Feedback (Priority: P1) 🎯 MVP

**Goal**: Submitters can view current status and read evaluation comments on their ideas

**Independent Test**: Submit idea, have evaluator add comments and change status, login as submitter, verify can see updates

### Backend Tasks for User Story 4

- [ ] T049 [US4] Implement IdeaEvaluationService.getEvaluationHistory method in backend/src/main/java/com/innovatepam/idea/service/IdeaEvaluationService.java
- [ ] T050 [US4] Implement GET /api/v1/ideas/{id}/evaluations endpoint in backend/src/main/java/com/innovatepam/idea/controller/IdeaEvaluationController.java
- [ ] T051 [US4] Add @PreAuthorize("isAuthenticated()") to evaluations endpoint (all users can view) in backend/src/main/java/com/innovatepam/idea/controller/IdeaEvaluationController.java
- [ ] T052 [US4] Ensure evaluation history ordered by createdAt ASC in backend/src/main/java/com/innovatepam/idea/repository/IdeaEvaluationRepository.java

### Frontend Tasks for User Story 4

- [ ] T053 [P] [US4] Add evaluation history timeline to EvaluationPanel in frontend/src/components/EvaluationPanel.jsx
- [ ] T054 [P] [US4] Display evaluations chronologically with evaluator name, timestamp, comment in frontend/src/components/EvaluationPanel.jsx
- [ ] T055 [P] [US4] Add visual indicators for status transitions (icons, colors) in frontend/src/components/EvaluationPanel.jsx
- [ ] T056 [P] [US4] Show "No evaluations yet" empty state in frontend/src/components/EvaluationPanel.jsx
- [ ] T057 [US4] Add getEvaluationHistory API call in frontend/src/services/ideaService.js
- [ ] T058 [US4] Add fetchEvaluations method to useIdeas hook in frontend/src/hooks/useIdeas.js
- [ ] T059 [US4] Implement role-based rendering (submitters see read-only history) in frontend/src/components/EvaluationPanel.jsx

**Checkpoint**: User Stories 1, 2, 3, and 4 are functional and independently testable

---

## Phase 6: User Story 5 - Role-Based Access Control for Evaluation (Priority: P1) 🎯 MVP

**Goal**: System enforces role-based permissions (only evaluators/admins can evaluate)

**Independent Test**: Attempt to access evaluation endpoints as different roles, verify authorization checks

### Backend Tasks for User Story 5

- [ ] T060 [P] [US5] Verify @PreAuthorize annotations on all evaluation endpoints in backend/src/main/java/com/innovatepam/idea/controller/IdeaEvaluationController.java
- [ ] T061 [P] [US5] Ensure UnauthorizedAccessException mapped to 403 Forbidden in backend/src/main/java/com/innovatepam/idea/exception/IdeaExceptionHandler.java
- [ ] T062 [US5] Add server-side role check in IdeaService.updateStatus in backend/src/main/java/com/innovatepam/idea/service/IdeaService.java

### Frontend Tasks for User Story 5

- [ ] T063 [P] [US5] Conditional rendering: Hide evaluation controls for submitters in frontend/src/components/EvaluationPanel.jsx
- [ ] T064 [P] [US5] Show 403 error message when unauthorized user attempts evaluation in frontend/src/components/EvaluationPanel.jsx
- [ ] T065 [US5] Verify role check in useAuth hook works correctly in frontend/src/hooks/useAuth.js

**Checkpoint**: All user stories (1-5) are functional and independently testable. MVP is complete.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [ ] T066 [P] Add status badge colors and icons (getStatusColor, getStatusIcon) in frontend/src/utils/statusUtils.js
- [ ] T067 [P] Improve evaluation timeline visual design (connecting lines, better spacing) in frontend/src/components/EvaluationPanel.jsx
- [ ] T068 [P] Add loading spinners during API calls in frontend/src/components/EvaluationPanel.jsx
- [ ] T069 [P] Add success/error toast notifications in frontend/src/components/EvaluationPanel.jsx
- [ ] T070 [P] Add ARIA labels for accessibility in frontend/src/components/EvaluationPanel.jsx
- [ ] T071 [P] Ensure keyboard navigation works (Tab order, Enter to submit) in frontend/src/components/EvaluationPanel.jsx
- [ ] T072 [P] Add inline validation error messages in frontend/src/components/EvaluationPanel.jsx
- [ ] T073 [P] Update API documentation in specs/003-evaluation-workflow/contracts/API.md if needed
- [ ] T074 [P] Validate quickstart guide steps in specs/003-evaluation-workflow/quickstart.md
- [ ] T075 Code review and refactoring pass on all backend/frontend evaluation code

---

## Phase 8: Backend Test Implementation (80% Coverage - Testing Pyramid)

**Purpose**: Comprehensive backend test coverage following Testing Pyramid: 70% unit, 20% integration, 10% E2E

**Coverage Target**: >=80% line coverage measured by JaCoCo

### Backend Unit Tests (Base of Pyramid - 70%)

**Purpose**: Test business logic, validation, and utilities in isolation with mocked dependencies

- [ ] T076 [P] Unit test IdeaEvaluation entity validation (@NotBlank on comment, @PrePersist timestamp) in backend/src/test/java/com/innovatepam/idea/model/IdeaEvaluationTest.java
- [ ] T077 [P] Unit test IdeaStatusValidator.isValidTransition (all valid/invalid transitions) in backend/src/test/java/com/innovatepam/idea/util/IdeaStatusValidatorTest.java
- [ ] T078 [P] Unit test IdeaStatusValidator.isCommentRequired (ACCEPTED/REJECTED true, others false) in backend/src/test/java/com/innovatepam/idea/util/IdeaStatusValidatorTest.java
- [ ] T079 [P] Unit test IdeaEvaluationService.addComment with mocked repositories in backend/src/test/java/com/innovatepam/idea/service/IdeaEvaluationServiceTest.java
- [ ] T080 [P] Unit test IdeaEvaluationService.addStatusEvaluation in backend/src/test/java/com/innovatepam/idea/service/IdeaEvaluationServiceTest.java
- [ ] T081 [P] Unit test IdeaEvaluationService.getEvaluationHistory in backend/src/test/java/com/innovatepam/idea/service/IdeaEvaluationServiceTest.java
- [ ] T082 [P] Unit test IdeaService.updateStatus with valid transitions in backend/src/test/java/com/innovatepam/idea/service/IdeaServiceTest.java
- [ ] T083 [P] Unit test IdeaService.updateStatus with invalid transitions throws exception in backend/src/test/java/com/innovatepam/idea/service/IdeaServiceTest.java
- [ ] T084 [P] Unit test IdeaService.updateStatus with missing required comment throws exception in backend/src/test/java/com/innovatepam/idea/service/IdeaServiceTest.java
- [ ] T085 [P] Unit test UpdateIdeaStatusRequest validation (@NotNull on newStatus) in backend/src/test/java/com/innovatepam/idea/dto/UpdateIdeaStatusRequestTest.java
- [ ] T086 [P] Unit test AddCommentRequest validation (@NotBlank, @Size) in backend/src/test/java/com/innovatepam/idea/dto/AddCommentRequestTest.java

### Backend Integration Tests (Middle of Pyramid - 20%)

**Purpose**: Test component interactions with real database and Spring context using Testcontainers

- [ ] T087 Configure Testcontainers for evaluation tests (reuse existing setup) in backend/src/test/resources/application-test.yml
- [ ] T088 [P] Integration test IdeaEvaluationRepository.findByIdeaIdOrderByCreatedAtAsc with Testcontainers in backend/src/test/java/com/innovatepam/idea/repository/IdeaEvaluationRepositoryTest.java
- [ ] T089 [P] Integration test cascade delete (deleting idea removes evaluations) in backend/src/test/java/com/innovatepam/idea/repository/IdeaEvaluationRepositoryTest.java
- [ ] T090 Integration test IdeaEvaluationService with real database persistence in backend/src/test/java/com/innovatepam/idea/service/IdeaEvaluationServiceIntegrationTest.java
- [ ] T091 Integration test IdeaService.updateStatus with database transactions in backend/src/test/java/com/innovatepam/idea/service/IdeaServiceIntegrationTest.java
- [ ] T092 Integration test transactional rollback (status update fails, evaluation not created) in backend/src/test/java/com/innovatepam/idea/service/IdeaServiceIntegrationTest.java
- [ ] T093 Integration test PATCH /api/v1/ideas/{id}/status with MockMvc and Testcontainers in backend/src/test/java/com/innovatepam/idea/controller/IdeaEvaluationControllerIntegrationTest.java
- [ ] T094 Integration test POST /api/v1/ideas/{id}/comments with MockMvc in backend/src/test/java/com/innovatepam/idea/controller/IdeaEvaluationControllerIntegrationTest.java
- [ ] T095 Integration test GET /api/v1/ideas/{id}/evaluations with MockMvc in backend/src/test/java/com/innovatepam/idea/controller/IdeaEvaluationControllerIntegrationTest.java
- [ ] T096 Integration test authorization (@PreAuthorize) - submitter returns 403, evaluator returns 200 in backend/src/test/java/com/innovatepam/idea/controller/IdeaEvaluationControllerIntegrationTest.java
- [ ] T097 Integration test validation errors return 400 (missing comment, invalid transition) in backend/src/test/java/com/innovatepam/idea/controller/IdeaEvaluationControllerIntegrationTest.java

### Backend End-to-End Tests (Top of Pyramid - 10%)

**Purpose**: Test complete workflows across multiple components

- [ ] T098 E2E test: Create idea → Evaluator updates to UNDER_REVIEW → Evaluator accepts with comment → Submitter views history in backend/src/test/java/com/innovatepam/idea/EvaluationWorkflowE2ETest.java
- [ ] T099 E2E test: Evaluator attempts invalid transition and receives error in backend/src/test/java/com/innovatepam/idea/EvaluationWorkflowE2ETest.java
- [ ] T100 E2E test: Performance - status update completes in < 500ms in backend/src/test/java/com/innovatepam/idea/EvaluationWorkflowPerformanceTest.java

### Backend Coverage Verification

- [ ] T101 Run JaCoCo coverage report: `mvn clean test jacoco:report`
- [ ] T102 Verify >=80% line coverage on evaluation classes (entity, repository, service, controller, util)
- [ ] T103 Add missing tests if coverage below threshold

---

## Phase 9: Frontend Test Implementation (80% Coverage - Testing Pyramid)

**Purpose**: Comprehensive frontend test coverage following Testing Pyramid

**Coverage Target**: >=80% line coverage measured by Vitest

### Frontend Component Tests (Base of Pyramid - 70%)

**Purpose**: Test React components with mocked dependencies

- [ ] T104 [P] Configure Vitest coverage collection in frontend/vite.config.js or frontend/vitest.config.js
- [ ] T105 [P] Create MSW handlers for evaluation endpoints in frontend/src/test/mocks/evaluationHandlers.js
- [ ] T106 [P] Component test: EvaluationPanel renders evaluation history for submitter (no controls) in frontend/src/test/EvaluationPanel.test.jsx
- [ ] T107 [P] Component test: EvaluationPanel renders status update form for evaluator in frontend/src/test/EvaluationPanel.test.jsx
- [ ] T108 [P] Component test: EvaluationPanel renders standalone comment form for evaluator in frontend/src/test/EvaluationPanel.test.jsx
- [ ] T109 [P] Component test: Status dropdown shows only valid transitions in frontend/src/test/EvaluationPanel.test.jsx
- [ ] T110 [P] Component test: Validation error shown when comment required but missing in frontend/src/test/EvaluationPanel.test.jsx
- [ ] T111 [P] Component test: Submitting status update calls updateStatus API in frontend/src/test/EvaluationPanel.test.jsx
- [ ] T112 [P] Component test: Loading state shown during API call in frontend/src/test/EvaluationPanel.test.jsx
- [ ] T113 [P] Component test: Success message after status update in frontend/src/test/EvaluationPanel.test.jsx
- [ ] T114 [P] Component test: Error message on API failure in frontend/src/test/EvaluationPanel.test.jsx
- [ ] T115 [P] Component test: Evaluation timeline displays chronologically in frontend/src/test/EvaluationPanel.test.jsx
- [ ] T116 [P] Component test: Empty state "No evaluations yet" in frontend/src/test/EvaluationPanel.test.jsx
- [ ] T117 [P] Component test: IdeaDetail page renders EvaluationPanel in frontend/src/test/IdeaDetail.test.jsx
- [ ] T118 [P] Component test: Role-based rendering (evaluator vs submitter views) in frontend/src/test/IdeaDetail.test.jsx

### Frontend Service/Hook Tests

**Purpose**: Test API service methods and custom hooks

- [ ] T119 [P] Service test: ideaService.updateIdeaStatus calls PATCH /ideas/{id}/status in frontend/src/test/ideaService.test.js
- [ ] T120 [P] Service test: ideaService.addEvaluationComment calls POST /ideas/{id}/comments in frontend/src/test/ideaService.test.js
- [ ] T121 [P] Service test: ideaService.getEvaluationHistory calls GET /ideas/{id}/evaluations in frontend/src/test/ideaService.test.js
- [ ] T122 [P] Service test: API errors are handled correctly (400, 403, 404) in frontend/src/test/ideaService.test.js
- [ ] T123 [P] Hook test: useIdeas.updateStatus updates state correctly in frontend/src/test/useIdeas.test.js
- [ ] T124 [P] Hook test: useIdeas.addComment updates evaluation list in frontend/src/test/useIdeas.test.js
- [ ] T125 [P] Hook test: useIdeas.fetchEvaluations loads history in frontend/src/test/useIdeas.test.js

### Frontend Utility Tests

**Purpose**: Test utility functions

- [ ] T126 [P] Utility test: getAllowedNextStatuses returns correct transitions in frontend/src/test/statusUtils.test.js
- [ ] T127 [P] Utility test: getStatusLabel returns display text in frontend/src/test/statusUtils.test.js
- [ ] T128 [P] Utility test: getStatusColor returns Tailwind classes in frontend/src/test/statusUtils.test.js
- [ ] T129 [P] Utility test: getStatusIcon returns correct icon in frontend/src/test/statusUtils.test.js
- [ ] T130 [P] Utility test: isCommentRequired returns true for ACCEPTED/REJECTED in frontend/src/test/statusUtils.test.js

### Frontend Coverage Verification

- [ ] T131 Run Vitest coverage report: `npm run test:coverage` or `npm run test -- --coverage`
- [ ] T132 Verify >=80% line coverage on evaluation components (EvaluationPanel, statusUtils, ideaService evaluation methods)
- [ ] T133 Add missing tests if coverage below threshold

---

## Phase 10: Final Documentation & Deployment

**Purpose**: Complete documentation and prepare for deployment

- [ ] T134 [P] Finalize quickstart.md with working examples and screenshots in specs/003-evaluation-workflow/quickstart.md
- [ ] T135 [P] Update main README with evaluation workflow overview in README.md
- [ ] T136 [P] Create CHANGELOG entry for evaluation workflow feature in CHANGELOG.md
- [ ] T137 [P] Document environment variables (if any new ones added) in .env.example
- [ ] T138 Verify all migrations run successfully on clean database: `mvn flyway:migrate`
- [ ] T139 Verify backend starts without errors: `mvn spring-boot:run`
- [ ] T140 Verify frontend starts without errors: `npm run dev`
- [ ] T141 Manual smoke test: Complete evaluation workflow end-to-end (submit → evaluate → view as submitter)
- [ ] T142 Performance test: Verify status update < 500ms, evaluation history loads < 1000ms
- [ ] T143 Security audit: Verify @PreAuthorize on all endpoints, no secrets in code
- [ ] T144 Final code review and cleanup
- [ ] T145 Commit and push all changes to 003-evaluation-workflow branch
- [ ] T146 Create pull request with summary of changes and test coverage report

---

## Summary

**Total Tasks**: 146
- **Phase 1 (Foundational)**: 18 tasks
- **Phase 2 (User Story 1)**: 6 tasks
- **Phase 3 (User Story 2)**: 11 tasks
- **Phase 4 (User Story 3)**: 13 tasks
- **Phase 5 (User Story 4)**: 11 tasks
- **Phase 6 (User Story 5)**: 6 tasks
- **Phase 7 (Polish)**: 10 tasks
- **Phase 8 (Backend Testing)**: 26 tasks
- **Phase 9 (Frontend Testing)**: 30 tasks
- **Phase 10 (Documentation & Deployment)**: 13 tasks

**Estimated Duration**: ~10-12 days
- Foundational: 1.5 days
- User Stories 1-5: 4.5 days
- Polish: 1 day
- Testing: 3 days
- Documentation & Deployment: 1 day

**Parallel Work Opportunities**: Tasks marked with [P] can be executed in parallel, estimated to save ~3 days with 2-3 developers

**MVP Checkpoint**: After Phase 6 (User Story 5), all MVP functionality is complete and testable. Phases 7-10 are polish, testing, and documentation.
