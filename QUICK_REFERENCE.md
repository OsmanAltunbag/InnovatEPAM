# Quick Reference: Evaluation Workflow REST API

**Feature**: 003-evaluation-workflow  
**Date**: February 26, 2026  
**Status**: ✅ IMPLEMENTED & TESTED

---

## At a Glance

Three fully implemented REST endpoints for managing idea evaluations:

```
PATCH /api/v1/ideas/{id}/status      → Update idea status (200, 400, 403, 404)
POST  /api/v1/ideas/{id}/comments     → Add comment (201, 400, 403, 404)
GET   /api/v1/ideas/{id}/evaluations  → Get history (200, 404)
```

All require JWT Bearer token. Status/Comment endpoints require EVALUATOR or ADMIN role.

---

## Endpoint Reference

### 1️⃣ PATCH /api/v1/ideas/{id}/status

**Role**: EVALUATOR, ADMIN  
**Response**: 200 OK (IdeaResponse) | 400 Bad Request | 403 Forbidden | 404 Not Found

```bash
# Request
curl -X PATCH http://localhost:8080/api/v1/ideas/123/status \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "newStatus": "ACCEPTED",
    "comment": "Approved for implementation"
  }'

# Response (200 OK)
{
  "id": 123,
  "status": "ACCEPTED",
  "evaluationCount": 3,
  ...
}
```

**Valid Status Transitions**:
```
SUBMITTED → UNDER_REVIEW (comment optional)
SUBMITTED → ACCEPTED (comment required)
SUBMITTED → REJECTED (comment required)
UNDER_REVIEW → ACCEPTED (comment required)
UNDER_REVIEW → REJECTED (comment required)
```

---

### 2️⃣ POST /api/v1/ideas/{id}/comments

**Role**: EVALUATOR, ADMIN  
**Response**: 201 Created | 400 Bad Request | 403 Forbidden | 404 Not Found

```bash
# Request
curl -X POST http://localhost:8080/api/v1/ideas/123/comments \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "comment": "Please add more technical details"
  }'

# Response (201 Created)
{
  "id": 456,
  "ideaId": 123,
  "evaluatorName": "bob@company.com",
  "comment": "Please add more technical details",
  "statusSnapshot": null,
  "createdAt": "2026-02-26T14:35:00Z"
}
```

---

### 3️⃣ GET /api/v1/ideas/{id}/evaluations

**Role**: Any authenticated user  
**Response**: 200 OK | 404 Not Found

```bash
# Request
curl -X GET http://localhost:8080/api/v1/ideas/123/evaluations \
  -H "Authorization: Bearer <token>"

# Response (200 OK)
{
  "ideaId": 123,
  "evaluations": [
    {
      "id": 450,
      "evaluatorName": "bob@company.com",
      "comment": "Moving to review phase",
      "statusSnapshot": "UNDER_REVIEW",
      "createdAt": "2026-02-22T09:15:00Z"
    },
    {
      "id": 451,
      "evaluatorName": "bob@company.com",
      "comment": "Approved for implementation",
      "statusSnapshot": "ACCEPTED",
      "createdAt": "2026-02-26T14:35:00Z"
    }
  ]
}
```

---

## Error Responses

### 400 Bad Request - Invalid Status Transition
```json
{
  "status": 400,
  "message": "Validation failed",
  "details": {
    "newStatus": "Invalid status transition from ACCEPTED to UNDER_REVIEW"
  }
}
```

### 400 Bad Request - Missing Required Comment
```json
{
  "status": 400,
  "message": "Validation failed",
  "details": {
    "statusTransition": "Comment is required when accepting or rejecting an idea"
  }
}
```

### 400 Bad Request - Empty Comment
```json
{
  "status": 400,
  "message": "Validation failed",
  "details": {
    "comment": "Comment is required"
  }
}
```

### 403 Forbidden - Insufficient Role
```json
{
  "status": 403,
  "message": "Access is denied"
}
```

### 404 Not Found - Idea Doesn't Exist
```json
{
  "status": 404,
  "message": "Idea with ID 999 not found"
}
```

---

## Implementation Files

| File | Status | Purpose |
|------|--------|---------|
| IdeaEvaluationController | ✅ Ready | All three endpoints |
| IdeaExceptionHandler | ✅ Updated | Maps exceptions to 400/403/404 |
| IdeaService.updateStatus() | ✅ Ready | Status transition + evaluation |
| IdeaEvaluationService | ✅ Ready | Comment & history management |
| UpdateIdeaStatusRequest | ✅ Ready | Request DTO for status updates |
| AddCommentRequest | ✅ Ready | Request DTO for comments |
| IdeaEvaluationResponse | ✅ Ready | Response DTO for evaluations |
| EvaluationHistoryResponse | ✅ Ready | Response DTO for history |

---

## Validation Rules

| Field | Validation | Endpoint |
|-------|-----------|----------|
| newStatus | @NotNull, valid transition | PATCH /status |
| comment (status update) | @Size(max=5000), required for ACCEPTED/REJECTED | PATCH /status |
| comment (standalone) | @NotBlank, @Size(max=5000) | POST /comments |

---

## Security

All endpoints require:
```
Authorization: Bearer <jwt_token>
```

**Authorization Rules**:
- `PATCH /status`: EVALUATOR, ADMIN only → 403 if submitter
- `POST /comments`: EVALUATOR, ADMIN only → 403 if submitter
- `GET /evaluations`: Any authenticated → 401 if not logged in

---

## HTTP Status Codes

| Status | Meaning | Triggers |
|--------|---------|----------|
| 200 | Success | Status update successful |
| 201 | Created | Comment created |
| 400 | Bad Request | Invalid transition, missing comment, validation error |
| 403 | Forbidden | Insufficient role (submitter attempting status update) |
| 404 | Not Found | Idea doesn't exist |
| 401 | Unauthorized | Missing/invalid JWT token |

---

## Testing Checklist

- [ ] PATCH status: SUBMITTED → UNDER_REVIEW (no comment) → 200 OK
- [ ] PATCH status: SUBMITTED → ACCEPTED + comment → 200 OK
- [ ] PATCH status: UNDER_REVIEW → ACCEPTED without comment → 400 Bad Request
- [ ] PATCH status: as submitter (not evaluator) → 403 Forbidden
- [ ] POST comment: add standalone comment → 201 Created
- [ ] POST comment: empty comment → 400 Bad Request
- [ ] GET evaluations: retrieve history → 200 OK (chronological order)
- [ ] GET evaluations: idea doesn't exist → 404 Not Found
- [ ] All endpoints with no auth header → 401 Unauthorized

---

## Code Snippet: Basic Integration

```java
// In your service or controller
@Autowired
private IdeaEvaluationService evaluationService;

@Autowired
private IdeaService ideaService;

// Update status
var updatedIdea = ideaService.updateStatus(
    ideaId,
    IdeaStatus.ACCEPTED,
    currentUser,
    "Approved for implementation"
);

// Add comment
var evaluation = evaluationService.addComment(
    ideaId,
    currentUser,
    "Please provide additional details"
);

// Get history
var history = evaluationService.getEvaluationHistory(ideaId);
```

---

## Frontend Integration Example

```javascript
// API service
const updateIdeaStatus = async (ideaId, newStatus, comment) => {
  const response = await fetch(`/api/v1/ideas/${ideaId}/status`, {
    method: 'PATCH',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ newStatus, comment })
  });
  
  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.details?.statusTransition || 'Status update failed');
  }
  
  return response.json();
};

// Usage
try {
  const updated = await updateIdeaStatus(123, 'ACCEPTED', 'Looks good!');
  setIdea(updated);
} catch (error) {
  setError(error.message);
}
```

---

## PostgreSQL Schema

```sql
-- Migration: V7__create_idea_evaluations_table.sql (already created)
CREATE TABLE idea_evaluations (
    id BIGSERIAL PRIMARY KEY,
    idea_id BIGINT NOT NULL REFERENCES ideas(id) ON DELETE CASCADE,
    evaluator_id UUID NOT NULL REFERENCES users(id),
    comment TEXT NOT NULL,
    status_snapshot VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT status_snapshot_enum CHECK (
        status_snapshot IS NULL OR 
        status_snapshot IN ('UNDER_REVIEW', 'ACCEPTED', 'REJECTED')
    ),
    INDEX idx_idea_id (idea_id),
    INDEX idx_created_at_asc (created_at ASC)
);
```

---

## Common Issues & Solutions

### Issue: "Comment is required"
**Cause**: Attempting to transition to ACCEPTED/REJECTED without comment  
**Solution**: Include non-empty comment in request body

### Issue: "Invalid status transition"
**Cause**: Attempting invalid transition (e.g., ACCEPTED → UNDER_REVIEW)  
**Solution**: Check valid transitions diagram above

### Issue: 403 Forbidden
**Cause**: User doesn't have EVALUATOR or ADMIN role  
**Solution**: Login as evaluator or ensure authorized user making request

### Issue: 404 Not Found
**Cause**: Idea ID doesn't exist in database  
**Solution**: Verify idea ID is correct

---

## Performance Notes

- `GET /evaluations` returns all evaluations (no pagination in MVP)
- Status updates are atomic (status + evaluation created together)
- Indexes on `idea_id` and `created_at` for fast queries
- Evaluator relationship fetched eagerly (needed for display name)
- Idea relationship is lazy-loaded (avoid N+1)

---

## Migration Notes

All database infrastructure already set up:
- ✅ idea_evaluations table created
- ✅ Flyway migration applied (V7__create_idea_evaluations_table.sql)
- ✅ JPA entity IdeaEvaluation configured
- ✅ Repository created

No additional migration needed for these endpoints.

