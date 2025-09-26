package mk.ukim.finki.routingsystem.service.implementations;

import mk.ukim.finki.routingsystem.model.Employee;
import mk.ukim.finki.routingsystem.model.documentEntities.Document;
import mk.ukim.finki.routingsystem.model.documentEntities.DocumentAction;
import mk.ukim.finki.routingsystem.model.dto.DocumentAction.CreateDocumentActionDto;
import mk.ukim.finki.routingsystem.model.dto.DocumentAction.DisplayDocumentActionDto;
import mk.ukim.finki.routingsystem.model.exceptions.DocumentNotFoundException;
import mk.ukim.finki.routingsystem.model.exceptions.EmployeeNotFoundException;
import mk.ukim.finki.routingsystem.repository.DocumentActionRepository;
import mk.ukim.finki.routingsystem.repository.DocumentRepository;
import mk.ukim.finki.routingsystem.repository.EmployeeRepository;
import mk.ukim.finki.routingsystem.service.DocumentActionService;
import mk.ukim.finki.routingsystem.service.mappers.DocumentActionMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DocumentActionServiceImpl implements DocumentActionService {

    private final DocumentActionRepository documentActionRepository;
    private final DocumentActionMapper documentActionMapper;
    private final EmployeeRepository employeeRepository;
    private final DocumentRepository documentRepository;

    public DocumentActionServiceImpl(DocumentActionRepository documentActionRepository, DocumentActionMapper documentActionMapper, EmployeeRepository employeeRepository, DocumentRepository documentRepository) {
        this.documentActionRepository = documentActionRepository;
        this.documentActionMapper = documentActionMapper;
        this.employeeRepository = employeeRepository;
        this.documentRepository = documentRepository;
    }


    @Override
    public List<DisplayDocumentActionDto> findAllForADocument(Long documentId) {
        return documentActionRepository.findByDocument_IdOrderByActionDateTime(documentId)
                .stream().map(documentActionMapper::toDto).toList();
    }

    @Override
    public List<DisplayDocumentActionDto> findAllBySpecificEmployee(Long employeeId, Long documentId) {
        return documentActionRepository.findByDocument_IdAndPerformedByEmployee_IdOrderByActionDateTime(employeeId, documentId)
                .stream().map(documentActionMapper::toDto).toList();
    }

    @Override
    public DisplayDocumentActionDto createAndSaveActionForADocument(CreateDocumentActionDto createDocumentActionDto) {

        Employee employee = employeeRepository.findById(createDocumentActionDto.performedByEmployeeId())
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));

        Document document = documentRepository.findById(createDocumentActionDto.documentId())
                .orElseThrow(() -> new DocumentNotFoundException("Document not found"));

        DocumentAction documentAction = new DocumentAction();
        documentAction.setDocument(document);
        documentAction.setPerformedByEmployee(employee);
        documentAction.setDocumentVersion(document.getCurrentDocumentVersion());
        documentAction.setActionDateTime(LocalDateTime.now());
        documentAction.setFromStatus(createDocumentActionDto.fromStatus());
        documentAction.setToStatus(createDocumentActionDto.toStatus());
        documentAction.setPerformedAction(createDocumentActionDto.actionType());

        switch (createDocumentActionDto.actionType()) {
            case UPLOADED -> documentAction.setNotes("Document" + document.getTitle() + " has been uploaded by employee " + employeeName(employee));
            case ROUTED -> documentAction.setNotes("Document status change: " + createDocumentActionDto.fromStatus() + " -> " + createDocumentActionDto.toStatus());
            case EDITED -> documentAction.setNotes("Document" + document.getTitle() + " has been edited by employee " + employeeName(employee));
            case APPROVED -> documentAction.setNotes("Document" + document.getTitle() + " has been approved by employee " + employeeName(employee));
            case REJECTED -> documentAction.setNotes("Document" + document.getTitle() + " has been rejected by employee " + employeeName(employee));
            case FAILED_ROUTING -> documentAction.setNotes("Document" + document.getTitle() + " has failed to route");
            case DOWNLOADED -> documentAction.setNotes("Document" + document.getTitle() + " has been downloaded by employee " + employeeName(employee));
        }

        documentActionRepository.save(documentAction);
        return documentActionMapper.toDto(documentAction);
    }

    private static String employeeName(Employee employee) {
        return employee.getFirstName() + " " + employee.getLastName();
    }
}
