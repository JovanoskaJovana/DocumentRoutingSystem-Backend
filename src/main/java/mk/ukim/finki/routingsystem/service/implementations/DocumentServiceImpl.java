package mk.ukim.finki.routingsystem.service.implementations;

import mk.ukim.finki.routingsystem.events.DocumentActionRequestedEvent;
import mk.ukim.finki.routingsystem.model.Company;
import mk.ukim.finki.routingsystem.model.Department;
import mk.ukim.finki.routingsystem.model.Employee;
import mk.ukim.finki.routingsystem.model.documentEntities.Document;
import mk.ukim.finki.routingsystem.model.documentEntities.DocumentVersion;
import mk.ukim.finki.routingsystem.model.dto.Document.*;
import mk.ukim.finki.routingsystem.model.dto.DocumentVersion.CreateDocumentVersionDto;
import mk.ukim.finki.routingsystem.model.dto.DocumentVersion.DisplayDocumentVersionDto;
import mk.ukim.finki.routingsystem.model.dto.Routing.RoutingDecision;
import mk.ukim.finki.routingsystem.model.dto.Routing.TitleAndBody;
import mk.ukim.finki.routingsystem.model.enumerations.ActionType;
import mk.ukim.finki.routingsystem.model.enumerations.DocumentStatus;
import mk.ukim.finki.routingsystem.model.enumerations.EmployeeType;
import mk.ukim.finki.routingsystem.model.exceptions.*;
import mk.ukim.finki.routingsystem.repository.*;
import mk.ukim.finki.routingsystem.service.DocumentService;
import mk.ukim.finki.routingsystem.service.DocumentVersionService;
import mk.ukim.finki.routingsystem.service.mappers.DocumentMapper;
import mk.ukim.finki.routingsystem.service.routing.rules.DocumentRouter;
import mk.ukim.finki.routingsystem.service.routing.text.DocumentTextExtractor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class DocumentServiceImpl implements DocumentService {

    public final DocumentRepository documentRepository;
    private final DocumentMapper documentMapper;
    private final EmployeeRepository employeeRepository;
    private final DocumentVersionService documentVersionService;
    private final DocumentRouter documentRouter;
    private final CompanyRepository companyRepository;

    private final DocumentTextExtractor documentTextExtractor;
    private final DepartmentRepository departmentRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final DocumentVersionRepository documentVersionRepository;

    public DocumentServiceImpl(DocumentRepository documentRepository, DocumentMapper documentMapper, EmployeeRepository employeeRepository, DocumentVersionService documentVersionService, DocumentRouter documentRouter, CompanyRepository companyRepository, DocumentTextExtractor documentTextExtractor, DepartmentRepository departmentRepository, ApplicationEventPublisher applicationEventPublisher, DocumentVersionRepository documentVersionRepository) {
        this.documentRepository = documentRepository;
        this.documentMapper = documentMapper;
        this.employeeRepository = employeeRepository;
        this.documentVersionService = documentVersionService;
        this.documentRouter = documentRouter;
        this.companyRepository = companyRepository;
        this.documentTextExtractor = documentTextExtractor;
        this.departmentRepository = departmentRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.documentVersionRepository = documentVersionRepository;
    }

    @Override
    @Transactional (readOnly = true)
    public Page<DisplayDocumentDto> findAllByRoutedToDepartment(Long departmentId, Long companyId, Pageable pageable) {
        return documentRepository.findAllByRoutedToDepartment_IdAndCompany_IdOrderByUploadDateTime(departmentId, companyId, pageable)
                .map(documentMapper::toDto);
    }

    @Override
    @Transactional
    public Page<DisplayAdminDocumentDto> findAllByRoutedToDepartmentByAdmin(Long departmentId, Long companyId, Pageable pageable) {
        return documentRepository.findAllByRoutedToDepartment_IdAndCompany_IdOrderByUploadDateTime(departmentId, companyId, pageable)
                .map(documentMapper::toAdminDto);
    }

    @Override
    @Transactional (readOnly = true)
    public Page<DisplayDocumentDto> findAllByRoutedToEmployee(List<DocumentStatus> documentStatuses, Long employeeId, Long companyId, Pageable pageable) {
        return documentRepository.findAllByDocumentStatusInAndRoutedToEmployees_IdAndCompany_IdOrderByUploadDateTime(documentStatuses, employeeId, companyId, pageable)
                .map(documentMapper::toDto);
    }

    @Override
    @Transactional (readOnly = true)
    public Page<DisplayDocumentDto> findAllUploadedByEmployee(List<DocumentStatus> documentStatuses, Long employeeId, Long companyId, Pageable pageable) {
        return documentRepository.findAllByDocumentStatusInAndUploadedByEmployee_IdAndCompany_IdOrderByUploadDateTime(documentStatuses, employeeId, companyId, pageable)
                .map(documentMapper::toDto);
    }

    @Override
    @Transactional (readOnly = true)
    public DisplayDocumentDto findAllWithVersions(Long documentId, Long companyId) {
        return documentRepository.findWithVersionsByIdAndCompany_Id(documentId, companyId)
                .map(documentMapper::toDto)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found."));
    }

    @Override
    @Transactional
    public DisplayDocumentDto createDocumentAndDocumentVersion(CreateDocumentDto documentDto, MultipartFile file, Long uploaderId, Long companyId) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IOException("PDF file is required.");
        }

        if (uploaderId == null) {
            throw new IOException("Authenticated employee id is missing.");
        }

        Employee employee = employeeRepository.findByIdAndCompany_Id(uploaderId, companyId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found."));

        Document document = new Document();
        document.setCompany(company);
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

        DisplayDocumentVersionDto documentVersion = documentVersionService.createAndSaveADocumentVersion(versionDto, companyId);

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
    public DisplayDocumentDto routeDocument(Long documentId, Long employeeId, Long companyId) {

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found"));

        String tenantKey = company.getCode();

        Document document = documentRepository.findByIdAndAndCompany_Id(documentId, companyId)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found."));

        if (document.getDocumentStatus() != DocumentStatus.UPLOADED) {
            throw new InvalidDocumentStateException("Only uploaded documents can be routed");
        }

        DocumentStatus fromStatus = document.getDocumentStatus();

        DocumentVersion documentVersion = document.getCurrentDocumentVersion();
        if (documentVersion == null || documentVersion.getId() == null) {
            throw new DocumentVersionNotFoundException("No current version to route.");
        }

        byte[] bytes = documentVersion.getFileData();
        if (bytes == null || bytes.length == 0) {
            throw new DocumentVersionNotFoundException("No file to route.");
        }

        TitleAndBody textData = documentTextExtractor.extractTitleAndBody(documentVersion.getFileData());
        RoutingDecision routingDecision = documentRouter.route(tenantKey, textData);

        if (routingDecision.winner() != null) {

            Department department = departmentRepository.findByDepartmentKeyAndCompany_Id(routingDecision.winner(), companyId)
                    .orElseThrow(() -> new DepartmentNotFoundException("Department not found."));

            Set<Employee> signatories = employeeRepository.findAllByCompany_IdAndDepartment_IdAndType(companyId, department.getId(), EmployeeType.SIGNATORY);

            document.setRoutedToEmployees(signatories);
            document.setRoutedToDepartment(department);
            document.setDocumentStatus(DocumentStatus.ROUTED);
            documentRepository.save(document);

            applicationEventPublisher.publishEvent(new DocumentActionRequestedEvent(
                    document.getId(),
                    document.getCurrentDocumentVersion().getId(),
                    employeeId,
                    ActionType.ROUTED,
                    fromStatus,
                    DocumentStatus.ROUTED,
                    LocalDateTime.now()
            ));

        } else {

            document.setRoutedToDepartment(null);
            document.getRoutedToEmployees().clear();
            document.setDocumentStatus(DocumentStatus.FAILED_ROUTING);
            document.setSuggestedDepartments(routingDecision.tiedDepartments());
            documentRepository.save(document);

            applicationEventPublisher.publishEvent(new DocumentActionRequestedEvent(
                    document.getId(),
                    documentVersion.getId(),
                    employeeId,
                    ActionType.FAILED_ROUTING,
                    fromStatus,
                    DocumentStatus.FAILED_ROUTING,
                    LocalDateTime.now()
            ));

        }

        return documentMapper.toDto(document);
    }

    @Transactional
    @Override
    public DisplayDocumentDto manualRouteDocument(Long documentId, Long employeeId, Long companyId, String departmentKey) {

        Document document = documentRepository.findByIdAndAndCompany_Id(documentId, companyId)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found."));

        if (document.getDocumentStatus() != DocumentStatus.FAILED_ROUTING) {
            throw new InvalidDocumentStateException("Only failed routing documents can be manually routed");
        }

        DocumentStatus fromStatus = document.getDocumentStatus();

        Department department = departmentRepository.findByDepartmentKeyAndCompany_Id(departmentKey, companyId)
                .orElseThrow(() -> new DepartmentNotFoundException("Department not found"));

        Set<Employee> signatories = employeeRepository.findAllByCompany_IdAndDepartment_IdAndType(companyId, department.getId(), EmployeeType.SIGNATORY);

        document.setRoutedToEmployees(signatories);
        document.setRoutedToDepartment(department);
        document.setSuggestedDepartments(null);
        document.setDocumentStatus(DocumentStatus.ROUTED);
        documentRepository.save(document);

        applicationEventPublisher.publishEvent(new DocumentActionRequestedEvent(
                document.getId(),
                document.getCurrentDocumentVersion().getId(),
                employeeId,
                ActionType.ROUTED,
                fromStatus,
                DocumentStatus.ROUTED,
                LocalDateTime.now()
        ));

        return documentMapper.toDto(document);
    }

    @Override
    @Transactional
    public boolean approveDocument(Long documentId, Long employeeId, Long companyId) {

        Document document = documentRepository.findByIdAndAndCompany_Id(documentId, companyId)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found."));

        Employee employee = employeeRepository.findByIdAndCompany_Id(employeeId, companyId)
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
    public boolean rejectDocument(Long documentId, Long employeeId, Long companyId) {

        Document document = documentRepository.findByIdAndAndCompany_Id(documentId, companyId)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found."));

        Employee employee = employeeRepository.findByIdAndCompany_Id(employeeId, companyId)
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
