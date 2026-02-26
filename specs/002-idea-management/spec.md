# Feature Specification: Idea Management System

**Feature Branch**: `002-idea-management`  
**Created**: February 25, 2026  
**Status**: Draft  
**Input**: User description: "Create the Idea Management System for the InnovatEPAM Portal. This feature must strictly build upon our existing JWT user authentication system. Core Requirements: 1) A submission form for users with the 'submitter' role containing Title, Description, and Category. 2) Support for uploading a single file attachment (e.g., PDF, PNG) per idea. 3) An Idea Listing dashboard where users can view submitted ideas. 4) A basic evaluation workflow where users with the 'evaluator/admin' role can change an idea's status (Submitted, Under Review, Accepted, Rejected) and add evaluation comments."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Submitter Creates and Submits an Idea (Priority: P1)

A user with the 'submitter' role needs to share their idea with the organization. They access the idea submission form, fill in their idea details (title, description, and category), optionally upload a supporting document, and submit it for review.

**Why this priority**: This is the core functionality that enables the entire innovation pipeline. Without this, no ideas can enter the system, making all other features non-functional.

**Independent Test**: Can be tested by having a submitter complete the form and verify the idea appears in the system backend. This delivers core value: the ability to capture innovation from the organization.

**Acceptance Scenarios**:

1. **Given** a user with 'submitter' role is logged in, **When** they navigate to the idea submission form, **Then** they see form fields for Title, Description, and Category
2. **Given** a submitter is on the submission form, **When** they fill in all required fields and click submit, **Then** the idea is saved and they receive a success message
3. **Given** a submitter is uploading a file, **When** they select a valid file (PDF or PNG), **Then** the file is attached to the submission
4. **Given** a submitter has completed a submission, **When** they verify their dashboard, **Then** they can see their submitted idea listed
5. **Given** a submitter attempts to submit without filling required fields, **When** they click submit, **Then** validation errors are displayed and submission is prevented

---

### User Story 2 - Managers View and Track All Submitted Ideas (Priority: P2)

A manager needs to see all ideas submitted by the team to understand what innovations are being proposed. They need to view a dashboard with a list of ideas, their current status, and who submitted them.

**Why this priority**: This provides essential visibility to leadership and enables the evaluation team to know what ideas are in the pipeline.

**Independent Test**: Can be tested by verifying that ideas created in Story 1 are visible in a list view with proper details (title, submitter, status, category).

**Acceptance Scenarios**:

1. **Given** a user is on the ideas listing page, **When** the page loads, **Then** all submitted ideas are displayed with their title, submitter name, submission date, and current status
2. **Given** multiple ideas exist in the system, **When** the evaluator views the list, **Then** ideas are organized in a readable format (table, cards, or list)
3. **Given** an idea has an attached file, **When** viewing the idea in the list, **Then** the user is able to see or access the attachment
4. **Given** ideas with different categories exist, **When** viewing the list, **Then** categories are visible to help classify ideas

---

### User Story 3 - Evaluator Reviews and Changes Idea Status (Priority: P1)

An evaluator or admin needs to assess submitted ideas and move them through the evaluation workflow. They can change an idea's status from Submitted → Under Review → Accepted/Rejected, and provide feedback via evaluation comments.

**Why this priority**: This is critical because without evaluation capability, ideas cannot progress. The evaluation workflow defines how ideas are assessed and is essential for the innovation process.

**Independent Test**: Can be tested independently by changing an idea's status and verifying the transition is recorded correctly. This delivers value: structured evaluation of ideas with documented feedback.

**Acceptance Scenarios**:

1. **Given** an evaluator is viewing an idea detail page, **When** they look for status controls, **Then** they see options to change the status to "Under Review", "Accepted", or "Rejected"
2. **Given** an evaluator wants to change an idea's status, **When** they select a new status, **Then** the idea's status is updated immediately and the change is persisted
3. **Given** an evaluator is leaving feedback, **When** they type evaluation comments and save them, **Then** the comments are attached to the idea and visible in the idea's history
4. **Given** an evaluator changes an idea's status to "Accepted", **When** they save, **Then** the status reflects "Accepted" and the idea's visibility/priority may be highlighted differently
5. **Given** an evaluator wants to reject an idea, **When** they change status to "Rejected", **Then** they must provide a comment explaining the rejection

---

### User Story 4 - Submitter Receives Feedback on Their Submitted Idea (Priority: P2)

A submitter wants to know what evaluators think about their idea. They can view the current status of their idea, read evaluator comments, and understand next steps.

**Why this priority**: This provides feedback loop closure. While not essential for the first submission, it's important for user engagement and learning within the innovation cycle.

**Independent Test**: Can be tested by submitting an idea, having an evaluator add comments, and verifying the submitter can read those comments.

**Acceptance Scenarios**:

1. **Given** a submitter is viewing their submitted idea, **When** an evaluator has added comments, **Then** the comments are visible to the submitter
2. **Given** an idea's status has changed, **When** the submitter views their idea detail, **Then** the current status is clearly displayed
3. **Given** an idea is rejected with a comment, **When** the submitter views it, **Then** they see the rejection reason provided by the evaluator

---

### Edge Cases

- What happens when a submitter tries to upload a file that exceeds a reasonable size limit (e.g., > 50MB)?
- What happens when a file with an unsupported format (e.g., .exe, .zip) is uploaded?
- How does the system handle when an evaluator deletes an evaluation comment that was already submitted?
- What happens if two evaluators try to change an idea's status simultaneously?
- How are ideas handled if a user with 'submitter' role is later downgraded in permissions?
- What happens if an idea's attachment file becomes corrupted or unavailable?
- Can a submitter edit or delete their own submitted idea? What states allow this?
- What happens when an evaluator tries to transition an idea to an invalid status (e.g., directly from Submitted to Accepted without Under Review)?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST authenticate all users via the existing JWT token system before allowing any action
- **FR-002**: System MUST restrict idea submission form access to users with the 'submitter' role
- **FR-003**: System MUST validate that submission form has Title (required), Description (required), and Category (required) fields
- **FR-004**: System MUST allow users to attach a single file (PDF or PNG) to an idea submission
- **FR-005**: System MUST validate that uploaded files are either PDF or PNG format
- **FR-006**: System MUST store uploaded files securely and associate them with the corresponding idea
- **FR-007**: System MUST persist submitted ideas in a database with metadata (submitter, submission timestamp, status, category)
- **FR-008**: System MUST display an idea listing view showing all ideas currently in the system
- **FR-009**: System MUST show each idea's title, submitter name, submission date, current status, and category on the listing view
- **FR-010**: System MUST restrict evaluation workflow access to users with 'evaluator' or 'admin' role
- **FR-011**: System MUST support changing an idea's status through valid workflow states: Submitted → Under Review → (Accepted or Rejected)
- **FR-012**: System MUST enforce that ideas are created with initial status "Submitted"
- **FR-013**: System MUST allow evaluators to add comments during the evaluation process
- **FR-014**: System MUST display attached files in the idea detail view so they can be accessed by authorized users
- **FR-015**: System MUST log all status changes and evaluation comments with timestamps and the evaluator's identity
- **FR-016**: System MUST prevent unauthorized users from modifying other users' ideas
- **FR-017**: System MUST provide a detail view for each idea showing all information (description, attached file, status, all evaluation comments)
- **FR-018**: System MUST ensure submitters can view the status and evaluation comments on their own ideas

### Key Entities

- **Idea**: Represents a submitted innovation proposal. Contains Title (text), Description (text), Category (enumeration), Status (enumeration: Submitted/Under Review/Accepted/Rejected), SubmitterId (reference to User), SubmissionTimestamp (datetime), AttachedFile (optional reference to File)
- **IdeaEvaluation**: Represents evaluator feedback on an idea. Contains EvaluatorId (reference to User), IdeaId (reference to Idea), Comment (text), Timestamp (datetime), StatusChange (what status was set, if applicable)
- **File**: Represents an uploaded attachment. Contains FileName (text), FileType (PDF or PNG), FileSize (number), StorageLocation (reference to where file is stored), UploadTimestamp (datetime)

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Submitters can complete idea submission (form fill + file upload) in under 3 minutes
- **SC-002**: An evaluator can view all submitted ideas and their details within 5 seconds of accessing the listing page
- **SC-003**: An evaluator can change an idea's status and add a comment in under 1 minute
- **SC-004**: 95% of file uploads complete without errors on valid PDF/PNG files under 50MB
- **SC-005**: Submitters receive feedback (status updates and comments from evaluators) within 24 hours of submission (process metric, not system metric)
- **SC-006**: System correctly enforces that only authenticated users with appropriate roles can access their respective features (100% enforcement)
- **SC-007**: All submitted ideas are persisted and retrievable even after system restart (zero data loss on valid submissions)

## Assumptions

- **Authentication**: The existing JWT authentication system is fully functional and available. This feature does not implement authentication but uses the existing system.
- **User Roles**: The system has established 'submitter', 'evaluator', and 'admin' roles assigned to users. Role management is external to this feature.
- **File Storage**: A file storage mechanism exists (local filesystem, cloud storage, etc.). This feature assumes file persistence is handled.
- **Database**: A relational database is available and accessible to store idea records and evaluation data.
- **File Size Limits**: A reasonable file size limit of 50MB is assumed; if different, this should be configured as a system parameter.
- **Concurrency**: The system has basic concurrency handling; race conditions on simultaneous status changes are unlikely in early usage.
- **Supported Formats**: Only PDF and PNG files are required; no other formats are needed for MVP.

## Dependencies

- **Depends On**: User Authentication System (JWT-based login and role management must be functional)
- **Related To**: Future notification system (could notify submitters of status changes, but not required for MVP)
- **Blocked By**: None identified

## Out of Scope

- Notification system (email/in-app alerts about status changes)
- Advanced search and filtering of ideas
- Idea versioning or edit history
- User permissions beyond role-based access
- Analytics or reporting on idea trends
- Approval workflows (e.g., multi-step evaluation by multiple people)
- Comment threading or discussions
- Idea assignment to specific evaluators
- Bulk operations on ideas
