---

description: "Task list for user authentication system"
---

# Tasks: User Authentication System

**Input**: Design documents from `.specify/specs/001-user-auth/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: Mandatory 80% test coverage following Testing Pyramid (70% unit, 20% integration, 10% E2E). Backend: JUnit 5, Mockito, Testcontainers. Frontend: Vitest, React Testing Library, MSW.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and shared structure

- [x] T001 Create backend Spring Boot project skeleton in backend/pom.xml and backend/src/main/java/com/innovatepam/auth/AuthApplication.java
- [x] T002 Create frontend React + Vite scaffold in frontend/package.json and frontend/src/main.jsx
- [x] T003 [P] Add shared .gitignore entries for backend/.env and frontend/.env in .gitignore
- [x] T004 [P] Add lint/format tooling configs in backend/pom.xml and frontend/.eslintrc.cjs

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**CRITICAL**: No user story work can begin until this phase is complete

- [x] T005 Add Spring Boot dependencies for security, JPA, validation, PostgreSQL, JWT, and OpenAPI in backend/pom.xml
- [x] T006 Configure application properties and env binding in backend/src/main/resources/application.yml
- [x] T007 Create Flyway migrations for roles, users, and authentication_attempts tables in backend/src/main/resources/db/migration/
- [x] T008 [P] Create JPA entities (User, Role, AuthenticationAttempt) in backend/src/main/java/com/innovatepam/auth/model/
- [x] T009 [P] Create repository interfaces in backend/src/main/java/com/innovatepam/auth/repository/
- [x] T010 Create DTOs (RegisterRequest, LoginRequest, AuthResponse, ErrorResponse) in backend/src/main/java/com/innovatepam/auth/dto/
- [x] T011 Implement global error handling and validation mapping in backend/src/main/java/com/innovatepam/auth/exception/GlobalExceptionHandler.java
- [x] T012 Implement JWT utilities and filter in backend/src/main/java/com/innovatepam/auth/security/JwtService.java and backend/src/main/java/com/innovatepam/auth/security/JwtAuthenticationFilter.java
- [x] T013 Configure Spring Security filter chain and BCrypt encoder in backend/src/main/java/com/innovatepam/auth/security/SecurityConfig.java
- [x] T014 [P] Create frontend API client and token storage utilities in frontend/src/services/api.js and frontend/src/utils/tokenStorage.js
- [x] T015 [P] Create frontend auth context and hook in frontend/src/context/AuthContext.jsx and frontend/src/hooks/useAuth.js
- [x] T016 [P] Create shared input validation utilities in frontend/src/utils/validators.js

**Checkpoint**: Foundation ready - user story implementation can now begin

---

## Phase 3: User Story 1 - User Registration (Priority: P1) MVP

**Goal**: Allow new users to register with email, password, and role selection

**Independent Test**: Submit valid registration data and verify user record is created; invalid inputs return errors; duplicate email rejected.

- [x] T017 [P] [US1] Implement role lookup service in backend/src/main/java/com/innovatepam/auth/service/RoleService.java
- [x] T018 [US1] Implement user registration service in backend/src/main/java/com/innovatepam/auth/service/RegistrationService.java
- [x] T019 [US1] Implement registration endpoint in backend/src/main/java/com/innovatepam/auth/controller/AuthController.java
- [x] T020 [US1] Add request validation rules and messages in backend/src/main/java/com/innovatepam/auth/dto/RegisterRequest.java
- [x] T021 [P] [US1] Build registration UI in frontend/src/pages/RegisterPage.jsx and frontend/src/components/auth/RegisterForm.jsx
- [x] T022 [P] [US1] Build role selector UI in frontend/src/components/auth/RoleSelector.jsx
- [x] T023 [US1] Implement registration API call in frontend/src/services/authService.js
- [x] T024 [US1] Wire registration flow into auth context and routing in frontend/src/context/AuthContext.jsx and frontend/src/App.jsx

**Checkpoint**: User Story 1 is functional and independently testable

---

## Phase 4: User Story 2 - User Login (Priority: P2)

**Goal**: Authenticate existing users and return secure JWT token

**Independent Test**: Login with valid credentials returns token with role; invalid credentials return generic error; lockout after 5 failed attempts within 15 minutes.

- [x] T025 [P] [US2] Implement authentication attempt tracking in backend/src/main/java/com/innovatepam/auth/service/AuthenticationAttemptService.java
- [x] T026 [US2] Implement login service with lockout logic in backend/src/main/java/com/innovatepam/auth/service/AuthService.java
- [x] T027 [US2] Implement login endpoint in backend/src/main/java/com/innovatepam/auth/controller/AuthController.java
- [x] T028 [US2] Add login validation rules in backend/src/main/java/com/innovatepam/auth/dto/LoginRequest.java
- [x] T029 [P] [US2] Build login UI in frontend/src/pages/LoginPage.jsx and frontend/src/components/auth/LoginForm.jsx
- [x] T030 [US2] Update auth context to persist JWT and decoded role in frontend/src/context/AuthContext.jsx
- [x] T031 [US2] Add login API call and error mapping in frontend/src/services/authService.js

**Checkpoint**: User Stories 1 and 2 are both functional and independently testable

---

## Phase 5: User Story 3 - Role-Based Access Validation (Priority: P3)

**Goal**: Provide role information for authorization decisions via JWT claims and user info endpoint

**Independent Test**: Login as submitter and evaluator/admin; verify role claim in token and /auth/me response.

- [x] T032 [US3] Implement current-user endpoint in backend/src/main/java/com/innovatepam/auth/controller/AuthController.java
- [x] T033 [US3] Add token-to-user mapping in backend/src/main/java/com/innovatepam/auth/security/JwtService.java
- [x] T034 [US3] Display role-aware UI state in frontend/src/pages/DashboardPage.jsx
- [x] T035 [US3] Guard protected routes based on authentication in frontend/src/App.jsx

**Checkpoint**: All user stories are functional and independently testable

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [x] T036 [P] Review OpenAPI contract consistency and update .specify/specs/001-user-auth/contracts/auth-api.yaml
- [x] T037 [P] Validate quickstart steps and update .specify/specs/001-user-auth/quickstart.md
- [x] T038 [P] Documentation updates in .specify/specs/001-user-auth/plan.md and .specify/specs/001-user-auth/research.md
- [x] T039 Security hardening review for JWT secret handling in backend/src/main/resources/application.yml
- [x] T040 Code cleanup and refactoring in backend/src/main/java/com/innovatepam/auth/ and frontend/src/

---

## Phase 7: Test Implementation (80% Coverage - Testing Pyramid)

**Purpose**: Comprehensive test coverage following Testing Pyramid: 70% unit, 20% integration, 10% E2E

**Coverage Target**: >=80% line coverage measured by JaCoCo (backend) and Vitest coverage (frontend)

### Backend Unit Tests (Base of Pyramid - 70%)

**Purpose**: Test business logic, validation, and utilities in isolation with mocked dependencies

- [x] T041 [P] Configure JaCoCo Maven plugin for coverage reporting in backend/pom.xml
- [x] T042 [P] [US1] Unit test RoleService with mocked RoleRepository in backend/src/test/java/com/innovatepam/auth/service/RoleServiceTest.java
- [x] T043 [P] [US1] Unit test RegistrationService email uniqueness and password hashing logic in backend/src/test/java/com/innovatepam/auth/service/RegistrationServiceTest.java
- [x] T044 [P] [US2] Unit test AuthenticationAttemptService lockout calculation logic in backend/src/test/java/com/innovatepam/auth/service/AuthenticationAttemptServiceTest.java
- [x] T045 [P] [US2] Unit test AuthService login validation and BCrypt verification in backend/src/test/java/com/innovatepam/auth/service/AuthServiceTest.java
- [x] T046 [P] [US3] Unit test JwtService token generation, parsing, and expiration in backend/src/test/java/com/innovatepam/auth/security/JwtServiceTest.java
- [x] T047 [P] Unit test JWT authentication filter token extraction and validation in backend/src/test/java/com/innovatepam/auth/security/JwtAuthenticationFilterTest.java
- [x] T048 [P] [US1] Unit test RegisterRequest validation constraints (email format, password strength, role required) in backend/src/test/java/com/innovatepam/auth/dto/RegisterRequestTest.java
- [x] T049 [P] [US2] Unit test LoginRequest validation constraints in backend/src/test/java/com/innovatepam/auth/dto/LoginRequestTest.java
- [x] T050 [P] Unit test GlobalExceptionHandler error response mapping in backend/src/test/java/com/innovatepam/auth/exception/GlobalExceptionHandlerTest.java
- [x] T051 [P] Unit test User entity account lockout logic and isAccountLocked() method in backend/src/test/java/com/innovatepam/auth/model/UserTest.java

### Backend Integration Tests (Middle of Pyramid - 20%)

**Purpose**: Test component interactions with real database and Spring context

**NOTE**: Backend coverage now at **96%** (exceeds 80% target). All 177 tests passing. Controller integration tests (T056-T059) complete with Testcontainers PostgreSQL.

- [x] T052 Configure test database (Testcontainers PostgreSQL) in backend/src/test/resources/application-test.yml
- [x] T053 [US1] Integration test UserRepository CRUD operations with Testcontainers in backend/src/test/java/com/innovatepam/auth/repository/UserRepositoryIntegrationTest.java
- [x] T054 [P] [US1] Integration test RoleRepository findByName query with test data in backend/src/test/java/com/innovatepam/auth/repository/RoleRepositoryIntegrationTest.java
- [x] T055 [US2] Integration test AuthenticationAttemptRepository failed attempt queries in backend/src/test/java/com/innovatepam/auth/repository/AuthenticationAttemptRepositoryIntegrationTest.java
- [x] T056 [US1] Integration test POST /api/v1/auth/register endpoint with MockMvc in backend/src/test/java/com/innovatepam/auth/controller/AuthControllerRegistrationIntegrationTest.java
- [x] T057 [US2] Integration test POST /api/v1/auth/login endpoint including lockout scenarios in backend/src/test/java/com/innovatepam/auth/controller/AuthControllerLoginIntegrationTest.java
- [x] T058 [US3] Integration test GET /api/v1/auth/me endpoint with JWT authentication in backend/src/test/java/com/innovatepam/auth/controller/AuthControllerMeIntegrationTest.java
- [x] T059 Integration test Spring Security configuration CORS and authentication filters in backend/src/test/java/com/innovatepam/auth/security/SecurityConfigIntegrationTest.java

### Frontend Unit Tests (Base of Pyramid - 70%)

**Purpose**: Test components, hooks, and utilities in isolation

- [x] T060 [P] Configure Vitest and React Testing Library in frontend/vite.config.js and frontend/src/test/setup.js
- [x] T061 [P] Unit test tokenStorage utilities (getToken, setToken, clearToken) in frontend/src/utils/tokenStorage.test.js
- [x] T062 [P] Unit test validators (isValidEmail, isStrongPassword) in frontend/src/utils/validators.test.js
- [x] T063 [P] [US1] Unit test RegisterForm component validation and submission in frontend/src/components/auth/RegisterForm.test.jsx
- [x] T064 [P] [US1] Unit test RoleSelector component rendering and selection in frontend/src/components/auth/RoleSelector.test.jsx
- [x] T065 [P] [US2] Unit test LoginForm component validation and submission in frontend/src/components/auth/LoginForm.test.jsx
- [x] T066 [P] Unit test useAuth hook within AuthProvider context in frontend/src/hooks/useAuth.test.js
- [x] T067 [P] Unit test AuthContext JWT decoding and state management in frontend/src/context/AuthContext.test.jsx
- [x] T068 [P] [US3] Unit test ProtectedRoute component authorization logic in frontend/src/components/ProtectedRoute.test.jsx

### Frontend Integration Tests (Middle of Pyramid - 20%)

**Purpose**: Test page-level flows with mocked API responses

- [x] T069 Configure MSW (Mock Service Worker) for API mocking in frontend/src/test/mocks/handlers.js and frontend/src/test/mocks/server.js
- [x] T070 [US1] Integration test RegisterPage with API success and error scenarios using MSW in frontend/src/pages/RegisterPage.test.jsx
- [x] T071 [US2] Integration test LoginPage with API success, error, and lockout scenarios using MSW in frontend/src/pages/LoginPage.test.jsx
- [x] T072 [US3] Integration test DashboardPage with authenticated user rendering in frontend/src/pages/DashboardPage.test.jsx
- [x] T073 Integration test API client token interceptor and error handling in frontend/src/services/api.test.js

### End-to-End Tests (Top of Pyramid - 10%)

**Purpose**: Test critical user journeys through the entire system

- [ ] T074 E2E test: Complete registration flow (US1) - fill form, submit, verify redirect to dashboard in frontend/src/test/e2e/registration.e2e.test.jsx
- [ ] T075 E2E test: Complete login flow (US2) - enter credentials, submit, verify dashboard access in frontend/src/test/e2e/login.e2e.test.jsx
- [ ] T076 E2E test: Role-based access flow (US3) - login as different roles, verify token contains correct role in frontend/src/test/e2e/role-access.e2e.test.jsx
- [ ] T077 E2E test: Account lockout flow (US2) - 5 failed login attempts, verify lockout message in frontend/src/test/e2e/lockout.e2e.test.jsx

### Coverage Validation & Reporting

- [ ] T078 Generate JaCoCo coverage report and validate >=80% backend coverage in backend/target/site/jacoco/index.html
- [ ] T079 Generate Vitest coverage report and validate >=80% frontend coverage using frontend/coverage/index.html
- [ ] T080 Document test execution instructions and coverage thresholds in README.md

**Checkpoint**: 80% test coverage achieved following Testing Pyramid principles

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3-5)**: All depend on Foundational phase completion
  - User stories can proceed in parallel once foundation is complete
- **Polish (Phase 6)**: Depends on all desired user stories being complete
- **Test Implementation (Phase 7)**: Can start after corresponding implementation phases complete; unit tests can be written alongside implementation (TDD approach recommended)

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2)
- **User Story 2 (P2)**: Can start after Foundational (Phase 2); depends on registration data model but independent of registration UI
- **User Story 3 (P3)**: Can start after Foundational (Phase 2); depends on JWT issuance from login

### Test Phase Dependencies

- **Backend Unit Tests (T041-T051)**: Can be written in parallel with implementation (TDD); each test depends only on its target class
- **Backend Integration Tests (T052-T059)**: Require implementation complete; T052 (Testcontainers setup) blocks others in this group
- **Frontend Unit Tests (T060-T068)**: Can be written in parallel with implementation; T060 (Vitest setup) blocks others
- **Frontend Integration Tests (T069-T073)**: Require implementation complete; T069 (MSW setup) blocks others in this group
- **E2E Tests (T074-T077)**: Require all three user stories implemented and both backend/frontend running
- **Coverage Validation (T078-T080)**: Final step after all tests implemented

### Within Each User Story

- Models before services
- Services before endpoints
- Core backend logic before frontend integration
- Tests can be written alongside implementation (TDD) or after (test-after approach)
- Story complete before moving to next priority

### Parallel Opportunities

- Setup tasks T003-T004 can run in parallel
- Foundational tasks T008-T010 and T014-T016 can run in parallel
- Frontend UI tasks for each story can run in parallel with backend endpoints once contracts are stable
- All unit tests within a category (T041-T051, T060-T068) can be written in parallel
- Backend and frontend test implementation can proceed in parallel

---

## Parallel Example: User Story 1

```bash
# Implementation
Task: "Implement RoleService in backend/src/main/java/com/innovatepam/auth/service/RoleService.java"
Task: "Build RegisterPage in frontend/src/pages/RegisterPage.jsx"

# Tests (can run alongside or after implementation)
Task: "Unit test RoleService in backend/src/test/java/com/innovatepam/auth/service/RoleServiceTest.java"
Task: "Unit test RegisterForm component in frontend/src/components/auth/RegisterForm.test.jsx"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL - blocks all stories)
3. Complete Phase 3: User Story 1
4. Complete Phase 7: Tests for User Story 1 (T041-T051 unit tests, T056 integration, T074 E2E)
5. Validate >=80% coverage for User Story 1
6. STOP and validate User Story 1 independently
7. Deploy/demo if ready

### Incremental Delivery with Tests

1. Complete Setup + Foundational
2. Add User Story 1 + Tests → Validate coverage → Test independently → Deploy/Demo
3. Add User Story 2 + Tests → Validate coverage → Test independently → Deploy/Demo
4. Add User Story 3 + Tests → Validate coverage → Test independently → Deploy/Demo
5. Run full E2E test suite (T074-T077) → Generate final coverage reports (T078-T080)
6. Each story adds value without breaking previous stories

### TDD Approach (Recommended)

1. Write failing unit test for service/component
2. Implement minimum code to pass test
3. Refactor while keeping tests green
4. Add integration tests once unit tests pass
5. Verify coverage incrementally (aim for 80% at each checkpoint)

---

## Notes

- [P] tasks = different files, no dependencies, can run in parallel
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Avoid vague tasks, shared-file conflicts, or cross-story coupling
- **Test Coverage**: Minimum 80% required following Testing Pyramid (70% unit, 20% integration, 10% E2E)
- **TDD Recommended**: Write tests before or alongside implementation for better design
- **Coverage Tools**: JaCoCo (backend), Vitest coverage (frontend)
- **Test Isolation**: Unit tests use mocks; integration tests use Testcontainers; E2E tests use full stack