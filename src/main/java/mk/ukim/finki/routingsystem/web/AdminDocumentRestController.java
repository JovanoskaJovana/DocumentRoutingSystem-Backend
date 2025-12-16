package mk.ukim.finki.routingsystem.web;

import mk.ukim.finki.routingsystem.model.dto.Document.DisplayAdminDocumentDto;

import mk.ukim.finki.routingsystem.model.dto.DocumentAction.DisplayDocumentActionDto;
import mk.ukim.finki.routingsystem.model.exceptions.DepartmentNotFoundException;
import mk.ukim.finki.routingsystem.repository.DepartmentRepository;
import mk.ukim.finki.routingsystem.service.DocumentActionService;
import mk.ukim.finki.routingsystem.service.DocumentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/admin")
public class AdminDocumentRestController {

    private final DocumentService documentService;
    private final DepartmentRepository departmentRepository;
    private final DocumentActionService documentActionService;

    public AdminDocumentRestController(DocumentService documentService, DepartmentRepository departmentRepository, DocumentActionService documentActionService) {
        this.documentService = documentService;
        this.departmentRepository = departmentRepository;
        this.documentActionService = documentActionService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/departments/{departmentId}/routed-documents")
    public ResponseEntity<Page<DisplayAdminDocumentDto>> getRoutedToDepartmentByAdmin( @PathVariable Long departmentId,
                                                                                       Pageable pageable) {
        departmentRepository.findById(departmentId)
                .orElseThrow(() -> new DepartmentNotFoundException("Department not found."));


        Page<DisplayAdminDocumentDto> adminDocumentDto = documentService.findAllByRoutedToDepartmentByAdmin(departmentId, pageable);

        return ResponseEntity.ok(adminDocumentDto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/employees/{employeeId}/documents/{documentId}/actions")
    public ResponseEntity<List<DisplayDocumentActionDto>> listAllActionsFromEmployee (@PathVariable Long employeeId,
                                                                                      @PathVariable Long documentId) {

        List<DisplayDocumentActionDto> documentActions = documentActionService.findAllBySpecificEmployee(documentId, employeeId);

        return documentActions.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(documentActions);

    }
}

