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

        DisplayDocumentDto created = documentService.createDocumentAndDocumentVersion(documentDto, file, employeePrincipal.getEmployeeId(), employeePrincipal.getCompanyId());

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{documentId}/route")
    public ResponseEntity<DisplayDocumentDto> routeDocument(@PathVariable Long documentId,
                                                            @AuthenticationPrincipal EmployeePrincipal employeePrincipal) {

        DisplayDocumentDto routed = documentService.routeDocument(documentId, employeePrincipal.getEmployeeId(), employeePrincipal.getCompanyId());

        return ResponseEntity.status(HttpStatus.OK).body(routed);

    }

    @GetMapping("/{documentId}/manual-review/departments")
    public ResponseEntity<List<CreateDisplayDepartmentDto>> manualReviewDepartments(@PathVariable Long documentId,
                                                                                    @AuthenticationPrincipal EmployeePrincipal employeePrincipal) {
        List<CreateDisplayDepartmentDto> departmentDtos = departmentService.getManualReviewDepartments(documentId, employeePrincipal.getCompanyId());

        return ResponseEntity.status(HttpStatus.OK).body(departmentDtos);
    }

    @PostMapping("/{documentId}/manual-review")
    public ResponseEntity<DisplayDocumentDto> manualRouteDocument(@PathVariable Long documentId,
                                                                  @RequestParam String departmentKey,
                                                                  @AuthenticationPrincipal EmployeePrincipal principal) {

        DisplayDocumentDto result = documentService.manualRouteDocument(documentId, principal.getEmployeeId(), principal.getCompanyId(), departmentKey);

        return ResponseEntity.ok(result);
    }

    @PreAuthorize("@documentAuth.canSign(#documentId, authentication.principal.companyId, authentication)")
    @PutMapping("/{documentId}/approve")
    public ResponseEntity<DisplayDocumentDto> approveDocument(@PathVariable Long documentId,
                                                              @AuthenticationPrincipal EmployeePrincipal employeePrincipal) {


        boolean approved = documentService.approveDocument(documentId, employeePrincipal.getEmployeeId(), employeePrincipal.getCompanyId());
        return approved ? ResponseEntity.noContent().build() : ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @PreAuthorize("@documentAuth.canSign(#documentId, authentication.principal.companyId, authentication)")
    @PutMapping("{documentId}/reject")
    public ResponseEntity<DisplayDocumentDto> rejectDocument(@PathVariable Long documentId,
                                                             @AuthenticationPrincipal EmployeePrincipal employeePrincipal) {

        boolean rejected = documentService.rejectDocument(documentId, employeePrincipal.getEmployeeId(), employeePrincipal.getCompanyId());

        return rejected ? ResponseEntity.noContent().build() : ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @GetMapping("/{documentId}")
    public ResponseEntity<DisplayDocumentDto> getDocumentWithVersions(@PathVariable Long documentId,
                                                                      @AuthenticationPrincipal EmployeePrincipal employeePrincipal) {

        return ResponseEntity.ok(documentService.findAllWithVersions(documentId, employeePrincipal.getCompanyId()));
    }

    @GetMapping("/routedToMe/inbox")
    public ResponseEntity<Page<DisplayDocumentDto>> getRoutedToEmployeeInbox(@AuthenticationPrincipal EmployeePrincipal employeePrincipal,
                                                                             Pageable pageable) {

        Page<DisplayDocumentDto> displayDocumentDto = documentService.findAllByRoutedToEmployee(List.of(DocumentStatus.ROUTED), employeePrincipal.getEmployeeId(), employeePrincipal.getCompanyId(), pageable);

        return ResponseEntity.ok(displayDocumentDto);

    }

    @GetMapping("/uploadedDocuments")
    public ResponseEntity<Page<DisplayDocumentDto>> getUploadedDocuments(@AuthenticationPrincipal EmployeePrincipal employeePrincipal,
                                                                         Pageable pageable) {
        Page<DisplayDocumentDto> displayDocumentDto = documentService.findAllUploadedByEmployee(List.of(DocumentStatus.ROUTED, DocumentStatus.REJECTED, DocumentStatus.APPROVED), employeePrincipal.getEmployeeId(), employeePrincipal.getCompanyId(), pageable);
        return ResponseEntity.ok(displayDocumentDto);
    }

    @GetMapping("/routedToMe/history")
    public ResponseEntity<Page<DisplayDocumentDto>> getRoutedToEmployeeHistory(@AuthenticationPrincipal EmployeePrincipal employeePrincipal,
                                                                               Pageable pageable) {

        Page<DisplayDocumentDto> displayDocumentDto = documentService.findAllByRoutedToEmployee(List.of(DocumentStatus.APPROVED, DocumentStatus.REJECTED, DocumentStatus.DOWNLOADED), employeePrincipal.getEmployeeId(), employeePrincipal.getCompanyId(), pageable);

        return ResponseEntity.ok(displayDocumentDto);

    }

    @GetMapping("/routedToMyDepartment")
    public ResponseEntity<Page<DisplayDocumentDto>> getRoutedToEmployeeDepartment(@AuthenticationPrincipal EmployeePrincipal employeePrincipal,
                                                                                  Pageable pageable) {

        if (employeePrincipal.getDepartmentId() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Page<DisplayDocumentDto> documentDto = documentService.findAllByRoutedToDepartment(employeePrincipal.getDepartmentId(), employeePrincipal.getCompanyId(), pageable);

        return ResponseEntity.ok(documentDto);

    }


}










