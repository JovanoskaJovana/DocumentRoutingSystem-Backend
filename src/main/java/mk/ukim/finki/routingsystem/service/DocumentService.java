package mk.ukim.finki.routingsystem.service;

import mk.ukim.finki.routingsystem.model.dto.Document.CreateDocumentDto;
import mk.ukim.finki.routingsystem.model.dto.Document.DisplayAdminDocumentDto;
import mk.ukim.finki.routingsystem.model.dto.Document.DisplayDocumentDto;
import mk.ukim.finki.routingsystem.model.enumerations.DocumentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Service interface for managing {@link mk.ukim.finki.routingsystem.model.documentEntities.Document} entities.
 */

public interface DocumentService {

  /**
   * Retrieves a paginated list of all documents routed to a given department within a company.
   *
   * @param departmentId the id of the given department
   * @param companyId    the id of the given company
   * @param pageable     pagination and sorting information
   * @return a {@link Page} of {@link DisplayDocumentDto} representing all the documents that match the department and company
   */
  Page<DisplayDocumentDto> findAllByRoutedToDepartment(Long departmentId, Long companyId, Pageable pageable);

  /**
   * Retrieves a paginated list of all documents routed to a given department within a company, intended for use by admin employees.
   *
   * @param departmentId the id of the given department
   * @param companyId    the id of the given company
   * @param pageable     pagination and sorting information
   * @return a {@link Page} of {@link DisplayAdminDocumentDto} representing all the documents that match the department and company
   */
  Page<DisplayAdminDocumentDto> findAllByRoutedToDepartmentByAdmin(Long departmentId, Long companyId, Pageable pageable);

  /**
   * Retrieves a paginated list of all documents that have a status matching the given list of statuses and are routed to the given employee within a company.
   *
   * @param documentStatuses the given list of statuses
   * @param employeeId       the id of the given employee
   * @param companyId        the id of the given company
   * @param pageable         pagination and sorting information
   * @return a {@link Page} of {@link DisplayDocumentDto} representing all the documents that match the employee, statuses and the company
   */
  Page<DisplayDocumentDto> findAllByRoutedToEmployee(List<DocumentStatus> documentStatuses, Long employeeId, Long companyId, Pageable pageable);

  /**
   * Retrieves a paginated list of all documents that have a status matching the given list of statuses and are uploaded by the given employee within a company.
   *
   * @param documentStatuses the given list of statuses
   * @param employeeId       the id of the given employee
   * @param companyId        the id of the given company
   * @param pageable         pagination and sorting information
   * @return a {@link Page} of {@link DisplayDocumentDto} representing all the documents that match the employee, statuses and the company
   */
  Page<DisplayDocumentDto> findAllUploadedByEmployee(List<DocumentStatus> documentStatuses, Long employeeId, Long companyId, Pageable pageable);

  /**
   * Retrieves a document with all of its versions within a company.
   *
   * @param documentId the id of the given document
   * @param companyId  the id of the given company
   * @return a {@link DisplayDocumentDto} that matches the document and company
   */
  DisplayDocumentDto findAllWithVersions(Long documentId, Long companyId);

  /**
   * Creates a new document and its first version within a company.
   *
   * @param documentDto the data required to create the document
   * @param file        the PDF file to be uploaded
   * @param uploaderId  the id of the employee uploading the document
   * @param companyId   the id of the given company
   * @return a {@link DisplayDocumentDto} representing the created document
   * @throws IOException                                                            if an error occurs while processing the uploaded file
   * @throws mk.ukim.finki.routingsystem.model.exceptions.EmployeeNotFoundException if the given id of the employee is not found
   * @throws mk.ukim.finki.routingsystem.model.exceptions.CompanyNotFoundException  if the given id of the company is not found
   */
  DisplayDocumentDto createDocumentAndDocumentVersion(CreateDocumentDto documentDto, MultipartFile file, Long uploaderId, Long companyId) throws IOException;

  /**
   * Automatically routes the document to the correct department within a company.
   *
   * @param documentId the id of the document that is routed
   * @param employeeId the id of the employee that routes the document
   * @param companyId  the id of the given company
   * @return a {@link DisplayDocumentDto} representing the routed document
   * @throws mk.ukim.finki.routingsystem.model.exceptions.EmployeeNotFoundException        if the given id of the employee is not found
   * @throws mk.ukim.finki.routingsystem.model.exceptions.DocumentNotFoundException        if the given id of the document is not found
   * @throws mk.ukim.finki.routingsystem.model.exceptions.DocumentVersionNotFoundException if the given id of the document version is not found
   * @throws mk.ukim.finki.routingsystem.model.exceptions.DepartmentNotFoundException      if the given id of the department is not found
   */
  DisplayDocumentDto routeDocument(Long documentId, Long employeeId, Long companyId);

  /**
   * Manually choosing the department to which the document should be routed within a company.
   *
   * @param documentId    the id of the document that needs to be routed
   * @param employeeId    the id of the employee that routes the document
   * @param companyId     the id of the given company
   * @param departmentKey the key of the department to which the document should be routed
   * @return a {@link DisplayDocumentDto} representing the routed document
   * @throws mk.ukim.finki.routingsystem.model.exceptions.EmployeeNotFoundException        if the given id of the employee is not found
   * @throws mk.ukim.finki.routingsystem.model.exceptions.DocumentNotFoundException        if the given id of the document is not found
   * @throws mk.ukim.finki.routingsystem.model.exceptions.DocumentVersionNotFoundException if the given id of the document version is not found
   * @throws mk.ukim.finki.routingsystem.model.exceptions.DepartmentNotFoundException      if the given id of the department is not found
   */
  DisplayDocumentDto manualRouteDocument(Long documentId, Long employeeId, Long companyId, String departmentKey);

  /**
   * Approves a document within a company.
   *
   * @param documentId the id of the given document
   * @param employeeId the id of the employee that approves the document
   * @param companyId  the id of the given company
   * @throws mk.ukim.finki.routingsystem.model.exceptions.EmployeeNotFoundException       if the given id of the employee is not found
   * @throws mk.ukim.finki.routingsystem.model.exceptions.DocumentNotFoundException       if the given id of the document is not found
   * @throws mk.ukim.finki.routingsystem.model.exceptions.DocumentAlreadyDecidedException if the document was already approved or rejected
   * @throws mk.ukim.finki.routingsystem.model.exceptions.InvalidDocumentStateException   if the document is not routed
   * @throws org.springframework.security.access.AccessDeniedException                   if the employee is not a signatory
   */
  void approveDocument(Long documentId, Long employeeId, Long companyId);

  /**
   * Rejects a document within a company.
   *
   * @param documentId the id of the given document
   * @param employeeId the id of the employee that rejects the document
   * @param companyId  the id of the given company
   * @throws mk.ukim.finki.routingsystem.model.exceptions.EmployeeNotFoundException       if the given id of the employee is not found
   * @throws mk.ukim.finki.routingsystem.model.exceptions.DocumentNotFoundException       if the given id of the document is not found
   * @throws mk.ukim.finki.routingsystem.model.exceptions.DocumentAlreadyDecidedException if the document was already approved or rejected
   * @throws mk.ukim.finki.routingsystem.model.exceptions.InvalidDocumentStateException   if the document is not routed
   * @throws org.springframework.security.access.AccessDeniedException                   if the employee is not a signatory
   */
  void rejectDocument(Long documentId, Long employeeId, Long companyId);
}
