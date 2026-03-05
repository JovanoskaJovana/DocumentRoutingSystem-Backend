package mk.ukim.finki.routingsystem.web;

import mk.ukim.finki.routingsystem.model.dto.DocumentAction.DisplayDocumentActionDto;
import mk.ukim.finki.routingsystem.security.EmployeePrincipal;
import mk.ukim.finki.routingsystem.service.DocumentActionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentActionRestController {

    private final DocumentActionService documentActionService;


    public DocumentActionRestController(DocumentActionService documentActionService) {
        this.documentActionService = documentActionService;
    }

    @GetMapping("{documentId}/documentActions")
    public ResponseEntity<List<DisplayDocumentActionDto>> getAllDocumentActions(@PathVariable Long documentId,
                                                                                @AuthenticationPrincipal EmployeePrincipal employeePrincipal) {

        return ResponseEntity.ok(documentActionService.findAllForADocument(documentId, employeePrincipal.getCompanyId()));
    }

    @GetMapping("/{documentId}/actionsByMe")
    public ResponseEntity<List<DisplayDocumentActionDto>> getAllActionsByEmployeeId(@PathVariable Long documentId,
                                                                                    @AuthenticationPrincipal EmployeePrincipal employeePrincipal) {

        return ResponseEntity.ok(documentActionService.findAllBySpecificEmployee(documentId, employeePrincipal.getEmployeeId(), employeePrincipal.getCompanyId()));
    }

}
