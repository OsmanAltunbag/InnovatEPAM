# Evaluation Workflow REST API Implementation Summary

**Date**: February 26, 2026  
**Feature**: 003-evaluation-workflow  
**Tasks Completed**: T015, T016, T027, T028, T040, T050, T051

## Overview

The Evaluation Workflow REST API has been successfully implemented with three core endpoints for managing idea evaluations. All endpoints are secured with JWT authentication and role-based access control using Spring Security `@PreAuthorize` annotations.

---

## Implemented Endpoints

### 1. PATCH `/api/v1/ideas/{id}/status` - Update Idea Status

**Purpose**: Change an idea's status through the evaluation workflow with optional or required comment

**Controller**: [IdeaEvaluationController](backend/src/main/java/com/innovatepam/idea/controller/IdeaEvaluationController.java)

**Authorization**: `@PreAuthorize("hasAnyRole('EVALUATOR', 'ADMIN')")` - Only evaluators and admins can update status

**Request**:
- Path Parameter: `id` (Long) - Idea ID
- Body: `UpdateIdeaStatusRequest`
  ```java
  {
    "newStatus": "ACCEPTED",           // Required: UNDER_REVIEW, ACCEPTED, REJECTED
    "comment": "Great proposal..."      // Required for ACCEPTED/REJECTED, optional for UNDER_REVIEW
  }
  ```

**Response (200 OK)**:
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

**Error Responses**:
- **400 Bad Request** - Invalid status transition or missing required comment
- **403 Forbidden** - User lacks EVALUATOR/ADMIN role
- **404 Not Found** - Idea doesn't exist

### 2. POST `/api/v1/ideas/{id}/comments` - Add Evaluation Comment

**Purpose**: Add a standalone evaluation comment without changing status

**Controller**: [IdeaEvaluationController](backend/src/main/java/com/innovatepam/idea/controller/IdeaEvaluationController.java)

**Authorization**: `@PreAuthorize("hasAnyRole('EVALUATOR', 'ADMIN')")` - Only evaluators and admins can comment

**Request**:
- Path Parameter: `id` (Long) - Idea ID
- Body: `AddCommentRequest`
  ```java
  {
    "comment": "Please provide additional implementation timeline details."
  }
  ```

**Response (201 Created)**:
```json
{
  "id": 456,
  "ideaId": 123,
  "evaluatorName": "bob@company.com",
  "evaluatorId": "660e8400-e29b-41d4-a716-446655440001",
  "comment": "Please provide additional details on the implementation timeline.",
  "statusSnapshot": null,
  "createdAt": "2026-02-26T14:35:00Z"
}
```

**Error Responses**:
- **400 Bad Request** - Comment is empty or exceeds 5000 characters
- **403 Forbidden** - User lacks EVALUATOR/ADMIN role
- **404 Not Found** - Idea doesn't exist

### 3. GET `/api/v1/ideas/{id}/evaluations` - Get Evaluation History

**Purpose**: Retrieve all evaluation comments and status changes for an idea (chronological order)

**Controller**: [IdeaEvaluationController](backend/src/main/java/com/innovatepam/idea/controller/IdeaEvaluationController.java)

**Authorization**: `@PreAuthorize("isAuthenticated()")` - Any authenticated user can view

**Request**:
- Path Parameter: `id` (Long) - Idea ID

**Response (200 OK)**:
```json
{
  "ideaId": 123,
  "evaluations": [
    {
      "id": 450,
      "ideaId": 123,
      "evaluatorName": "bob@company.com",
      "evaluatorId": "660e8400-e29b-41d4-a716-446655440001",
      "comment": "Moving to review phase. Requesting technical feasibility analysis.",
      "statusSnapshot": "UNDER_REVIEW",
      "createdAt": "2026-02-22T09:15:00Z"
    },
    {
      "id": 451,
      "ideaId": 123,
      "evaluatorName": "carol@company.com",
      "evaluatorId": "770e8400-e29b-41d4-a716-446655440002",
      "comment": "Technical analysis complete. Architecture looks solid.",
      "statusSnapshot": null,
      "createdAt": "2026-02-24T11:20:00Z"
    },
    {
      "id": 452,
      "ideaId": 123,
      "evaluatorName": "bob@company.com",
      "evaluatorId": "660e8400-e29b-41d4-a716-446655440001",
      "comment": "Approved for implementation. Budget allocated for Q2.",
      "statusSnapshot": "ACCEPTED",
      "createdAt": "2026-02-26T14:30:00Z"
    }
  ]
}
```

**Error Responses**:
- **404 Not Found** - Idea doesn't exist

---

## Key Implementation Files

### Controller Layer

**File**: [IdeaEvaluationController.java](backend/src/main/java/com/innovatepam/idea/controller/IdeaEvaluationController.java)

Handles all three evaluation endpoints:
- `PATCH /{id}/status` - Update idea status with optional comment
- `POST /{id}/comments` - Add standalone comment 
- `GET /{id}/evaluations` - Retrieve evaluation history

Features:
- Role-based authorization via `@PreAuthorize`
- Request validation via `@Valid` on DTOs
- Proper HTTP status codes (200, 201, 400, 403, 404)
- User authentication extraction from Security context

### Service Layer

**IdeaService** - [Link](backend/src/main/java/com/innovatepam/idea/service/IdeaService.java)
- `updateStatus(ideaId, targetStatus, evaluator, comment)` - Validates transition and creates evaluation
- Enforces status workflow rules
- Validates comment requirements
- Uses `@Transactional` for atomicity

**IdeaEvaluationService** - [Link](backend/src/main/java/com/innovatepam/idea/service/IdeaEvaluationService.java)
- `addComment(ideaId, evaluator, comment)` - Creates standalone comment
- `addStatusEvaluation(idea, evaluator, comment, statusSnapshot)` - Creates status change evaluation
- `getEvaluationHistory(ideaId)` - Retrieves evaluations ordered by createdAt ASC

### Exception Handling

**File**: [IdeaExceptionHandler.java](backend/src/main/java/com/innovatepam/idea/exception/IdeaExceptionHandler.java)

**Updated Exception Mappings**:

| Exception | HTTP Status | Response Format |
|-----------|-------------|-----------------|
| `InvalidStatusTransitionException` | **400 Bad Request** | `ValidationErrorResponse` with details map |
| `IdeaNotFoundException` | 404 Not Found | `ErrorResponse` |
| `UnauthorizedAccessException` | 403 Forbidden | `ErrorResponse` |
| `MethodArgumentNotValidException` | 400 Bad Request | `ValidationErrorResponse` |
| `ConstraintViolationException` | 400 Bad Request | `ErrorResponse` |

**Key Change**: `InvalidStatusTransitionException` now maps to HTTP 400 (validation error) instead of 409 (conflict), aligning with REST API contract specifications.

### Data Transfer Objects

**UpdateIdeaStatusRequest** - [Link](backend/src/main/java/com/innovatepam/idea/dto/UpdateIdeaStatusRequest.java)
```java
public record UpdateIdeaStatusRequest(
    @NotNull(message = "New status is required")
    IdeaStatus newStatus,
    
    @Size(max = 5000, message = "Comment cannot exceed 5000 characters")
    String comment
) {}
```

**AddCommentRequest** - [Link](backend/src/main/java/com/innovatepam/idea/dto/AddCommentRequest.java)
```java
public record AddCommentRequest(
    @NotBlank(message = "Comment is required")
    @Size(max = 5000, message = "Comment cannot exceed 5000 characters")
    String comment
) {}
```

**IdeaEvaluationResponse** - [Link](backend/src/main/java/com/innovatepam/idea/dto/IdeaEvaluationResponse.java)
```java
public record IdeaEvaluationResponse(
    Long id,
    Long ideaId,
    String evaluatorName,
    UUID evaluatorId,
    String comment,
    IdeaStatus statusSnapshot,
    LocalDateTime createdAt
) {}
```

**EvaluationHistoryResponse** - [Link](backend/src/main/java/com/innovatepam/idea/dto/EvaluationHistoryResponse.java)
```java
public record EvaluationHistoryResponse(
    Long ideaId,
    List<IdeaEvaluationResponse> evaluations
) {}
```

### Entity Model

**IdeaEvaluation** - [Link](backend/src/main/java/com/innovatepam/idea/model/IdeaEvaluation.java)
- JPA Entity mapping to `idea_evaluations` table
- Relationships: ManyToOne to Idea (LAZY) and User/evaluator (EAGER)
- Validation: `@NotBlank` on comment, `@Size(max=5000)`
- Immutability: `createdAt` field is non-updatable
- Indexes: idea_id, evaluator_id, created_at for performance

---

## Security & Authorization

All endpoints require JWT Bearer token in Authorization header:
```
Authorization: Bearer <jwt_token>
```

### Authorization Rules

| Endpoint | Method | Required Role | Access |
|----------|--------|---------------|--------|
| `/api/v1/ideas/{id}/status` | PATCH | EVALUATOR, ADMIN | Evaluators and admins only |
| `/api/v1/ideas/{id}/comments` | POST | EVALUATOR, ADMIN | Evaluators and admins only |
| `/api/v1/ideas/{id}/evaluations` | GET | isAuthenticated() | All authenticated users |

---

## Validation Rules

### Status Transition Workflow

Valid transitions:
```
SUBMITTED
  ├→ UNDER_REVIEW   (comment optional)
  ├→ ACCEPTED       (comment required)
  └→ REJECTED       (comment required)

UNDER_REVIEW
  ├→ ACCEPTED       (comment required)
  └→ REJECTED       (comment required)
```

Invalid transitions return 400 Bad Request with validation error details.

### Comment Validation

- **Required for**: Status transitions to ACCEPTED or REJECTED
- **Optional for**: Status transitions to UNDER_REVIEW, standalone comments always require text
- **Max length**: 5000 characters (enforced at entity and DTO level)
- **Validation**: `@NotBlank` and `@Size` annotations

---

## Testing Recommendations

### Unit Tests
- Test all valid/invalid status transitions in `IdeaStatusValidator`
- Test comment requirement validation
- Test DTOs with boundary values (5000 char limit)
- Test exception handlers with various error scenarios

### Integration Tests
- Test full workflow: SUBMITTED → UNDER_REVIEW → ACCEPTED with comments
- Test invalid transitions and proper 400 response
- Test authorization: evaluator can update, submitter cannot (403)
- Test evaluation history ordering (createdAt ASC)
- Test @Transactional atomicity (status and evaluation together)

### API Tests
```bash
# Update status (valid)
curl -X PATCH http://localhost:8080/api/v1/ideas/123/status \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"newStatus":"UNDER_REVIEW"}'

# Add comment
curl -X POST http://localhost:8080/api/v1/ideas/123/comments \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"comment":"Looks good"}'

# Get evaluation history
curl -X GET http://localhost:8080/api/v1/ideas/123/evaluations \
  -H "Authorization: Bearer <token>"
```

---

## Completed Tasks

- ✅ **T015**: Create IdeaEvaluationController with all three endpoints
- ✅ **T016**: Update IdeaExceptionHandler to map InvalidStatusTransitionException to 400 Bad Request
- ✅ **T027**: Implement PATCH /api/v1/ideas/{id}/status endpoint
- ✅ **T028**: Add @PreAuthorize("hasAnyRole('EVALUATOR', 'ADMIN')") to status endpoint
- ✅ **T040**: Implement POST /api/v1/ideas/{id}/comments endpoint
- ✅ **T050**: Implement GET /api/v1/ideas/{id}/evaluations endpoint
- ✅ **T051**: Add @PreAuthorize("isAuthenticated()") to evaluations endpoint

---

## API Contract Compliance

✅ All endpoints strictly follow the specifications in `specs/003-evaluation-workflow/contracts/API.md`:
- Request/response formats match specification
- Error codes and messages align with contract
- Authorization requirements properly enforced
- Validation rules implemented per specification
- HTTP status codes correct (200, 201, 400, 403, 404)

