# RoutingSystemApplication
A Spring Boot application for managing document routing, approvals and version control within an organization. Employees can upload documents, which are automatically routed to the appropriate department, while the system maintains version history and supports actions such as signing-approval and rejection.

### Key Features

- Authentication & Security  
  - JWT-based authentication & authorization  
  - Role-based access (ADMIN, EMPLOYEE)  
  - Secure endpoints with @PreAuthorize  

- Document Management  
  - Upload and manage documents with multiple versions  
  - Track status (PENDING, APPROVED, REJECTED, etc.)  
  - Route documents to specific departments and employees with SIGNATORY employeeType  
  - Download latest or specific versions  

- Actions & Events  
  - Approve or reject documents  
  - Application events for logging actions (all status changes on a document)  

- Departments & Employees  
  - Manage departments and employees  
  - View documents routed to a specific department  
  - Admin-only operations for organization-wide visibility  

- Integration & Testing  
  - PostgreSQL for persistent storage  
  - API testing with Postman collections

# API Endpoints

### Authentication
- **POST /auth/login** – Authenticate with email and password and receive a JWT token.  
- **POST /auth/logout** – Invalidate the current JWT session (client-side token removal).  

### Document
- **POST /api/documents** – Create and upload a new document.  
- **PUT /api/documents/{documentId}/route** – Route a document to the appropriate department.  
- **PUT /api/documents/{documentId}/approve** – Approve a document routed to the employee or department.  
- **PUT /api/documents/{documentId}/reject** – Reject a document routed to the employee or department.  
- **GET /api/documents/{documentId}** – Retrieve a document with all its versions and details.  
- **GET /api/documents/routedToMe/inbox** – List all documents currently routed to the employee’s inbox.  
- **GET /api/documents/routedToMe/history** – Show history of all documents previously routed to the employee.  
- **GET /api/documents/routedToMyDepartment** – List documents routed to the employee’s department.  

### Document Version
- **GET /api/documents/{documentId}/allVersions** – Retrieve all versions of a given document.  
- **PUT /api/documents/{documentId}/editDocument** – Update a document and create a new version entry.  

### Document Action
- **GET /api/documents/{documentId}/documentActions** – Get a list of all actions performed on a document.  
- **GET /api/documents/{documentId}/actionsByMe** – Get all actions performed on a document by the currently logged-in employee.  

### Document Download
- **GET /api/documents/{documentId}/versions/{versionId}/download** – Download a specific version of a document as a PDF.  
- **GET /api/documents/{documentId}/documentDownloads** – List all downloads performed for a specific document.  
- **GET /api/documents/downloadsByMe** – List all documents downloaded by the logged-in employee.  

### Admin Document
- **GET /api/admin/departments/{departmentId}/routed-documents** – For admins: list all documents routed to a specific department.  
- **GET /api/admin/employees/{employeeId}/documents/{documentId}/actions** – For admins: list all actions performed by a specific employee on a given document.  

### Employee
- **GET /api/employees/{id}** – Retrieve details of a specific employee by ID.  
- **PUT /api/employees/{id}** – Update employee information.  
- **DELETE /api/employees/{id}** – Delete an employee from the system.  

### Department
- **GET /api/departments/{id}** – Retrieve details of a specific department by ID.  
- **PUT /api/departments/{id}** – Update department information.  
- **DELETE /api/departments/{id}** – Delete a department from the system.  

 
### Tech Stack
- **Java 21**, **Spring Boot**
- **PostgreSQL**, **pgAdmin**
- **Spring Security + JWT**
- **Maven**
- **Postman** for API testing

### Flow Example
1. Employee logs in and uploads a document.  
2. The system automatically routes the document to the correct department.  
3. A signatory employee reviews and approves or rejects the document.  
4. All document versions and actions are tracked in the system.  
5. Admin can audit routed documents, actions and downloads across departments.  



