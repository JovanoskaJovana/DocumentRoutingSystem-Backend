package mk.ukim.finki.routingsystem.listeners;

import mk.ukim.finki.routingsystem.events.DocumentDownloadRequestedEvent;
import mk.ukim.finki.routingsystem.model.Employee;
import mk.ukim.finki.routingsystem.model.documentEntities.Document;
import mk.ukim.finki.routingsystem.model.documentEntities.DocumentDownload;
import mk.ukim.finki.routingsystem.model.documentEntities.DocumentVersion;
import mk.ukim.finki.routingsystem.model.exceptions.DocumentNotFoundException;
import mk.ukim.finki.routingsystem.model.exceptions.DocumentVersionNotFoundException;
import mk.ukim.finki.routingsystem.model.exceptions.EmployeeNotFoundException;
import mk.ukim.finki.routingsystem.repository.DocumentDownloadRepository;
import mk.ukim.finki.routingsystem.repository.DocumentRepository;
import mk.ukim.finki.routingsystem.repository.DocumentVersionRepository;
import mk.ukim.finki.routingsystem.repository.EmployeeRepository;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@EnableAsync
public class DocumentDownloadListener {

    private final DocumentRepository documentRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final EmployeeRepository employeeRepository;
    private final DocumentDownloadRepository documentDownloadRepository;

    public DocumentDownloadListener(DocumentRepository documentRepository, DocumentVersionRepository documentVersionRepository, EmployeeRepository employeeRepository, DocumentDownloadRepository documentDownloadRepository) {
        this.documentRepository = documentRepository;
        this.documentVersionRepository = documentVersionRepository;
        this.employeeRepository = employeeRepository;
        this.documentDownloadRepository = documentDownloadRepository;
    }

    @EventListener
    public void on(DocumentDownloadRequestedEvent event) {

        Document document = documentRepository.findById(event.documentId())
                .orElseThrow(() -> new DocumentNotFoundException("Document not found"));

        DocumentVersion documentVersion = event.versionId() != null ? documentVersionRepository.findById(event.versionId())
                .orElseThrow(() -> new DocumentVersionNotFoundException("Document version not found"))
                : null;

        Employee employee = event.employeeId() != null ? employeeRepository.findById(event.employeeId())
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"))
                : null;

        DocumentDownload documentDownload = new DocumentDownload();
        documentDownload.setDocument(document);
        documentDownload.setDocumentVersion(documentVersion);
        documentDownload.setEmployee(employee);
        documentDownload.setDownloadDateTime(LocalDateTime.now());
        documentDownloadRepository.save(documentDownload);

    }

}
