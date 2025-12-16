package mk.ukim.finki.routingsystem.listeners;

import mk.ukim.finki.routingsystem.events.DocumentActionRequestedEvent;
import mk.ukim.finki.routingsystem.model.Employee;
import mk.ukim.finki.routingsystem.model.documentEntities.Document;
import mk.ukim.finki.routingsystem.model.documentEntities.DocumentAction;
import mk.ukim.finki.routingsystem.model.documentEntities.DocumentVersion;
import mk.ukim.finki.routingsystem.model.exceptions.DocumentNotFoundException;
import mk.ukim.finki.routingsystem.model.exceptions.DocumentVersionNotFoundException;
import mk.ukim.finki.routingsystem.model.exceptions.EmployeeNotFoundException;
import mk.ukim.finki.routingsystem.repository.DocumentActionRepository;
import mk.ukim.finki.routingsystem.repository.DocumentRepository;
import mk.ukim.finki.routingsystem.repository.DocumentVersionRepository;
import mk.ukim.finki.routingsystem.repository.EmployeeRepository;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;


@Component
@EnableAsync
public class DocumentActionListener {

    private final DocumentRepository documentRepository;
    private final EmployeeRepository employeeRepository;
    private final DocumentActionRepository documentActionRepository;
    private final DocumentVersionRepository documentVersionRepository;

    public DocumentActionListener(DocumentRepository documentRepository, EmployeeRepository employeeRepository, DocumentActionRepository documentActionRepository, DocumentVersionRepository documentVersionRepository) {
        this.documentRepository = documentRepository;
        this.employeeRepository = employeeRepository;
        this.documentActionRepository = documentActionRepository;
        this.documentVersionRepository = documentVersionRepository;
    }

    @EventListener
    public void on(DocumentActionRequestedEvent event) {
        Document document = documentRepository.findById(event.documentId())
                .orElseThrow(() -> new DocumentNotFoundException("Document not found."));

        DocumentVersion documentVersion = (event.versionId() != null ? documentVersionRepository.findById(event.versionId())
                .orElseThrow(() -> new DocumentVersionNotFoundException("Document version not found."))
                : document.getCurrentDocumentVersion());

        Employee employee = (event.employeeId() != null) ? employeeRepository.findById(event.employeeId())
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"))
                : null;

        DocumentAction documentAction = new DocumentAction();
        documentAction.setDocument(document);
        documentAction.setDocumentVersion(documentVersion);
        documentAction.setPerformedByEmployee(employee);
        documentAction.setActionDateTime(event.dateTime());
        documentAction.setFromStatus(event.fromStatus());
        documentAction.setToStatus(event.toStatus());
        documentAction.setPerformedAction(event.actionType());
        documentAction.setNotes(buildNotes(document,employee,event));

        documentActionRepository.save(documentAction);
    }

    private static String buildNotes(Document document, Employee employee, DocumentActionRequestedEvent event) {

        String employeeName = (employee == null) ? "Unknown" : employee.getFirstName() + " " + employee.getLastName();
        return switch (event.actionType()) {
            case UPLOADED -> "Document" + document.getTitle() + " has been uploaded by employee " + employeeName;
            case ROUTED -> "Document status change: " + event.fromStatus() + " -> " + event.toStatus();
            case EDITED -> "Document" + document.getTitle() + " has been edited by employee " + employeeName;
            case APPROVED -> "Document" + document.getTitle() + " has been approved by employee " + employeeName;
            case REJECTED -> "Document" + document.getTitle() + " has been rejected by employee " + employeeName;
            case FAILED_ROUTING -> "Document" + document.getTitle() + " has failed to route";
            case DOWNLOADED -> "Document" + document.getTitle() + " has been downloaded by employee " + employeeName;
        };
    }

}
