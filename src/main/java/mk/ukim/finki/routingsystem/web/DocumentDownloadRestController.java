package mk.ukim.finki.routingsystem.web;

import mk.ukim.finki.routingsystem.events.DocumentActionRequestedEvent;
import mk.ukim.finki.routingsystem.events.DocumentDownloadRequestedEvent;
import mk.ukim.finki.routingsystem.model.documentEntities.DocumentVersion;
import mk.ukim.finki.routingsystem.model.dto.DocumentDownload.DisplayDocumentDownloadDto;
import mk.ukim.finki.routingsystem.model.dto.DocumentDownload.FileResource;
import mk.ukim.finki.routingsystem.model.enumerations.ActionType;
import mk.ukim.finki.routingsystem.model.enumerations.DocumentStatus;
import mk.ukim.finki.routingsystem.model.exceptions.DocumentVersionNotFoundException;
import mk.ukim.finki.routingsystem.repository.DocumentVersionRepository;
import mk.ukim.finki.routingsystem.security.EmployeePrincipal;
import mk.ukim.finki.routingsystem.service.DocumentDownloadService;
import mk.ukim.finki.routingsystem.service.LoadingFileService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentDownloadRestController {

    private final LoadingFileService fileService;
    private final DocumentVersionRepository documentVersionRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final DocumentDownloadService documentDownloadService;

    public DocumentDownloadRestController(LoadingFileService fileService, DocumentVersionRepository documentVersionRepository, ApplicationEventPublisher applicationEventPublisher, DocumentDownloadService documentDownloadService) {
        this.fileService = fileService;
        this.documentVersionRepository = documentVersionRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.documentDownloadService = documentDownloadService;
    }


    @GetMapping("{documentId}/versions/{versionId}/download")
    public ResponseEntity<Resource> downloadPdf(@PathVariable Long documentId,
                                                @PathVariable Long versionId,
                                                @AuthenticationPrincipal EmployeePrincipal employeePrincipal) {


        DocumentVersion documentVersion = documentVersionRepository.findById(versionId)
                .orElseThrow(() -> new DocumentVersionNotFoundException("Document version not found."));

        if (!documentVersion.getDocument().getId().equals(documentId)) {
            throw new DocumentVersionNotFoundException("Version does not belong to this document.");
        }

        FileResource file = fileService.loadFile(documentId, versionId, employeePrincipal.getCompanyId());

        if (file == null || file.length() == 0) {
            return ResponseEntity.notFound().build();
        }

        String title = documentVersion.getDocument().getTitle().trim();

        applicationEventPublisher.publishEvent(
                new DocumentDownloadRequestedEvent(
                        documentId,
                        title,
                        employeePrincipal.getEmployeeId(),
                        versionId,
                        LocalDateTime.now())
        );

        applicationEventPublisher.publishEvent(new DocumentActionRequestedEvent(
                documentVersion.getDocument().getId(),
                documentVersion.getId(),
                employeePrincipal.getEmployeeId(),
                ActionType.DOWNLOADED,
                documentVersion.getDocument().getDocumentStatus(),
                DocumentStatus.DOWNLOADED,
                LocalDateTime.now()
        ));


        String fileName = documentVersion.getFileName() != null ? documentVersion.getFileName() : "document-" + versionId + ".pdf";

        return ResponseEntity.ok()
                .contentLength(file.length())
                .contentType(file.mediaType() != null ? file.mediaType() : MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(file.resource());

    }


    @GetMapping("/{documentId}/documentDownloads")
    public ResponseEntity<List<DisplayDocumentDownloadDto>> getDownloadsByDocument(@PathVariable Long documentId,
                                                                                   @AuthenticationPrincipal EmployeePrincipal employeePrincipal) {

        return ResponseEntity.ok(documentDownloadService.findAllDownloadsByDocument(documentId, employeePrincipal.getCompanyId()));

    }


    @GetMapping("/downloadsByMe")
    public ResponseEntity<List<DisplayDocumentDownloadDto>> getDownloadsByEmployee(@AuthenticationPrincipal EmployeePrincipal employeePrincipal) {

        return ResponseEntity.ok(documentDownloadService.findAllDownloadsByEmployee(employeePrincipal.getEmployeeId(), employeePrincipal.getCompanyId()));

    }

}
