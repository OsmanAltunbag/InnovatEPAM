# InnovatEPAM Constitution

## Core Principles

### I. Clean Architecture
Design follows clean architecture boundaries: domain and use cases are framework-agnostic; dependencies point inward; interfaces defined in the core and implemented at the edges.

### II. TypeScript Strict Mode
TypeScript `strict` mode is required; any exceptions must be documented with rationale and minimized via targeted suppression only.

### III. JWT Authentication
Authentication uses JWT with well-defined token lifetimes, rotation or refresh strategy, and validation of issuer, audience, and signature.

### IV. Repository Pattern
Data access is abstracted behind repositories; business logic depends on repository interfaces, not concrete storage technologies.

### V. Business Logic Test Coverage
Maintain at least 80% test coverage for business logic (domain + use cases), measured in CI and enforced as a quality gate.

### VI. No Hardcoded Secrets
Secrets never appear in source code, configs committed to VCS, or test fixtures; use environment variables or secret managers with local development fallbacks.

## Additional Constraints

- Clean architecture boundaries must be reflected in folder structure and import rules.
- JWT signing keys and related secrets must be provided via secure configuration at runtime.
- Repository implementations may live in infrastructure modules only; domain stays persistence-agnostic.

## Quality Gates

- TypeScript compiler runs with `strict` enabled in CI.
- Business logic coverage must remain at or above 80%.
- Security checks include secret scanning and dependency vulnerability reporting.

## Governance

- This constitution supersedes local practices and templates.
- Amendments require a documented rationale, migration plan, and approval by maintainers.

**Version**: 1.0.0 | **Ratified**: 2026-02-24 | **Last Amended**: 2026-02-24
