# API Contracts: Evaluation Workflow

**Feature**: Evaluation Workflow  
**Date**: February 26, 2026  
**Version**: 1.0  
**Base URL**: `/api/v1`

---

## Overview

This document defines the HTTP API contracts for the Evaluation Workflow feature. All endpoints require JWT authentication. Role-based access control is enforced server-side via `@PreAuthorize` annotations.

### Authentication

All requests must include a valid JWT token in the Authorization header:
```
Authorization: Bearer <jwt_token>
```

### Common Error Responses

**401 Unauthorized** - Missing or invalid JWT token
```json
{
  "error": "UNAUTHORIZED",
  "message": "Authentication token is missing or invalid",
  "timestamp": "2026-02-26T14:30:00Z"
}
```

**403 Forbidden** - Insufficient permissions
```json
{
  "error": "FORBIDDEN",
  "message": "User does not have permission to perform this action",
  "timestamp": "2026-02-26T14:30:00Z"
}
```

**404 Not Found** - Resource not found
```json
{
  "error": "NOT_FOUND",
  "message": "Idea with ID 123 not found",
  "timestamp": "2026-02-26T14:30:00Z"
}
```

---

##  1. Update Idea Status

**Endpoint**: `PATCH /api/v1/ideas/{id}/status`

**Purpose**: Change an idea's status through the evaluation workflow with optional or required comment

**Authentication**: Required (JWT Bearer token)

**Authorization**: Role `EVALUATOR` or `ADMIN` only

**HTTP Method**: PATCH (partial update - only status field changes)

### Request

**Path Parameters**:
- `id` (Long, required): The ID of the idea to update

**Request Body** (`application/json`):
```json
{
  "newStatus": "ACCEPTED",
  "comment": "Excellent proposal with clear ROI. Approved for Phase 2 implementation."
}
```

**Request Fields**:
- `newStatus` (String, required): Target status. Must be valid transition from current status.
  - Allowed values: `"UNDER_REVIEW"`, `"ACCEPTED"`, `"REJECTED"`
- `comment` (String, conditional): Evaluation comment explaining the decision
  - Required when `newStatus` is `"ACCEPTED"` or `"REJECTED"`
  - Optional when `newStatus` is `"UNDER_REVIEW"`
  - Max length: 5000 characters
  - Min length: 1 character (if provided)

**Validation Rules**:
- Status transition must be valid per workflow (enforced by `IdeaStatusValidator`)
- Comment required for terminal states (ACCEPTED/REJECTED)
- Comment must not be empty string if provided
- Idea must exist
- User must have EVALUATOR or ADMIN role

### Response

**Success: 200 OK**

Returns updated idea summary:

```json
{
  "id": 123,
  "title": "Sustainable Packaging Initiative",
  "category": "Sustainability",
  "status": "ACCEPTED",
  "submitterName": "Alice Johnson",
  "submitterId": "550e8400-e29b-41d4-a716-446655440000",
  "createdAt": "2026-02-20T10:15:00Z",
  "updatedAt": "2026-02-26T14:30:00Z",
  "hasAttachment": true,
  "evaluationCount": 3
}
```

**Error: 400 Bad Request** (Validation failed)

```json
{
  "error": "VALIDATION_ERROR",
  "message": "Validation failed",
  "details": {
    "comment": "Comment is required when accepting or rejecting an idea",
    "newStatus": "Invalid status transition from ACCEPTED to SUBMITTED"
  },
  "timestamp": "2026-02-26T14:30:00Z"
}
```

**Error: 403 Forbidden** (Insufficient permissions)

```json
{
  "error": "INSUFFICIENT_PERMISSIONS",
  "message": "Only evaluators and admins can update idea status",
  "timestamp": "2026-02-26T14:30:00Z"
}
```

**Error: 404 Not Found** (Idea doesn't exist)

```json
{
  "error": "IDEA_NOT_FOUND",
  "message": "Idea with ID 123 not found",
  "timestamp": "2026-02-26T14:30:00Z"
}
```

### Example Requests

**Accept an idea**:
```bash
curl -X PATCH http://localhost:8080/api/v1/ideas/123/status \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..." \
  -H "Content-Type: application/json" \
  -d '{
    "newStatus": "ACCEPTED",
    "comment": "Great idea! Moving forward with implementation."
  }'
```

**Move to under review**:
```bash
curl -X PATCH http://localhost:8080/api/v1/ideas/123/status \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..." \
  -H "Content-Type: application/json" \
  -d '{
    "newStatus": "UNDER_REVIEW",
    "comment": "Reviewing feasibility and cost estimates."
  }'
```

**Reject an idea** (comment required):
```bash
curl -X PATCH http://localhost:8080/api/v1/ideas/123/status \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..." \
  -H "Content-Type: application/json" \
  -d '{
    "newStatus": "REJECTED",
    "comment": "Conflicts with current strategic priorities. Please resubmit next quarter."
  }'
```

---

## 2. Add Evaluation Comment

**Endpoint**: `POST /api/v1/ideas/{id}/comments`

**Purpose**: Add a standalone evaluation comment without changing status

**Authentication**: Required (JWT Bearer token)

**Authorization**: Role `EVALUATOR` or `ADMIN` only

**HTTP Method**: POST

### Request

**Path Parameters**:
- `id` (Long, required): The ID of the idea to comment on

**Request Body** (`application/json`):
```json
{
  "comment": "Please provide additional details on the implementation timeline."
}
```

**Request Fields**:
- `comment` (String, required): The evaluation comment text
  - Min length: 1 character
  - Max length: 5000 characters
  - Cannot be empty or whitespace only

**Validation Rules**:
- Comment must not be blank
- Comment length within limits
- Idea must exist
- User must have EVALUATOR or ADMIN role

### Response

**Success: 201 Created**

Returns the created evaluation:

```json
{
  "id": 456,
  "ideaId": 123,
  "evaluatorName": "Bob Smith",
  "evaluatorId": "660e8400-e29b-41d4-a716-446655440001",
  "comment": "Please provide additional details on the implementation timeline.",
  "statusSnapshot": null,
  "createdAt": "2026-02-26T14:35:00Z"
}
```

**Response Fields**:
- `id`: Unique evaluation ID
- `ideaId`: ID of the idea being evaluated
- `evaluatorName`: Full name of the evaluator
- `evaluatorId`: UUID of the evaluator user
- `comment`: The evaluation comment text
- `statusSnapshot`: Status change associated with this evaluation (null for standalone comments)
- `createdAt`: Timestamp when evaluation was recorded (ISO 8601 format)

**Error: 400 Bad Request** (Validation failed)

```json
{
  "error": "VALIDATION_ERROR",
  "message": "Validation failed",
  "details": {
    "comment": "Comment cannot be empty"
  },
  "timestamp": "2026-02-26T14:35:00Z"
}
```

**Error: 403 Forbidden** (Insufficient permissions)

```json
{
  "error": "INSUFFICIENT_PERMISSIONS",
  "message": "Only evaluators and admins can add evaluation comments",
  "timestamp": "2026-02-26T14:35:00Z"
}
```

**Error: 404 Not Found** (Idea doesn't exist)

```json
{
  "error": "IDEA_NOT_FOUND",
  "message": "Idea with ID 123 not found",
  "timestamp": "2026-02-26T14:35:00Z"
}
```

### Example Request

```bash
curl -X POST http://localhost:8080/api/v1/ideas/123/comments \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..." \
  -H "Content-Type: application/json" \
  -d '{
    "comment": "Have you considered the environmental impact?"
  }'
```

---

## 3. Get Evaluation History

**Endpoint**: `GET /api/v1/ideas/{id}/evaluations`

**Purpose**: Retrieve all evaluation comments and status changes for an idea

**Authentication**: Required (JWT Bearer token)

**Authorization**: Any authenticated user (submitters can view their own ideas' evaluations)

**HTTP Method**: GET

### Request

**Path Parameters**:
- `id` (Long, required): The ID of the idea

**Query Parameters**: None (pagination out of scope for MVP)

### Response

**Success: 200 OK**

Returns evaluation history in chronological order:

```json
{
  "ideaId": 123,
  "evaluations": [
    {
      "id": 450,
      "ideaId": 123,
      "evaluatorName": "Bob Smith",
      "evaluatorId": "660e8400-e29b-41d4-a716-446655440001",
      "comment": "Moving to review phase. Requesting technical feasibility analysis.",
      "statusSnapshot": "UNDER_REVIEW",
      "createdAt": "2026-02-22T09:15:00Z"
    },
    {
      "id": 451,
      "ideaId": 123,
      "evaluatorName": "Carol Davis",
      "evaluatorId": "770e8400-e29b-41d4-a716-446655440002",
      "comment": "Technical analysis complete. Architecture looks solid.",
      "statusSnapshot": null,
      "createdAt": "2026-02-24T11:20:00Z"
    },
    {
      "id": 452,
      "ideaId": 123,
      "evaluatorName": "Bob Smith",
      "evaluatorId": "660e8400-e29b-41d4-a716-446655440001",
      "comment": "Approved for implementation. Budget allocated for Q2.",
      "statusSnapshot": "ACCEPTED",
      "createdAt": "2026-02-26T14:30:00Z"
    }
  ]
}
```

**Response Fields**:
- `ideaId`: ID of the idea
- `evaluations`: Array of evaluation records, ordered by `createdAt` ASC (oldest first)
  - `id`: Unique evaluation ID
  - `ideaId`: ID of the idea (same as parent)
  - `evaluatorName`: Display name of evaluator
  - `evaluatorId`: UUID of evaluator user
  - `comment`: Evaluation comment text
  - `statusSnapshot`: Status set during this evaluation (null if comment-only)
  - `createdAt`: Timestamp in ISO 8601 format

**Success: 200 OK** (Empty history)

```json
{
  "ideaId": 123,
  "evaluations": []
}
```

**Error: 404 Not Found** (Idea doesn't exist)

```json
{
  "error": "IDEA_NOT_FOUND",
  "message": "Idea with ID 123 not found",
  "timestamp": "2026-02-26T14:35:00Z"
}
```

### Example Request

```bash
curl -X GET http://localhost:8080/api/v1/ideas/123/evaluations \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIs..."
```

---

## Data Transfer Objects (DTOs)

### UpdateIdeaStatusRequest

```java
public record UpdateIdeaStatusRequest(
    @NotNull(message = "Status is required")
    IdeaStatus newStatus,
    
    String comment  // Validated conditionally in service layer
) {}
```

**Validation**:
- `newStatus`: Cannot be null
- `comment`: Required if `newStatus` is ACCEPTED or REJECTED (service layer validation)

---

### AddCommentRequest

```java
public record AddCommentRequest(
    @NotBlank(message = "Comment cannot be empty")
    @Size(max = 5000, message = "Comment cannot exceed 5000 characters")
    String comment
) {}
```

**Validation**:
- `comment`: Not blank (not null, not empty, not whitespace-only)
- `comment`: Max 5000 characters

---

### IdeaEvaluationResponse

```java
public record IdeaEvaluationResponse(
    Long id,
    Long ideaId,
    String evaluatorName,
    UUID evaluatorId,
    String comment,
    IdeaStatus statusSnapshot,
    LocalDateTime createdAt
) {
    public static IdeaEvaluationResponse from(IdeaEvaluation evaluation) {
        return new IdeaEvaluationResponse(
            evaluation.getId(),
            evaluation.getIdea().getId(),
            evaluation.getEvaluator().getFullName(),
            evaluation.getEvaluator().getId(),
            evaluation.getComment(),
            evaluation.getStatusSnapshot(),
            evaluation.getCreatedAt()
        );
    }
}
```

---

### EvaluationHistoryResponse

```java
public record EvaluationHistoryResponse(
    Long ideaId,
    List<IdeaEvaluationResponse> evaluations
) {}
```

---

### IdeaResponse (extended from existing)

Existing DTO with `evaluationCount` field:

```java
public record IdeaResponse(
    Long id,
    String title,
    String category,
    IdeaStatus status,
    String submitterName,
    UUID submitterId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    boolean hasAttachment,
    int evaluationCount  // NEW: Count of evaluations
) {
    public static IdeaResponse from(Idea idea) {
        return new IdeaResponse(
            idea.getId(),
            idea.getTitle(),
            idea.getCategory(),
            idea.getStatus(),
            idea.getSubmitter().getFullName(),
            idea.getSubmitter().getId(),
            idea.getCreatedAt(),
            idea.getUpdatedAt(),
            idea.getAttachment() != null,
            idea.getEvaluations().size()  // Count evaluations
        );
    }
}
```

---

## Workflow State Transitions

Valid status transitions enforced by backend:

```
SUBMITTED
    ├→ UNDER_REVIEW (comment optional)
    ├→ ACCEPTED (comment required)
    └→ REJECTED (comment required)

UNDER_REVIEW
    ├→ ACCEPTED (comment required)
    └→ REJECTED (comment required)

ACCEPTED (terminal state)
REJECTED (terminal state)
```

**Invalid Transitions** (will return 400 Bad Request):
- Any transition from ACCEPTED or REJECTED
- Any invalid status value
- Any transition not defined above

---

## Security Considerations

### Authorization Matrix

| Endpoint | Submitter | Evaluator | Admin |
|----------|-----------|-----------|-------|
| `PATCH /ideas/{id}/status` | ❌ 403 | ✅ 200 | ✅ 200 |
| `POST /ideas/{id}/comments` | ❌ 403 | ✅ 201 | ✅ 201 |
| `GET /ideas/{id}/evaluations` | ✅ 200 | ✅ 200 | ✅ 200 |

### Input Validation

All user input is validated both client-side (for UX) and server-side (for security):
- Comment length limits prevent DoS via large payloads
- Status enum validation prevents injection attacks
- SQL injection prevented via JPA parameterized queries
- XSS prevented via output encoding in frontend

### Rate Limiting

Consider implementing rate limiting for evaluation endpoints (future enhancement):
- Max 10 status updates per minute per user
- Max 50 comments per hour per user

---

## Performance Considerations

### Response Times

Target response times under normal load:
- Status update: < 500ms
- Add comment: < 300ms
- Get evaluation history: < 1000ms

### Optimization Strategies

- Database indexes on `idea_id` for fast evaluation retrieval
- EAGER fetch evaluator relationship to avoid N+1 queries
- Consider caching evaluation counts for idea listing page (future)

---

## Versioning

**Current Version**: v1  
**Base Path**: `/api/v1`

Future API changes will use version increments (`/api/v2`) to maintain backward compatibility.

---

**Version**: 1.0  
**Last Updated**: February 26, 2026  
**Status**: Ready for implementation
