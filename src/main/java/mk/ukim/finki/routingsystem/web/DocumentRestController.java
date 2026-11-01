package mk.ukim.finki.routingsystem.web;

import mk.ukim.finki.routingsystem.model.dto.CreateDisplayDepartmentDto;
import mk.ukim.finki.routingsystem.model.dto.Document.CreateDocumentDto;
import mk.ukim.finki.routingsystem.model.dto.Document.DisplayDocumentDto;
import mk.ukim.finki.routingsystem.model.enumerations.DocumentStatus;
import mk.ukim.finki.routingsystem.security.EmployeePrincipal;
import mk.ukim.finki.routingsystem.service.DepartmentService;
import mk.ukim.finki.routingsystem.service.DocumentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * REST controller for managing documents.
 */

@RestController
@RequestMapping("/api/documents")
public class DocumentRestController {

  public final DocumentService documentService;
  private final DepartmentService departmentService;

  public DocumentRestController(DocumentService documentService, DepartmentService departmentService) {
    this.documentService = documentService;
    this.departmentService = departmentService;
  }


  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<DisplayDocumentDto> createDocument(@RequestPart("data") CreateDocumentDto documentDto,
                                                           @RequestPart("file") MultipartFile file,
                                                           @AuthenticationPrincipal EmployeePrincipal employeePrincipal) throws IOException {

    DisplayDocumentDto created = documentService.createDocumentAndDocumentVersion(documentDto, file, employeePrincipal.employeeId(), employeePrincipal.companyId());

    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @PutMapping("/{documentId}/route")
  public ResponseEntity<DisplayDocumentDto> routeDocument(@PathVariable Long documentId,
                                                          @AuthenticationPrincipal EmployeePrincipal employeePrincipal) {

    DisplayDocumentDto routed = documentService.routeDocument(documentId, employeePrincipal.employeeId(), employeePrincipal.companyId());

    return ResponseEntity.status(HttpStatus.OK).body(routed);

  }

  @GetMapping("/{documentId}/manual-review/departments")
  public ResponseEntity<List<CreateDisplayDepartmentDto>> manualReviewDepartments(@PathVariable Long documentId,
                                                                                  @AuthenticationPrincipal EmployeePrincipal employeePrincipal) {
    List<CreateDisplayDepartmentDto> departmentDtos = departmentService.getManualReviewDepartments(documentId, employeePrincipal.companyId());

    return ResponseEntity.status(HttpStatus.OK).body(departmentDtos);
  }

  @PostMapping("/{documentId}/manual-review")
  public ResponseEntity<DisplayDocumentDto> manualRouteDocument(@PathVariable Long documentId,
                                                                @RequestParam String departmentKey,
                                                                @AuthenticationPrincipal EmployeePrincipal principal) {

    DisplayDocumentDto result = documentService.manualRouteDocument(documentId, principal.employeeId(), principal.companyId(), departmentKey);

    return ResponseEntity.ok(result);
  }

  @PreAuthorize("@documentAuth.canSign(#documentId, authentication.principal.companyId, authentication)")
  @PutMapping("/{documentId}/approve")
  public ResponseEntity<DisplayDocumentDto> approveDocument(@PathVariable Long documentId,
                                                            @AuthenticationPrincipal EmployeePrincipal employeePrincipal) {

    documentService.approveDocument(documentId, employeePrincipal.employeeId(), employeePrincipal.companyId());
    return ResponseEntity.noContent().build();
  }

  @PreAuthorize("@documentAuth.canSign(#documentId, authentication.principal.companyId, authentication)")
  @PutMapping("{documentId}/reject")
  public ResponseEntity<DisplayDocumentDto> rejectDocument(@PathVariable Long documentId,
                                                           @AuthenticationPrincipal EmployeePrincipal employeePrincipal) {

    documentService.rejectDocument(documentId, employeePrincipal.employeeId(), employeePrincipal.companyId());
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{documentId}")
  public ResponseEntity<DisplayDocumentDto> getDocumentWithVersions(@PathVariable Long documentId,
                                                                    @AuthenticationPrincipal EmployeePrincipal employeePrincipal) {

    return ResponseEntity.ok(documentService.findAllWithVersions(documentId, employeePrincipal.companyId()));
  }

  @GetMapping("/routedToMe/inbox")
  public ResponseEntity<Page<DisplayDocumentDto>> getRoutedToEmployeeInbox(@AuthenticationPrincipal EmployeePrincipal employeePrincipal,
                                                                           Pageable pageable) {

    Page<DisplayDocumentDto> displayDocumentDto = documentService.findAllByRoutedToEmployee(List.of(DocumentStatus.ROUTED), employeePrincipal.employeeId(), employeePrincipal.companyId(), pageable);

    return ResponseEntity.ok(displayDocumentDto);

  }

  @GetMapping("/uploadedDocuments")
  public ResponseEntity<Page<DisplayDocumentDto>> getUploadedDocuments(@AuthenticationPrincipal EmployeePrincipal employeePrincipal,
                                                                       Pageable pageable) {
    Page<DisplayDocumentDto> displayDocumentDto = documentService.findAllUploadedByEmployee(List.of(DocumentStatus.ROUTED, DocumentStatus.REJECTED, DocumentStatus.APPROVED), employeePrincipal.employeeId(), employeePrincipal.companyId(), pageable);
    return ResponseEntity.ok(displayDocumentDto);
  }

  @GetMapping("/routedToMe/history")
  public ResponseEntity<Page<DisplayDocumentDto>> getRoutedToEmployeeHistory(@AuthenticationPrincipal EmployeePrincipal employeePrincipal,
                                                                             Pageable pageable) {

    Page<DisplayDocumentDto> displayDocumentDto = documentService.findAllByRoutedToEmployee(List.of(DocumentStatus.APPROVED, DocumentStatus.REJECTED, DocumentStatus.DOWNLOADED), employeePrincipal.employeeId(), employeePrincipal.companyId(), pageable);

    return ResponseEntity.ok(displayDocumentDto);

  }

  @GetMapping("/routedToMyDepartment")
  public ResponseEntity<Page<DisplayDocumentDto>> getRoutedToEmployeeDepartment(@AuthenticationPrincipal EmployeePrincipal employeePrincipal,
                                                                                Pageable pageable) {

    if (employeePrincipal.departmentId() == null) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    Page<DisplayDocumentDto> documentDto = documentService.findAllByRoutedToDepartment(employeePrincipal.departmentId(), employeePrincipal.companyId(), pageable);

    return ResponseEntity.ok(documentDto);

  }

}










