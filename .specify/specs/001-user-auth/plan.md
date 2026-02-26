# Implementation Plan: User Authentication System

**Branch**: `001-user-auth` | **Date**: 2026-02-25 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `.specify/specs/001-user-auth/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/plan-template.md` for the execution workflow.

## Summary

Implement a secure user authentication system for the InnovatEPAM Portal enabling new users to register with email, password, and role selection (submitter or evaluator/admin), and existing users to authenticate and receive JWT tokens for session management. The system will use Spring Boot 3.x with Spring Security for the backend REST API, React with Vite for the frontend UI, PostgreSQL for persistent storage, and BCrypt for password hashing. All database access will follow the Repository pattern as mandated by the constitution, with comprehensive test coverage using JUnit 5/Mockito (backend) and Vitest/React Testing Library (frontend) to meet the 80% coverage requirement.

## Technical Context

**Language/Version**: Java 21 (backend), JavaScript ES2023+ (frontend via React 18+)  
**Primary Dependencies**: Spring Boot 3.x, Spring Security 6.x, Spring Data JPA, PostgreSQL driver, BCrypt, JWT (jjwt), Springdoc OpenAPI (backend); React 18+, Vite 5+, Tailwind CSS 3.x, Axios, React Router (frontend)  
**Storage**: PostgreSQL 15+ for user accounts, roles, and authentication metadata  
**Testing**: JUnit 5, Mockito, Spring Boot Test, Testcontainers (backend); Vitest, React Testing Library, MSW (Mock Service Worker) for API mocking (frontend)  
**Target Platform**: Linux/Windows/macOS server for backend; Modern web browsers (Chrome, Firefox, Safari, Edge) for frontend  
**Project Type**: Web application (RESTful API backend + SPA frontend)  
**Performance Goals**: Registration/login endpoints respond within 500ms at p95; JWT generation within 100ms; support 100 concurrent authentication requests  
**Constraints**: Authentication token expires in 24 hours; account lockout after 5 failed attempts within 15 minutes; passwords hashed with BCrypt cost factor 12; email uniqueness enforced at database level  
**Scale/Scope**: Initially designed for 1,000-10,000 users; approximately 5-10 database tables for auth domain; 4-6 REST endpoints; 3-5 frontend pages/components

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### Clean Code Principles
- Functions will be small (< 20 lines target) with descriptive names
- No magic strings/numbers: constants for token expiration, BCrypt rounds, lockout thresholds
- Comments document WHY for security decisions, not WHAT code does
- Service methods follow Single Responsibility at function level

### SOLID Principles
- **SRP**: Separate services for UserRegistration, UserAuthentication, TokenGeneration
- **OCP**: Strategy pattern for password validation rules (extensible without modification)
- **LSP**: Repository interfaces allow substitution of implementation (JPA, in-memory for tests)
- **ISP**: Narrow interfaces: UserRepository, RoleRepository, AuthenticationAttemptRepository
- **DIP**: Controllers depend on service interfaces, services depend on repository interfaces

### RESTful API Design
- `POST /api/v1/auth/register` - user registration (returns 201 Created)
- `POST /api/v1/auth/login` - user login (returns 200 OK with token)
- `GET /api/v1/auth/me` - get current user info (requires auth token)
- Resource-based URIs, proper HTTP verbs, semantic status codes
- Versioned API (`/api/v1/`) for future compatibility
- OpenAPI documentation via Springdoc

### Repository Pattern
- `UserRepository` interface extending JpaRepository
- `RoleRepository` interface for role lookups
- `AuthenticationAttemptRepository` for tracking failed login attempts
- Controllers NEVER directly access EntityManager or database
- Services orchestrate business logic, repositories handle data access

### Testing Pyramid & Coverage
- **Unit Tests (70%)**: Service layer business logic, validation, token generation
- **Integration Tests (20%)**: Repository tests with Testcontainers PostgreSQL, API endpoint tests with MockMvc
- **E2E Tests (10%)**: Full registration and login flows via frontend
- Target: >=80% line coverage measured by JaCoCo (backend), Vitest coverage (frontend)
- Tests written following TDD: write failing test -> implement -> refactor

### Security & Secrets Management
- JWT secret key stored in environment variable (`JWT_SECRET_KEY`)
- Database credentials in environment variables (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`)
- BCrypt automatically salts passwords (no hardcoded salt)
- `.env` files excluded from version control via `.gitignore`
- Input validation prevents SQL injection (parameterized queries via JPA)
- Spring Security handles CORS, CSRF protection for stateless JWT

### Version Control & Commit Discipline
- Conventional commits: `feat(auth): implement user registration endpoint`
- Atomic commits per feature slice (e.g., "add UserService", "add registration endpoint")
- Commit messages explain WHY: "Use BCrypt cost factor 12 per OWASP recommendations"
- PR references feature branch `001-user-auth`

**Status**: PASS - All gates satisfied. Proceeding to Phase 0 research.

## Project Structure

### Documentation (this feature)

```text
.specify/specs/001-user-auth/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
│   ├── auth-api.yaml    # OpenAPI spec for authentication endpoints
│   └── README.md       # API contract documentation
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
backend/
├── src/
│   ├── main/
│   │   ├── java/com/innovatepam/auth/
│   │   │   ├── controller/        # REST controllers (AuthController)
│   │   │   ├── service/           # Business logic (UserService, AuthService, TokenService)
│   │   │   ├── repository/        # JPA repositories (UserRepository, RoleRepository)
│   │   │   ├── model/             # JPA entities (User, Role, AuthenticationAttempt)
│   │   │   ├── dto/               # Request/Response DTOs (RegisterRequest, LoginRequest, AuthResponse)
│   │   │   ├── security/          # Security config, JWT utilities, BCrypt config
│   │   │   ├── exception/         # Custom exceptions (DuplicateEmailException, AccountLockedException)
│   │   │   └── AuthApplication.java
│   │   └── resources/
│   │       ├── application.yml    # Spring Boot configuration (uses env vars)
│   │       └── db/migration/      # Flyway SQL migrations
│   └── test/
│       ├── java/com/innovatepam/auth/
│       │   ├── unit/              # Unit tests (service layer, utilities)
│       │   ├── integration/       # Integration tests (repository, API endpoints)
│       │   └── e2e/               # End-to-end tests (full flows)
│       └── resources/
│           └── application-test.yml
├── pom.xml                         # Maven dependencies
└── Dockerfile

frontend/
├── src/
│   ├── components/                # React components
│   │   ├── auth/
│   │   │   ├── RegisterForm.jsx
│   │   │   ├── LoginForm.jsx
│   │   │   └── RoleSelector.jsx
│   │   └── common/
│   │       ├── Button.jsx
│   │       └── Input.jsx
│   ├── pages/
│   │   ├── RegisterPage.jsx
│   │   ├── LoginPage.jsx
│   │   └── DashboardPage.jsx
│   ├── services/
│   │   ├── authService.js         # API calls for registration/login
│   │   └── api.js                 # Axios instance with interceptors
│   ├── hooks/
│   │   └── useAuth.js             # Custom hook for auth state
│   ├── context/
│   │   └── AuthContext.jsx        # Auth context provider
│   ├── utils/
│   │   ├── validators.js          # Email, password validation
│   │   └── tokenStorage.js        # localStorage for JWT
│   ├── App.jsx
│   ├── main.jsx
│   └── index.css                   # Tailwind imports
├── tests/
│   ├── unit/                       # Component unit tests
│   ├── integration/                # API integration tests with MSW
│   └── e2e/                         # End-to-end tests (optional)
├── package.json
├── vite.config.js
├── tailwind.config.js
└── Dockerfile
```

**Structure Decision**: Web application architecture selected. Backend provides RESTful API using Spring Boot with layered architecture (controllers -> services -> repositories -> database). Frontend is a React SPA that consumes the backend API. Both backend and frontend have separate test suites aligned with the Testing Pyramid (unit, integration, e2e). Structure supports independent development and deployment of backend/frontend, with clear separation of concerns per SOLID principles.

## Post-Design Constitution Check (After Phase 1)

**Clean Code**: Data model and contracts avoid implementation detail leakage; DTOs and entities kept focused.  
**SOLID**: Repository interfaces isolate persistence; service boundaries remain explicit.  
**RESTful API**: Contracts define resource-based endpoints with correct HTTP methods and status codes.  
**Repository Pattern**: Data model and contract plan assume all DB access through repositories.  
**Testing Pyramid**: Quickstart includes unit/integration/e2e steps and >=80% coverage targets.  
**Security**: Research confirms BCrypt hashing, JWT secrets via env vars, and lockout policy.  

**Status**: PASS - No violations introduced in Phase 1 artifacts.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

**Status**: PASS - No violations. All constitution principles are followed.

- Clean Code: Enforced via code review and SonarLint  
- SOLID: Layered architecture with dependency injection
- RESTful API: Standard Spring Boot REST conventions
- Repository Pattern: Spring Data JPA repositories used exclusively
- Testing: JUnit 5 + Mockito (backend), Vitest (frontend) with >=80% coverage target
- Security: Environment variables for secrets, BCrypt for passwords, JWT for tokens
- Version Control: Conventional commits enforced
