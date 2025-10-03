package mk.ukim.finki.routingsystem.service.implementations;

import mk.ukim.finki.routingsystem.events.DocumentActionRequestedEvent;
import mk.ukim.finki.routingsystem.model.Department;
import mk.ukim.finki.routingsystem.model.Employee;
import mk.ukim.finki.routingsystem.model.documentEntities.Document;
import mk.ukim.finki.routingsystem.model.documentEntities.DocumentVersion;
import mk.ukim.finki.routingsystem.model.dto.Document.*;
import mk.ukim.finki.routingsystem.model.dto.DocumentVersion.CreateDocumentVersionDto;
import mk.ukim.finki.routingsystem.model.dto.DocumentVersion.DisplayDocumentVersionDto;
import mk.ukim.finki.routingsystem.model.enumerations.ActionType;
import mk.ukim.finki.routingsystem.model.enumerations.DocumentStatus;
import mk.ukim.finki.routingsystem.model.enumerations.EmployeeType;
import mk.ukim.finki.routingsystem.model.exceptions.*;
import mk.ukim.finki.routingsystem.repository.DepartmentRepository;
import mk.ukim.finki.routingsystem.repository.DocumentRepository;
import mk.ukim.finki.routingsystem.repository.DocumentVersionRepository;
import mk.ukim.finki.routingsystem.repository.EmployeeRepository;
import mk.ukim.finki.routingsystem.service.DocumentService;
import mk.ukim.finki.routingsystem.service.DocumentVersionService;
import mk.ukim.finki.routingsystem.service.mappers.DocumentMapper;
import mk.ukim.finki.routingsystem.service.rules.RoutingRules;
import mk.ukim.finki.routingsystem.service.text.DocumentTextExtractor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class DocumentServiceImpl implements DocumentService {

    public final DocumentRepository documentRepository;
    private final DocumentMapper documentMapper;
    private final EmployeeRepository employeeRepository;
    private final DocumentVersionService documentVersionService;

    private final DocumentTextExtractor documentTextExtractor;
    private final RoutingRules routingRules;
    private final DepartmentRepository departmentRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final DocumentVersionRepository documentVersionRepository;

    public DocumentServiceImpl(DocumentRepository documentRepository, DocumentMapper documentMapper, EmployeeRepository employeeRepository, DocumentVersionService documentVersionService, DocumentTextExtractor documentTextExtractor, RoutingRules routingRules, DepartmentRepository departmentRepository, ApplicationEventPublisher applicationEventPublisher, DocumentVersionRepository documentVersionRepository) {
        this.documentRepository = documentRepository;
        this.documentMapper = documentMapper;
        this.employeeRepository = employeeRepository;
        this.documentVersionService = documentVersionService;
        this.documentTextExtractor = documentTextExtractor;
        this.routingRules = routingRules;
        this.departmentRepository = departmentRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.documentVersionRepository = documentVersionRepository;
    }

    @Override
    @Transactional (readOnly = true)
    public Page<DisplayDocumentDto> findAllByRoutedToDepartment(Long departmentId, Pageable pageable) {
        return documentRepository.findAllByRoutedToDepartment_Id(departmentId, pageable)
                .map(documentMapper::toDto);
    }

    @Override
    @Transactional
    public Page<DisplayAdminDocumentDto> findAllByRoutedToDepartmentByAdmin(Long departmentId, Pageable pageable) {
        return documentRepository.findAllByRoutedToDepartment_Id(departmentId, pageable)
                .map(documentMapper::toAdminDto);
    }

    @Override
    @Transactional (readOnly = true)
    public Page<DisplayDocumentDto> findAllByRoutedToEmployee(List<DocumentStatus> documentStatuses, Long employeeId, Pageable pageable) {
        return documentRepository.findAllByDocumentStatusInAndRoutedToEmployees_Id(documentStatuses, employeeId, pageable)
                .map(documentMapper::toDto);
    }

    @Override
    @Transactional (readOnly = true)
    public DisplayDocumentDto findAllWithVersions(Long documentId) {
        return documentRepository.findWithVersionsById(documentId).map(documentMapper::toDto)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found."));
    }

    @Override
    @Transactional
    public DisplayDocumentDto createDocumentAndDocumentVersion(CreateDocumentDto documentDto, MultipartFile file, Long uploaderId) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IOException("PDF file is required.");
        }

        if (uploaderId == null) {
            throw new IOException("Authenticated employee id is missing.");
        }

        Employee employee = employeeRepository.findById(uploaderId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));


        Document document = new Document();
        document.setDocumentStatus(DocumentStatus.UPLOADED);
        document.setTitle(documentDto.title());
        document.setUploadedByEmployee(employee);
        document.setUploadDateTime(LocalDateTime.now());
        documentRepository.save(document);

        CreateDocumentVersionDto versionDto = new CreateDocumentVersionDto(
                document.getId(),
                1,
                employee.getId(),
                document.getTitle(),
                "No made changes.",
                LocalDateTime.now(),
                file.getBytes()
        );

        DisplayDocumentVersionDto documentVersion = documentVersionService.createAndSaveADocumentVersion(versionDto);

        document.setCurrentDocumentVersion(documentVersionRepository.getReferenceById(documentVersion.versionId()));
        documentRepository.save(document);


        applicationEventPublisher.publishEvent(new DocumentActionRequestedEvent(
                document.getId(),
                document.getCurrentDocumentVersion().getId(),
                employee.getId(),
                ActionType.UPLOADED,
                null,
                DocumentStatus.UPLOADED,
                LocalDateTime.now()
        ));

        return documentMapper.toDto(document);
    }

    @Override
    @Transactional
    public DisplayDocumentDto routeDocument(Long documentId, Long employeeId) {

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found."));

        if (document.getDocumentStatus() != DocumentStatus.UPLOADED) {
            throw new InvalidDocumentStateException("Only uploaded documents can be routed");
        }

        DocumentStatus from = document.getDocumentStatus();

        DocumentVersion documentVersion = document.getCurrentDocumentVersion();
        if (documentVersion == null || documentVersion.getId() == null) {
            throw new DocumentVersionNotFoundException("No current version to route.");
        }

        byte[] bytes = documentVersion.getFileData();
        if (bytes == null || bytes.length == 0) {
            throw new DocumentVersionNotFoundException("No file to route.");
        }

        TitleAndBody text = documentTextExtractor.extractTitleAndBody(documentVersion.getFileData());
        RoutingResultDto resultDto = routingRules.routeToDepartmentAndEmployees(text);

        boolean hasEmployees = resultDto != null && resultDto.routedToEmployees() != null && !resultDto.routedToEmployees().isEmpty();
        boolean hasDepartment = resultDto != null && resultDto.departmentId() != null;

        if (!hasEmployees && !hasDepartment) {
            document.setRoutedToDepartment(null);
            document.getRoutedToEmployees().clear();
            document.setDocumentStatus(DocumentStatus.FAILED_ROUTING);
            documentRepository.save(document);

            applicationEventPublisher.publishEvent(new DocumentActionRequestedEvent(
                    document.getId(),
                    documentVersion.getId(),
                    employeeId,
                    ActionType.FAILED_ROUTING,
                    from,
                    DocumentStatus.FAILED_ROUTING,
                    LocalDateTime.now()
            ));

            return documentMapper.toDto(document);
        }

        Set<Employee> routedToEmployees = new HashSet<>(resultDto.routedToEmployees());
        document.setRoutedToEmployees(routedToEmployees);

        Department department = departmentRepository.findById(resultDto.departmentId())
                        .orElseThrow(() -> new DepartmentNotFoundException("Department not found"));

        document.setRoutedToDepartment(department);
        document.setDocumentStatus(DocumentStatus.ROUTED);

        documentRepository.save(document);

        applicationEventPublisher.publishEvent(new DocumentActionRequestedEvent(
                document.getId(),
                document.getCurrentDocumentVersion().getId(),
                employeeId,
                ActionType.ROUTED,
                from,
                DocumentStatus.ROUTED,
                LocalDateTime.now()
        ));

        return documentMapper.toDto(document);
    }

    @Override
    @Transactional
    public boolean approveDocument(Long documentId, Long employeeId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found."));

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found."));

        boolean isSignatory = employee.getType().equals(EmployeeType.SIGNATORY);

        if (document.getDocumentStatus() == DocumentStatus.ROUTED && isSignatory) {
            DocumentStatus from = document.getDocumentStatus();
            document.setDocumentStatus(DocumentStatus.APPROVED);
            documentRepository.save(document);

            applicationEventPublisher.publishEvent(new DocumentActionRequestedEvent(
                    document.getId(),
                    document.getCurrentDocumentVersion().getId(),
                    employee.getId(),
                    ActionType.APPROVED,
                    from,
                    DocumentStatus.APPROVED,
                    LocalDateTime.now()
            ));

            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean rejectDocument(Long documentId, Long employeeId) {

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found."));

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found."));

        boolean isSignatory = employee.getType().equals(EmployeeType.SIGNATORY);

        if (document.getDocumentStatus() == DocumentStatus.ROUTED && isSignatory) {
            DocumentStatus from = document.getDocumentStatus();
            document.setDocumentStatus(DocumentStatus.REJECTED);
            documentRepository.save(document);

            applicationEventPublisher.publishEvent(new DocumentActionRequestedEvent(
                    document.getId(),
                    document.getCurrentDocumentVersion().getId(),
                    employee.getId(),
                    ActionType.REJECTED,
                    from,
                    DocumentStatus.REJECTED,
                    LocalDateTime.now()
            ));

            return true;
        }
        return false;
    }
}
