package mk.ukim.finki.routingsystem.web;

import mk.ukim.finki.routingsystem.model.dto.DocumentVersion.DisplayDocumentVersionDto;
import mk.ukim.finki.routingsystem.model.dto.DocumentVersion.UpdateDocumentAndVersionDto;
import mk.ukim.finki.routingsystem.security.EmployeePrincipal;
import mk.ukim.finki.routingsystem.service.DocumentVersionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/documents")
public class DocumentVersionRestController {

    private final DocumentVersionService documentVersionService;

    public DocumentVersionRestController(DocumentVersionService documentVersionService) {
        this.documentVersionService = documentVersionService;
    }


    @GetMapping("{documentId}/allVersions")
    public ResponseEntity<Page<DisplayDocumentVersionDto>> getAllVersions(@PathVariable Long documentId,
                                                                   Pageable pageable) {

        Page<DisplayDocumentVersionDto> displayDocumentVersionDto = documentVersionService.listAllVersionsOfADocument(documentId, pageable);

        return ResponseEntity.ok(displayDocumentVersionDto);
    }

    @PutMapping(value = "{documentId}/editDocument", consumes = {"multipart/form-data"})
    public ResponseEntity<DisplayDocumentVersionDto> updateDocumentAndVersion(@PathVariable Long documentId,
                                                                              @RequestParam (required = false) MultipartFile file,
                                                                              @RequestParam String title,
                                                                              @RequestParam String changeNote,
                                                                              @AuthenticationPrincipal EmployeePrincipal employeePrincipal) throws IOException {

        byte [] fileData = (file != null && !file.isEmpty()) ? file.getBytes() : null;


        UpdateDocumentAndVersionDto updateDocumentAndVersionDto = new UpdateDocumentAndVersionDto(
                title != null ? title.trim() : null,
                employeePrincipal.getEmployeeId(),
                changeNote != null ? changeNote.trim() : null,
                fileData
        );

        DisplayDocumentVersionDto documentVersionDto = documentVersionService.updateAndSaveDocumentVersion(documentId, updateDocumentAndVersionDto);

        return ResponseEntity.ok(documentVersionDto);
    }

    @GetMapping("version/{versionId}")
    public ResponseEntity<DisplayDocumentVersionDto> getDocumentVersion(@PathVariable Long versionId) {
        return ResponseEntity.ok(documentVersionService.getDocumentVersion(versionId));
    }
}
