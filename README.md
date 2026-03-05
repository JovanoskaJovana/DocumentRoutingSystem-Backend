# Flowdesk Backend

A multi-tenant Spring Boot application for intelligent document routing, approvals and version control within organizations. Employees upload documents which are automatically routed to the correct department using a weighted keyword scoring engine, while the system maintains full version history and allows signatory employees to approve or reject them.

## Key Features

- **Multi-Tenancy**
  - Full tenant isolation — each company has its own routing rules, departments and employees
  - All queries scoped to the authenticated employee's company
  - Company code used as tenant identifier for routing configuration

- **Intelligent Document Routing**
  - Weighted keyword scoring engine with strong, weak and negative word lists per department
  - Positional weighting — title matches score higher than body matches
  - Configurable confidence threshold and minimum score difference to prevent ambiguous routing
  - Tie detection — flags documents for manual review when two departments score equally
  - Manual review flow — user selects department when automatic routing fails
  - Human-in-the-loop feedback loop — stores manual routing decisions and analyzes document content over time to suggest new keywords per department

- **Authentication & Security**
  - JWT-based authentication and authorization
  - Role-based access (SUPER_ADMIN, ADMIN, EMPLOYEE)
  - Secure endpoints with @PreAuthorize
  - Company-scoped login with company code

- **Document Management**
  - Upload and manage documents with automatic versioning
  - Track document status (UPLOADED, ROUTED, FAILED_ROUTING, APPROVED, REJECTED, EDITED)
  - Route documents to specific departments and signatory employees
  - Re-routing on document edit
  - Download specific or latest document versions

- **Actions & Events**
  - Approve or reject routed documents
  - Application events for logging all status changes
  - Full audit trail per document

- **Departments & Employees**
  - Manage departments and employees per company
  - View documents routed to a specific department
  - Admin-only operations for organization-wide visibility
  - Keyword suggestions for admins based on manual routing history

## API Endpoints

### Authentication
- **POST /api/auth/login** – Authenticate with email, password and company code. Returns a JWT token.
- **POST /api/auth/logout** – Invalidate the current JWT session (client-side token removal).

### Document
- **POST /api/documents** – Upload a new document.
- **POST /api/documents/{documentId}/route** – Automatically route a document using the scoring engine.
- **POST /api/documents/{documentId}/manual-review** – Manually route a document to a chosen department.
- **GET /api/documents/{documentId}/manual-review/departments** – Get departments to display for manual review.
- **PUT /api/documents/{documentId}/approve** – Approve a routed document.
- **PUT /api/documents/{documentId}/reject** – Reject a routed document.
- **GET /api/documents/{documentId}** – Retrieve a document with all its versions and details.
- **GET /api/documents/routedToMe/inbox** – List all documents currently routed to the employee's inbox.
- **GET /api/documents/routedToMe/history** – Show history of all documents previously routed to the employee.
- **GET /api/documents/routedToMyDepartment** – List documents routed to the employee's department.

### Document Version
- **GET /api/documents/{documentId}/allVersions** – Retrieve all versions of a document.
- **PUT /api/documents/{documentId}/editDocument** – Update a document and create a new version. Resets routing status.

### Document Action
- **GET /api/documents/{documentId}/documentActions** – Get all actions performed on a document.
- **GET /api/documents/{documentId}/actionsByMe** – Get all actions performed on a document by the logged-in employee.

### Document Download
- **GET /api/documents/{documentId}/versions/{versionId}/download** – Download a specific version as a PDF.
- **GET /api/documents/{documentId}/documentDownloads** – List all downloads for a document.
- **GET /api/documents/downloadsByMe** – List all documents downloaded by the logged-in employee.

### Admin
- **GET /api/admin/departments/{departmentId}/routed-documents** – List all documents routed to a department.
- **GET /api/admin/employees/{employeeId}/documents/{documentId}/actions** – List all actions by a specific employee on a document.

### Keyword Suggestions
- **GET /api/suggestions** – Returns keyword suggestions for all departments based on manual routing history. Admin only.

### Employee
- **GET /api/employees/{id}** – Retrieve a specific employee.
- **PUT /api/employees/{id}** – Update employee information.
- **DELETE /api/employees/{id}** – Delete an employee.

### Department
- **GET /api/departments/{id}** – Retrieve a specific department.
- **PUT /api/departments/{id}** – Update department information.
- **DELETE /api/departments/{id}** – Delete a department.

## Routing Algorithm

The scoring engine assigns each document a score per department by iterating through three keyword lists — strong, weak and negative — configured per tenant in `application.yml`. Each keyword match is weighted by both its strength tier and its position in the document (title vs body):

```
score += titleMatches × titleWeight × strengthWeight
score += bodyMatches  × bodyWeight  × strengthWeight
```

Default weights:
| Position | Weight |
|----------|--------|
| Title    | 3.0    |
| Body     | 1.0    |

| Strength | Weight |
|----------|--------|
| Strong   | 1.0    |
| Weak     | 0.4    |
| Negative | -2.0   |

The department with the highest score wins, provided it clears the minimum confidence threshold and leads the second-place department by the minimum required difference. Otherwise the document is flagged for manual review.

## Tech Stack
- **Java 21**, **Spring Boot**
- **PostgreSQL**, **pgAdmin**
- **Spring Security + JWT**
- **MapStruct**, **JPA/Hibernate**
- **Maven**
- **Docker**
- **Postman** for API testing

## Flow Example
1. Employee logs in with their company code and credentials.
2. Employee uploads a document — system creates version 1.
3. The scoring engine analyzes the document and routes it to the highest-scoring department.
4. If routing confidence is too low or there is a tie, the document is flagged for manual review and the employee selects a department.
5. A signatory employee in the routed department approves or rejects the document.
6. All versions, actions and routing decisions are tracked throughout.
7. Admin reviews keyword suggestions generated from manual routing history to improve future routing accuracy.
