# Authentication API Contracts

**Feature**: 001-user-auth  
**API Version**: v1  
**Date**: 2026-02-25

## Overview

This directory contains the API contract specifications for the InnovatEPAM Authentication System. The API follows RESTful principles and uses JWT tokens for stateless authentication.

## Files

- **[auth-api.yaml](auth-api.yaml)**: OpenAPI 3.0.3 specification for all authentication endpoints

## Endpoints Summary

| Endpoint | Method | Auth Required | Purpose |
|----------|--------|---------------|---------|
| `/api/v1/auth/register` | POST | No | Register a new user account |
| `/api/v1/auth/login` | POST | No | Authenticate user and receive JWT token |
| `/api/v1/auth/me` | GET | Yes | Get current authenticated user information |

## Authentication Flow

### Registration Flow
```
1. User submits POST /api/v1/auth/register
   Body: { email, password, role }
   
2. Server validates input:
   - Email format and uniqueness
   - Password strength (≥8 chars, not common)
   - Role is 'submitter' or 'evaluator/admin'
   
3. Server hashes password with BCrypt (cost 12)
   
4. Server creates user record in database
   
5. Server generates JWT token (24h expiration)
   
6. Server returns 201 Created with token
   Response: { token, email, role, userId, expiresIn }
```

### Login Flow
```
1. User submits POST /api/v1/auth/login
   Body: { email, password }
   
2. Server checks if account is locked
   - If locked: return 403 Forbidden with lockout expiration time
   
3. Server verifies email exists
   - If not found: track failed attempt, return 401 Unauthorized
   
4. Server verifies password (BCrypt comparison)
   - If mismatch: track failed attempt, check lockout threshold, return 401
   
5. Server checks failed attempts in last 15 minutes
   - If ≥5 failures: lock account for 30 minutes, return 403 Forbidden
   
6. Login successful: generate JWT token (24h expiration)
   
7. Server returns 200 OK with token
   Response: { token, email, role, userId, expiresIn }
```

### JWT Token Structure
```json
{
  "sub": "user@example.com",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "role": "submitter",
  "iat": 1708876800,
  "exp": 1708963200
}
```

## Request/Response Examples

### Register User (Success)

**Request:**
```http
POST /api/v1/auth/register HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "email": "john.doe@example.com",
  "password": "SecureP@ss123",
  "role": "submitter"
}
```

**Response:**
```http
HTTP/1.1 201 Created
Content-Type: application/json

{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "email": "john.doe@example.com",
  "role": "submitter",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "expiresIn": 86400
}
```

### Register User (Email Already Exists)

**Request:**
```http
POST /api/v1/auth/register HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "email": "existing@example.com",
  "password": "SecureP@ss123",
  "role": "submitter"
}
```

**Response:**
```http
HTTP/1.1 409 Conflict
Content-Type: application/json

{
  "status": 409,
  "message": "Email already registered",
  "timestamp": "2026-02-25T10:30:00Z",
  "path": "/api/v1/auth/register"
}
```

### Login (Success)

**Request:**
```http
POST /api/v1/auth/login HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "email": "john.doe@example.com",
  "password": "SecureP@ss123"
}
```

**Response:**
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "email": "john.doe@example.com",
  "role": "submitter",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "expiresIn": 86400
}
```

### Login (Account Locked)

**Request:**
```http
POST /api/v1/auth/login HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "email": "john.doe@example.com",
  "password": "WrongPassword"
}
```

**Response (after 5 failed attempts):**
```http
HTTP/1.1 403 Forbidden
Content-Type: application/json

{
  "status": 403,
  "message": "Account locked due to too many failed attempts. Try again after 2026-02-25T11:00:00Z",
  "timestamp": "2026-02-25T10:30:00Z",
  "path": "/api/v1/auth/login"
}
```

### Get Current User (Authorized)

**Request:**
```http
GET /api/v1/auth/me HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response:**
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "john.doe@example.com",
  "role": "submitter",
  "createdAt": "2026-02-20T08:15:30Z"
}
```

## Status Codes

| Code | Meaning | When Used |
|------|---------|-----------|
| 200 | OK | Successful login or data retrieval |
| 201 | Created | User successfully registered |
| 400 | Bad Request | Invalid input (malformed email, weak password, invalid role) |
| 401 | Unauthorized | Invalid credentials or missing/expired token |
| 403 | Forbidden | Account locked due to failed attempts |
| 409 | Conflict | Email already registered |
| 500 | Internal Server Error | Unexpected server error |

## Security Considerations

### Password Security
- Passwords hashed with BCrypt (cost factor 12)
- Plain text passwords never stored or logged
- Minimum 8 characters enforced
- Common passwords rejected (top 10,000 list)

### Token Security
- JWT signed with HS256 algorithm
- Secret key stored in environment variable (never in code)
- Token expires after 24 hours
- Token includes minimal claims (sub, userId, role, iat, exp)
- Token validated on every protected endpoint request

### Rate Limiting
- Failed login attempts tracked per email
- 5 failed attempts within 15 minutes triggers lockout
- Account locked for 30 minutes
- Lockout automatically expires after 30 minutes

### Input Validation
- Email format validated (RFC 5322 compliant)
- Email uniqueness enforced at database level
- Role must be exact match ('submitter' or 'evaluator/admin')
- All inputs sanitized to prevent injection attacks

### HTTPS Requirement
- **Production**: HTTPS required for all endpoints
- **Development**: HTTP acceptable on localhost only

## Error Handling

All errors follow a standard format:
```json
{
  "status": 400,
  "message": "Human-readable error description",
  "timestamp": "2026-02-25T10:30:00Z",
  "path": "/api/v1/auth/register"
}
```

## Testing the API

### Using cURL

```bash
# Register a new user
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"SecurePass123","role":"submitter"}'

# Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"SecurePass123"}'

# Get current user (replace <TOKEN> with actual token from login response)
curl -X GET http://localhost:8080/api/v1/auth/me \
  -H "Authorization: Bearer <TOKEN>"
```

### Using Swagger UI

Once the backend is running, access interactive API documentation at:
```
http://localhost:8080/swagger-ui.html
```

## Versioning

This API follows semantic versioning in the URL path:
- Current version: `/api/v1`
- Breaking changes will increment version: `/api/v2`
- Non-breaking changes (new optional fields, new endpoints) remain in v1

## Contact

For questions or issues with the API contract:
- **Feature Spec**: [../spec.md](../spec.md)
- **Implementation Plan**: [../plan.md](../plan.md)
- **Data Model**: [../data-model.md](../data-model.md)

---

**Generated**: 2026-02-25  
**Status**: ✅ Contract complete and ready for implementation
