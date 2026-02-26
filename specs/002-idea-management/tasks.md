# Tasks: Idea Management System

**Input**: Design documents from `/specs/002-idea-management/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), data-model.md, contracts/API.md

## Format: `[ID] [P?] [Phase] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- Each task includes verification steps

---

## Phase 1: Persistence & Entities

**Goal**: Database schema + JPA entities + repositories

- [X] T001 [P] [Phase 1] Add Flyway migration for ideas table in backend/src/main/resources/db/migration/V4__create_ideas_table.sql. Verify: `mvn -f backend/pom.xml -DskipTests flyway:migrate` creates `ideas` table with expected columns and indexes.
- [X] T002 [P] [Phase 1] Add Flyway migration for idea evaluations in backend/src/main/resources/db/migration/V5__create_idea_evaluations_table.sql. Verify: `mvn -f backend/pom.xml -DskipTests flyway:migrate` creates `idea_evaluations` table with FK constraints.
- [X] T003 [P] [Phase 1] Add Flyway migration for idea attachments in backend/src/main/resources/db/migration/V6__create_idea_attachments_table.sql. Verify: `mvn -f backend/pom.xml -DskipTests flyway:migrate` creates `idea_attachments` table with unique idea_id.
- [X] T004 [P] [Phase 1] Create Idea entity in backend/src/main/java/com/innovatepam/idea/model/Idea.java with JPA annotations and status enum. Verify: `mvn -f backend/pom.xml -DskipTests compile` succeeds.
- [X] T005 [P] [Phase 1] Create IdeaEvaluation entity in backend/src/main/java/com/innovatepam/idea/model/IdeaEvaluation.java with JPA annotations. Verify: `mvn -f backend/pom.xml -DskipTests compile` succeeds.
- [X] T006 [P] [Phase 1] Create IdeaAttachment entity in backend/src/main/java/com/innovatepam/idea/model/IdeaAttachment.java with JPA annotations and FileType enum. Verify: `mvn -f backend/pom.xml -DskipTests compile` succeeds.
- [X] T007 [P] [Phase 1] Create repositories in backend/src/main/java/com/innovatepam/idea/repository/IdeaRepository.java, IdeaEvaluationRepository.java, IdeaAttachmentRepository.java. Verify: Spring context loads with repositories discovered (backend starts).
- [X] T008 [Phase 1] Add model unit tests in backend/src/test/java/com/innovatepam/idea/model/IdeaTest.java and IdeaEvaluationTest.java. Verify: `mvn -f backend/pom.xml -Dtest=IdeaTest,IdeaEvaluationTest test` passes.

---

## Phase 2: Backend Logic

**Goal**: Services for file storage, idea lifecycle, evaluation workflow

- [X] T009 [P] [Phase 2] Implement FileStorageService in backend/src/main/java/com/innovatepam/idea/service/FileStorageService.java with validation (PDF/PNG, size limit) and local storage. Verify: unit tests confirm rejection of invalid type/size and successful save.
- [X] T010 [P] [Phase 2] Implement IdeaService in backend/src/main/java/com/innovatepam/idea/service/IdeaService.java (create idea, list, detail, status transition). Verify: unit tests cover valid transitions and invalid transitions.
- [X] T011 [P] [Phase 2] Implement IdeaEvaluationService in backend/src/main/java/com/innovatepam/idea/service/IdeaEvaluationService.java (add comment, fetch history). Verify: unit tests confirm comment creation and ordering by createdAt.
- [X] T012 [Phase 2] Add validators in backend/src/main/java/com/innovatepam/idea/util/IdeaStatusValidator.java and FileValidator.java. Verify: unit tests in backend/src/test/java/com/innovatepam/idea/util/FileValidatorTest.java pass.
- [X] T013 [Phase 2] Add custom exceptions in backend/src/main/java/com/innovatepam/idea/exception/ (IdeaNotFoundException, InvalidStatusTransitionException, InvalidFileException, UnauthorizedAccessException). Verify: unit tests assert thrown exceptions are mapped in controller advice (Phase 3).

---

## Phase 3: API & Security

**Goal**: REST endpoints, DTOs, role enforcement via @PreAuthorize

- [X] T014 [P] [Phase 3] Create DTOs in backend/src/main/java/com/innovatepam/idea/dto/ (CreateIdeaRequest, UpdateIdeaStatusRequest, IdeaResponse, IdeaDetailResponse, IdeaEvaluationResponse, FileMetadataResponse). Verify: `mvn -f backend/pom.xml -DskipTests compile` succeeds.
- [X] T015 [Phase 3] Implement IdeaController in backend/src/main/java/com/innovatepam/idea/controller/IdeaController.java with endpoints for create, list, detail, download attachment. Verify: `mvn -f backend/pom.xml -Dtest=IdeaControllerTest test` passes.
- [X] T016 [Phase 3] Implement IdeaEvaluationController in backend/src/main/java/com/innovatepam/idea/controller/IdeaEvaluationController.java for status updates and comments. Verify: `mvn -f backend/pom.xml -Dtest=IdeaEvaluationControllerTest test` passes.
- [X] T017 [Phase 3] Add @PreAuthorize annotations for submitter/evaluator/admin roles on controller methods. Verify: security tests with Spring Security Test validate 403 for unauthorized roles.
- [X] T018 [Phase 3] Implement global exception handler in backend/src/main/java/com/innovatepam/idea/exception/IdeaExceptionHandler.java. Verify: integration test asserts 400/403/404/409 responses match contract.
- [X] T019 [Phase 3] Add multipart config in backend/src/main/resources/application.yml for file size limits and upload directory. Verify: uploading > 50MB triggers 413 in integration test.

---

## Phase 4: Frontend Core

**Goal**: API services, hooks, file upload logic

- [X] T020 [P] [Phase 4] Create API client in frontend/src/services/ideaService.js using Axios with multipart support. Verify: MSW tests in frontend/src/test/ideaService.test.js pass.
- [X] T021 [P] [Phase 4] Add file upload utilities in frontend/src/utils/fileUploadUtils.js (type/size validation) and status helpers in frontend/src/utils/statusUtils.js. Verify: unit tests cover allowed/blocked types and transitions.
- [X] T022 [P] [Phase 4] Implement useIdeas hook in frontend/src/hooks/useIdeas.js for list/detail fetching and state management. Verify: hook tests confirm loading/error states and data mapping.
- [X] T023 [P] [Phase 4] Implement useFileUpload hook in frontend/src/hooks/useFileUpload.js for file state, validation, and progress. Verify: hook tests confirm rejection of invalid files.
- [X] T024 [Phase 4] Add MSW handlers in frontend/src/test/mocks/ideaHandlers.js matching contracts/API.md. Verify: MSW handlers respond to create/list/detail endpoints in tests.

---

## Phase 5: Frontend UI

**Goal**: Submission form, dashboard, detail view with evaluation

- [X] T025 [P] [Phase 5] Build submission form component in frontend/src/components/IdeaForm.jsx with Tailwind styling and validation. Verify: IdeaForm.test.jsx confirms required fields and successful submit flow.
- [X] T026 [P] [Phase 5] Build file upload component in frontend/src/components/FileUpload.jsx with validation messages. Verify: component test ensures invalid file type shows error.
- [X] T027 [P] [Phase 5] Build dashboard list in frontend/src/pages/IdeaListing.jsx with pagination and status display. Verify: IdeaListing.test.jsx renders ideas from mock API.
- [X] T028 [P] [Phase 5] Build detail view in frontend/src/pages/IdeaDetail.jsx showing description, attachment, evaluation history. Verify: component test renders evaluation list correctly.
- [X] T029 [Phase 5] Build evaluation panel in frontend/src/components/EvaluationPanel.jsx for status updates and comments. Verify: component test enforces comment required when rejecting.

---

## Phase 6: Verification

**Goal**: Unit + Integration tests (Testcontainers, MSW) and coverage >= 80%

- [X] T030 [Phase 6] Add backend unit tests for IdeaService and FileStorageService in backend/src/test/java/com/innovatepam/idea/service/. Verify: `mvn -f backend/pom.xml -Dtest=IdeaServiceTest,FileStorageServiceTest test` passes.
- [X] T031 [Phase 6] Add backend integration tests with Testcontainers in backend/src/test/java/com/innovatepam/idea/repository/IdeaRepositoryIntegrationTest.java and backend/src/test/java/com/innovatepam/idea/controller/IdeaControllerIntegrationTest.java. Verify: `mvn -f backend/pom.xml -Dtest=*IntegrationTest test` passes.
- [X] T032 [Phase 6] Add frontend component tests in frontend/src/test/IdeaForm.test.jsx and frontend/src/test/IdeaListing.test.jsx using MSW mocks. Verify: `npm --prefix frontend test -- --run` passes.
- [X] T033 [Phase 6] Add frontend API tests in frontend/src/test/ideaService.test.js using MSW. Verify: `npm --prefix frontend test -- --run` passes.
- [X] T034 [Phase 6] Validate coverage thresholds: backend `mvn -f backend/pom.xml test` meets JaCoCo 0.80 minimum; frontend `npm --prefix frontend test -- --coverage` meets 0.80. Verify: both coverage reports show >= 80%.
