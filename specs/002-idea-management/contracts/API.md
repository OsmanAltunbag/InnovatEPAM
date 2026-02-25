# API Contracts: Idea Management System

**Feature**: Idea Management System  
**Date**: February 25, 2026  
**Version**: 1.0  
**Base URL**: `/api/v1`

---

## 1. Create Idea

**Endpoint**: `POST /api/v1/ideas`

**Authentication**: Required (JWT Bearer token)

**Authorization**: Role `SUBMITTER` or `ADMIN`

**Content-Type**: `multipart/form-data`

### Request

```
POST /api/v1/ideas HTTP/1.1
Authorization: Bearer {jwt_token}
Content-Type: multipart/form-data; boundary=----WebKitFormBoundary

------WebKitFormBoundary
Content-Disposition: form-data; name="title"

Sustainable Packaging Initiative
------WebKitFormBoundary
Content-Disposition: form-data; name="description"

Proposal to switch to eco-friendly packaging materials across all product lines
------WebKitFormBoundary
Content-Disposition: form-data; name="category"

Sustainability
------WebKitFormBoundary
Content-Disposition: form-data; name="file"; filename="proposal.pdf"
Content-Type: application/pdf

[binary PDF content]
------WebKitFormBoundary--
```

**Request Fields**:
- `title` (string, required): Idea title. Max 255 characters.
- `description` (string, required): Full description. No length limit.
- `category` (string, required): Classification category. E.g., "Process Improvement", "Cost Reduction", "Sustainability"
- `file` (file, optional): Single PDF or PNG file attachment. Max 50MB.

### Response

**Success: 201 Created**

```json
{
  "id": 42,
  "title": "Sustainable Packaging Initiative",
  "category": "Sustainability",
  "status": "SUBMITTED",
  "submitterName": "Alice Johnson",
  "createdAt": "2026-02-25T14:30:00Z",
  "hasAttachment": true,
  "evaluationCount": 0
}
```

**Error: 400 Bad Request** (validation failed)

```json
{
  "error": "VALIDATION_ERROR",
  "message": "Validation failed",
  "details": {
    "title": "Title is required",
    "file": "File size exceeds 50MB limit"
  }
}
```

**Error: 413 Payload Too Large** (file size exceeded)

```json
{
  "error": "FILE_SIZE_LIMIT_EXCEEDED",
  "message": "File size cannot exceed 50MB"
}
```

**Error: 403 Forbidden** (insufficient permissions)

```json
{
  "error": "INSUFFICIENT_PERMISSIONS",
  "message": "User role does not have permission to submit ideas"
}
```

---

## 2. Get All Ideas (Listing)

**Endpoint**: `GET /api/v1/ideas`

**Authentication**: Required (JWT Bearer token)

**Authorization**: All authenticated users

**Query Parameters**:
- `page` (integer, optional, default=0): Page number (0-indexed)
- `size` (integer, optional, default=10): Items per page
- `status` (string, optional): Filter by status (SUBMITTED, UNDER_REVIEW, ACCEPTED, REJECTED)
- `category` (string, optional): Filter by category

### Request

```
GET /api/v1/ideas?page=0&size=10&status=SUBMITTED HTTP/1.1
Authorization: Bearer {jwt_token}
```

### Response

**Success: 200 OK**

```json
{
  "content": [
    {
      "id": 42,
      "title": "Sustainable Packaging Initiative",
      "category": "Sustainability",
      "status": "SUBMITTED",
      "submitterName": "Alice Johnson",
      "createdAt": "2026-02-25T14:30:00Z",
      "hasAttachment": true,
      "evaluationCount": 0
    },
    {
      "id": 41,
      "title": "Remote Work Policy Update",
      "category": "HR",
      "status": "UNDER_REVIEW",
      "submitterName": "Bob Smith",
      "createdAt": "2026-02-24T10:15:00Z",
      "hasAttachment": false,
      "evaluationCount": 2
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 25,
    "totalPages": 3
  }
}
```

**Response Fields**:
- `content`: Array of idea summaries
- `pageable`: Pagination metadata (page number, size, total count)

**Error: 401 Unauthorized**

```json
{
  "error": "UNAUTHORIZED",
  "message": "Authentication token is missing or invalid"
}
```

---

## 3. Get Idea Detail

**Endpoint**: `GET /api/v1/ideas/{id}`

**Authentication**: Required (JWT Bearer token)

**Authorization**: All authenticated users

**Path Parameters**:
- `id` (integer, required): Idea ID

### Request

```
GET /api/v1/ideas/42 HTTP/1.1
Authorization: Bearer {jwt_token}
```

### Response

**Success: 200 OK**

```json
{
  "id": 42,
  "title": "Sustainable Packaging Initiative",
  "description": "Proposal to switch to eco-friendly packaging materials across all product lines. This would reduce our environmental footprint by 40% and improve brand image.",
  "category": "Sustainability",
  "status": "UNDER_REVIEW",
  "submitterName": "Alice Johnson",
  "createdAt": "2026-02-25T14:30:00Z",
  "updatedAt": "2026-02-25T16:45:00Z",
  "attachment": {
    "id": 101,
    "originalFilename": "proposal.pdf",
    "fileSize": 2048576,
    "createdAt": "2026-02-25T14:30:00Z"
  },
  "evaluations": [
    {
      "id": 201,
      "evaluatorName": "Charlie Lee",
      "comment": "Moving to Under Review for detailed cost analysis",
      "statusSnapshot": "UNDER_REVIEW",
      "createdAt": "2026-02-25T15:00:00Z"
    },
    {
      "id": 202,
      "evaluatorName": "Diana Wong",
      "comment": "Cost-benefit analysis looks promising. Need timeline for implementation.",
      "statusSnapshot": null,
      "createdAt": "2026-02-25T16:45:00Z"
    }
  ]
}
```

**Response Fields**:
- `id`: Idea identifier
- `title`, `description`: Idea content
- `category`, `status`: Classification and workflow state
- `submitterName`: Display name of submitter
- `createdAt`, `updatedAt`: Timestamps
- `attachment`: Optional file metadata (null if no attachment)
- `evaluations`: Array of evaluation comments in chronological order

**Error: 404 Not Found**

```json
{
  "error": "NOT_FOUND",
  "message": "Idea with ID 42 not found"
}
```

---

## 4. Update Idea Status

**Endpoint**: `PATCH /api/v1/ideas/{id}/status`

**Authentication**: Required (JWT Bearer token)

**Authorization**: Role `EVALUATOR` or `ADMIN`

**Content-Type**: `application/json`

### Request

```json
{
  "newStatus": "UNDER_REVIEW",
  "comment": "Forwarding for evaluation by product team"
}
```

**Request Fields**:
- `newStatus` (string, required): Target status (UNDER_REVIEW, ACCEPTED, REJECTED)
- `comment` (string, required if status=REJECTED, optional otherwise): Evaluator feedback

**Validation Rules**:
- Valid status transitions:
  - SUBMITTED → UNDER_REVIEW
  - UNDER_REVIEW → ACCEPTED
  - UNDER_REVIEW → REJECTED
  - SUBMITTED → REJECTED (optional, allows direct rejection)
- Comment is mandatory if `newStatus` is REJECTED
- Comment length: max 5000 characters

### Response

**Success: 200 OK**

```json
{
  "id": 42,
  "title": "Sustainable Packaging Initiative",
  "category": "Sustainability",
  "status": "UNDER_REVIEW",
  "submitterName": "Alice Johnson",
  "createdAt": "2026-02-25T14:30:00Z",
  "hasAttachment": true,
  "evaluationCount": 1
}
```

**Error: 400 Bad Request** (invalid transition)

```json
{
  "error": "INVALID_STATUS_TRANSITION",
  "message": "Cannot transition from ACCEPTED to UNDER_REVIEW",
  "currentStatus": "ACCEPTED",
  "attemptedStatus": "UNDER_REVIEW"
}
```

**Error: 400 Bad Request** (missing required comment)

```json
{
  "error": "VALIDATION_ERROR",
  "message": "Comment is required when rejecting an idea",
  "details": {
    "comment": "Required field missing"
  }
}
```

**Error: 404 Not Found**

```json
{
  "error": "NOT_FOUND",
  "message": "Idea with ID 42 not found"
}
```

**Error: 403 Forbidden** (insufficient permissions)

```json
{
  "error": "INSUFFICIENT_PERMISSIONS",
  "message": "Only evaluators and admins can update idea status"
}
```

**Error: 409 Conflict** (concurrent update)

```json
{
  "error": "CONCURRENT_UPDATE",
  "message": "Idea was modified by another user. Please refresh and try again."
}
```

---

## 5. Add Evaluation Comment

**Endpoint**: `POST /api/v1/ideas/{id}/comments`

**Authentication**: Required (JWT Bearer token)

**Authorization**: Role `EVALUATOR` or `ADMIN`

**Content-Type**: `application/json`

### Request

```json
{
  "comment": "Great initiative with strong ROI potential. Need more details on supplier partnerships."
}
```

**Request Fields**:
- `comment` (string, required): Evaluator feedback. Max 5000 characters.

### Response

**Success: 201 Created**

```json
{
  "id": 203,
  "evaluatorName": "Eve Martinez",
  "comment": "Great initiative with strong ROI potential. Need more details on supplier partnerships.",
  "statusSnapshot": null,
  "createdAt": "2026-02-25T17:30:00Z"
}
```

**Error: 400 Bad Request** (validation failed)

```json
{
  "error": "VALIDATION_ERROR",
  "message": "Validation failed",
  "details": {
    "comment": "Comment cannot be empty"
  }
}
```

**Error: 404 Not Found**

```json
{
  "error": "NOT_FOUND",
  "message": "Idea with ID 42 not found"
}
```

**Error: 403 Forbidden**

```json
{
  "error": "INSUFFICIENT_PERMISSIONS",
  "message": "Only evaluators and admins can add comments"
}
```

---

## 6. Get Evaluation History

**Endpoint**: `GET /api/v1/ideas/{id}/evaluations`

**Authentication**: Required (JWT Bearer token)

**Authorization**: All authenticated users (can see evaluations on any public idea)

### Request

```
GET /api/v1/ideas/42/evaluations HTTP/1.1
Authorization: Bearer {jwt_token}
```

### Response

**Success: 200 OK**

```json
{
  "ideaId": 42,
  "evaluations": [
    {
      "id": 201,
      "evaluatorName": "Charlie Lee",
      "comment": "Moving to Under Review for detailed cost analysis",
      "statusSnapshot": "UNDER_REVIEW",
      "createdAt": "2026-02-25T15:00:00Z"
    },
    {
      "id": 202,
      "evaluatorName": "Diana Wong",
      "comment": "Cost-benefit analysis looks promising. Need timeline for implementation.",
      "statusSnapshot": null,
      "createdAt": "2026-02-25T16:45:00Z"
    },
    {
      "id": 203,
      "evaluatorName": "Eve Martinez",
      "comment": "Approved pending supplier verification",
      "statusSnapshot": "ACCEPTED",
      "createdAt": "2026-02-25T17:30:00Z"
    }
  ]
}
```

**Error: 404 Not Found**

```json
{
  "error": "NOT_FOUND",
  "message": "Idea with ID 42 not found"
}
```

---

## 7. Download Attachment

**Endpoint**: `GET /api/v1/ideas/{id}/attachments/{attachmentId}`

**Authentication**: Required (JWT Bearer token)

**Authorization**: All authenticated users

### Request

```
GET /api/v1/ideas/42/attachments/101 HTTP/1.1
Authorization: Bearer {jwt_token}
Accept: application/pdf
```

### Response

**Success: 200 OK** (file content)

```
HTTP/1.1 200 OK
Content-Type: application/pdf
Content-Disposition: attachment; filename="proposal.pdf"
Content-Length: 2048576

[binary PDF content]
```

**Error: 404 Not Found**

```json
{
  "error": "NOT_FOUND",
  "message": "Attachment with ID 101 not found for idea 42"
}
```

**Error: 403 Forbidden** (idea belongs to different user)

```json
{
  "error": "INSUFFICIENT_PERMISSIONS",
  "message": "Cannot access attachment from other ideas"
}
```

---

## Error Response Format

All error responses follow this standardized format:

```json
{
  "error": "[ERROR_CODE]",
  "message": "[User-friendly error message]",
  "details": {
    "[field]": "[error description]"
  },
  "timestamp": "2026-02-25T17:30:00Z"
}
```

**Common HTTP Status Codes**:
- `200 OK`: Successful GET or PATCH
- `201 Created`: Successful POST (resource created)
- `400 Bad Request`: Validation error or invalid state transition
- `401 Unauthorized`: Missing or invalid JWT token
- `403 Forbidden`: Authenticated but insufficient permissions
- `404 Not Found`: Resource not found
- `409 Conflict`: Concurrency issue (optimistic lock failure)
- `413 Payload Too Large`: File upload exceeds size limit
- `500 Internal Server Error`: Server error

---

## Authentication Header

All endpoints require JWT authentication:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

Token is obtained from the existing authentication service (`POST /api/v1/auth/login`).

---

## Pagination Format

List endpoints support pagination:

**Query Parameters**:
- `page` (integer, default=0): Zero-indexed page number
- `size` (integer, default=10, max=100): Items per page

**Response Structure**:

```json
{
  "content": [...array of items...],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 42,
    "totalPages": 5
  }
}
```

---

## Rate Limiting (Future)

Not implemented in MVP but documented for future:
- Rate limit: 100 requests per minute per user
- Response headers: `X-RateLimit-Limit`, `X-RateLimit-Remaining`, `X-RateLimit-Reset`
