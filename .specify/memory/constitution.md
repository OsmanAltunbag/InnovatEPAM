<!--
Sync Impact Report
==================
Version change: TEMPLATE → 1.0.0
Modified principles:
  - All principles defined from template placeholders
Added sections:
  - I. Clean Code Principles
  - II. SOLID Principles (Strict)
  - III. RESTful API Design
  - IV. Repository Pattern for Data Access
  - V. Testing Pyramid & Coverage (NON-NEGOTIABLE)
  - VI. Security & Secrets Management
  - VII. Version Control & Commit Discipline
  - Architecture Standards
  - Code Quality & Review Process
Templates requiring updates:
  ✅ plan-template.md (Constitution Check section aligns with new principles)
  ✅ spec-template.md (User stories and acceptance scenarios support testability principle)
  ✅ tasks-template.md (Task structure supports independent testing and incremental delivery)
Follow-up TODOs: None
-->

# InnovatEPAM Constitution

## Core Principles

### I. Clean Code Principles

**MUST** write code that is self-documenting, readable, and maintainable:
- Functions MUST do one thing and do it well (Single Responsibility at function level)
- Names MUST be descriptive and reveal intent (no abbreviations except standard domain terms)
- Functions MUST be small (ideally < 20 lines) with minimal parameters (≤ 3 preferred)
- Comments explain WHY, not WHAT (code explains what it does)
- No magic numbers or strings; use named constants
- DRY principle: eliminate duplication through abstraction
- Boy Scout Rule: leave code cleaner than you found it

**Rationale**: Clean code reduces cognitive load, accelerates onboarding, minimizes bugs, and lowers maintenance costs over the project lifecycle.

### II. SOLID Principles (Strict)

**MUST** adhere to all five SOLID principles in every design decision:
- **Single Responsibility**: Each class/module has exactly one reason to change
- **Open/Closed**: Entities are open for extension, closed for modification (use inheritance, composition, dependency injection)
- **Liskov Substitution**: Subtypes must be substitutable for their base types without breaking behavior
- **Interface Segregation**: No client depends on methods it doesn't use; prefer small, focused interfaces
- **Dependency Inversion**: Depend on abstractions (interfaces), not concrete implementations

**Rationale**: SOLID principles ensure systems remain flexible, testable, and resilient to change. Violations create technical debt that compounds exponentially.

### III. RESTful API Design

**MUST** follow RESTful conventions and HTTP semantics strictly:
- Resources identified by nouns (not verbs): `/users`, `/orders`, not `/getUser`
- HTTP methods used correctly: GET (read), POST (create), PUT (replace), PATCH (update), DELETE (remove)
- Statelessness: no session state on server; authentication via tokens
- Status codes used semantically: 2xx (success), 4xx (client error), 5xx (server error)
- Resource relationships expressed via URI structure or HATEOAS links
- Versioning required for breaking changes: `/api/v1/`, `/api/v2/`
- Pagination, filtering, sorting via query parameters on collections

**Rationale**: RESTful design ensures APIs are predictable, cacheable, scalable, and easily consumable by diverse clients.

### IV. Repository Pattern for Data Access

**MUST** use the Repository pattern as the exclusive data access abstraction:
- All database queries encapsulated in repository interfaces and implementations
- Controllers/services NEVER directly call ORMs or database drivers
- One repository per aggregate root (follows DDD aggregates)
- Repositories return domain entities, not database records or DTOs
- Data access logic centralized for testability (mock repositories in unit tests)
- Transaction boundaries managed at service layer, repositories stay unaware

**Rationale**: Repository pattern decouples business logic from data access technology, enabling independent testing, technology swaps, and consistent query patterns.

### V. Testing Pyramid & Coverage (NON-NEGOTIABLE)

**MUST** achieve minimum 80% test coverage for all business logic using the Testing Pyramid approach:
- **Unit Tests** (70% of tests): Fast, isolated tests for individual functions/classes; mocked dependencies
- **Integration Tests** (20% of tests): Test interactions between components (e.g., repository + database, API + service layer)
- **End-to-End Tests** (10% of tests): Test complete user flows through UI or API

**Coverage Requirements**:
- Business logic: ≥ 80% line coverage (measured via coverage tools)
- Edge cases and error paths explicitly tested
- Tests written BEFORE implementation (TDD red-green-refactor cycle strongly encouraged)
- All tests MUST pass before merge; broken tests block deployment

**Rationale**: High test coverage prevents regressions, documents expected behavior, supports refactoring confidence, and ensures quality at scale. The pyramid shape optimizes speed and cost.

### VI. Security & Secrets Management

**MUST** never hardcode secrets or sensitive configuration:
- Secrets (API keys, passwords, tokens, certificates) stored in environment variables or secure vaults (e.g., Azure Key Vault, AWS Secrets Manager, HashiCorp Vault)
- Configuration files MUST NOT contain production secrets; use `.env` files excluded from version control
- All secrets rotated regularly and encrypted at rest
- Principle of least privilege: grant minimum necessary permissions
- Input validation and sanitization mandatory to prevent injection attacks
- Authentication and authorization enforced on all protected endpoints

**Rationale**: Hardcoded secrets lead to breaches, compliance violations, and catastrophic data loss. Proper secrets management is non-negotiable for production systems.

### VII. Version Control & Commit Discipline

**MUST** maintain meaningful, atomic commits with clear messages:
- Commit messages follow conventional commit format: `type(scope): description`
  - Types: `feat`, `fix`, `refactor`, `test`, `docs`, `chore`, `style`, `perf`
  - Example: `feat(auth): implement JWT token refresh mechanism`
- Each commit represents a single logical change (atomic)
- Commit descriptions explain WHY the change was made (context and motivation)
- No "WIP", "fix", "stuff" messages; commits tell a coherent story
- Pull requests reference related issues/tickets
- Branches follow naming convention: `###-feature-name` or `###-fix-description`

**Rationale**: Meaningful commits enable effective code review, simplify debugging via `git bisect`, document decision-making, and support rollbacks.

## Architecture Standards

**Layered Architecture**: MUST organize code into distinct layers with clear responsibilities:
- **Presentation Layer**: Controllers, API routes, request/response DTOs
- **Business Logic Layer**: Services, domain models, business rules
- **Data Access Layer**: Repositories, database context, migrations
- **Cross-Cutting Concerns**: Logging, exception handling, validation (via middleware or AOP)

**Dependency Flow**: Dependencies MUST flow inward (presentation → business logic → data access). Inner layers never depend on outer layers (Dependency Inversion).

**API Contracts**: All API endpoints MUST be documented with OpenAPI/Swagger specifications. Breaking changes require version increments.

**Error Handling**: Exceptions MUST be caught at appropriate boundaries and translated to meaningful error responses. Never expose stack traces to clients in production.

## Code Quality & Review Process

**Code Review Requirements**:
- All code changes require peer review before merge
- Reviewers MUST verify:
  - Compliance with all constitution principles
  - Test coverage meets 80% threshold
  - No hardcoded secrets or magic values
  - Clean code standards followed
  - SOLID principles not violated
  - RESTful conventions honored (for API changes)
  - Repository pattern used (for data access changes)
- At least one approval required; two approvals for architecture changes

**Quality Gates**:
- All tests pass (unit, integration, E2E)
- Linter and formatter checks pass
- Code coverage threshold met
- Security scanning passes (no critical vulnerabilities)
- Build succeeds on CI/CD pipeline

**Technical Debt**: Deviations from constitution principles MUST be documented as technical debt items with:
- Clear justification for why deviation was necessary
- Remediation plan with timeline
- Risk assessment and mitigation strategy

## Governance

**Authority**: This constitution supersedes all other coding practices, guidelines, or conventions. In case of conflict, constitution principles take precedence.

**Amendment Process**:
- Amendments require documentation of:
  - Rationale for change (problem being solved)
  - Impact analysis (affected code, templates, workflows)
  - Migration plan (if breaking existing practices)
- Version increment follows semantic versioning:
  - **MAJOR**: Removal or redefinition of core principles (breaking governance contract)
  - **MINOR**: Addition of new principles or sections (extends governance)
  - **PATCH**: Clarifications, wording improvements, non-semantic refinements
- All templates (plan, spec, tasks) MUST be updated to reflect constitution changes

**Compliance Review**:
- All pull requests MUST verify compliance via checklist or automated checks
- Quarterly architecture review to assess adherence and identify technical debt
- Complexity introduced without constitutional justification will be rejected

**Version**: 1.0.0 | **Ratified**: 2026-02-25 | **Last Amended**: 2026-02-25
