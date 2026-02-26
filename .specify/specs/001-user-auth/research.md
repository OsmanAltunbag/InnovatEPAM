# Research: User Authentication System

**Feature**: 001-user-auth  
**Date**: 2026-02-25  
**Status**: Complete

## Purpose

Capture technical decisions for the authentication system with rationale and alternatives. All unknowns from Technical Context are resolved.

---

## R1: Backend Security Model

**Decision**: Use Spring Security 6.x with stateless JWT authentication.

**Rationale**:
- Aligns with REST statelessness requirement from the constitution.
- Scales without server-side session storage.
- Integrates cleanly with Spring Boot 3.x.

**Alternatives considered**:
- Server-side sessions with Spring Session and Redis (rejected: stateful, added infra).
- OAuth2/OIDC provider integration (rejected: out of scope for MVP).

---

## R2: Password Hashing Algorithm

**Decision**: Use BCrypt with cost factor 12 via `BCryptPasswordEncoder`.

**Rationale**:
- OWASP recommended for password storage.
- Built-in salt per password.
- Cost factor 12 balances security and login latency.

**Alternatives considered**:
- Argon2 (rejected: added dependency complexity for MVP).
- PBKDF2 (rejected: weaker against GPU attacks).

---

## R3: JWT Library and Token Claims

**Decision**: Use JJWT with HS256 and 24-hour expiration; include userId and role claims.

**Rationale**:
- JJWT is mature and widely used for Java JWT handling.
- HS256 is sufficient for a single backend service.
- 24-hour expiry balances security with user experience.
- Role in claims enables authorization without extra DB queries.

**Alternatives considered**:
- RS256 (rejected: requires key management without clear benefit).
- Short-lived tokens with refresh flow (rejected: out of scope for MVP).

---

## R4: Persistence and Migrations

**Decision**: PostgreSQL 15+ with Flyway migrations.

**Rationale**:
- ACID compliance and relational model fit auth data.
- Flyway provides versioned schema changes aligned with version control discipline.
- Email uniqueness and relational integrity enforced at DB level.

**Alternatives considered**:
- MongoDB (rejected: relational integrity required).
- Manual SQL without migrations (rejected: error-prone, poor traceability).

---

## R5: Repository Pattern Implementation

**Decision**: Spring Data JPA repositories (`JpaRepository`) for all DB access.

**Rationale**:
- Constitution mandates Repository pattern.
- Interface-based repositories are easy to mock for unit tests.
- Reduces boilerplate and isolates data access.

**Alternatives considered**:
- JDBC Template (rejected: more boilerplate, less abstraction).
- Direct `EntityManager` usage (rejected: violates repository abstraction goal).

---

## R6: API Documentation

**Decision**: Springdoc OpenAPI 2.x auto-generated docs, exported to `contracts/auth-api.yaml`.

**Rationale**:
- Constitution requires API contracts.
- Auto-generated docs reduce drift from implementation.
- Swagger UI supports rapid testing during development.

**Alternatives considered**:
- Manually maintained OpenAPI YAML (rejected: higher drift risk).
- Swagger 2.x (rejected: outdated).

---

## R7: Frontend Stack

**Decision**: React 18 + Vite 5 + Tailwind CSS 3 + Axios.

**Rationale**:
- Fast dev and build tooling with Vite.
- Tailwind accelerates consistent UI styling.
- Axios interceptors simplify JWT header injection.

**Alternatives considered**:
- Create React App (rejected: slower builds and legacy toolchain).
- CSS Modules or Styled Components (rejected: slower iteration for MVP).

---

## R8: Frontend Auth State

**Decision**: React Context API for auth state; JWT stored in localStorage.

**Rationale**:
- Auth state is small and scoped; Context is sufficient.
- localStorage persists login across refresh.

**Alternatives considered**:
- Redux (rejected: unnecessary complexity for auth-only state).
- HTTP-only cookies (rejected for MVP; would require CSRF strategy).

---

## R9: Account Lockout Policy

**Decision**: Lock account after 5 failed attempts in 15 minutes; lock duration 30 minutes.

**Rationale**:
- Aligns with OWASP guidance for brute-force protection.
- Balances user experience with security.

**Alternatives considered**:
- IP-based rate limiting (rejected: shared IPs can block legitimate users).
- CAPTCHA after failures (rejected: adds UI complexity for MVP).

---

## R10: Testing Strategy

**Decision**: Testing Pyramid with >=80% coverage. Backend uses JUnit 5, Mockito, Testcontainers; frontend uses Vitest, React Testing Library, MSW.

**Rationale**:
- Constitution mandates Testing Pyramid and coverage threshold.
- Unit tests catch logic errors early; integration tests validate wiring; E2E verifies flows.

**Alternatives considered**:
- E2E-heavy strategy (rejected: slower feedback, higher cost).
- Unit-only strategy (rejected: misses integration issues).

---

## Summary

All technical decisions align with the InnovatEPAM Constitution: Clean Code, SOLID, RESTful API, Repository pattern, Testing Pyramid (>=80%), and strict secrets management. Phase 0 research is complete and ready for Phase 1 design artifacts.