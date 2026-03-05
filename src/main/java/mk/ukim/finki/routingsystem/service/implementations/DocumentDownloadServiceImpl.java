package mk.ukim.finki.routingsystem.service.implementations;

import mk.ukim.finki.routingsystem.model.Employee;
import mk.ukim.finki.routingsystem.model.documentEntities.Document;
import mk.ukim.finki.routingsystem.model.documentEntities.DocumentDownload;
import mk.ukim.finki.routingsystem.model.documentEntities.DocumentVersion;
import mk.ukim.finki.routingsystem.model.dto.DocumentDownload.CreateDocumentDownloadDto;
import mk.ukim.finki.routingsystem.model.dto.DocumentDownload.DisplayDocumentDownloadDto;
import mk.ukim.finki.routingsystem.model.exceptions.DocumentNotFoundException;
import mk.ukim.finki.routingsystem.model.exceptions.DocumentVersionNotFoundException;
import mk.ukim.finki.routingsystem.model.exceptions.EmployeeNotFoundException;
import mk.ukim.finki.routingsystem.repository.DocumentDownloadRepository;
import mk.ukim.finki.routingsystem.repository.DocumentRepository;
import mk.ukim.finki.routingsystem.repository.DocumentVersionRepository;
import mk.ukim.finki.routingsystem.repository.EmployeeRepository;
import mk.ukim.finki.routingsystem.service.DocumentDownloadService;
import mk.ukim.finki.routingsystem.service.mappers.DocumentDownloadMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DocumentDownloadServiceImpl implements DocumentDownloadService {

    public final DocumentDownloadRepository documentDownloadRepository;
    private final DocumentDownloadMapper documentDownloadMapper;
    private final DocumentRepository documentRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final EmployeeRepository employeeRepository;

    public DocumentDownloadServiceImpl(DocumentDownloadRepository documentDownloadRepository, DocumentDownloadMapper documentDownloadMapper, DocumentRepository documentRepository, DocumentVersionRepository documentVersionRepository, EmployeeRepository employeeRepository) {
        this.documentDownloadRepository = documentDownloadRepository;
        this.documentDownloadMapper = documentDownloadMapper;
        this.documentRepository = documentRepository;
        this.documentVersionRepository = documentVersionRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DisplayDocumentDownloadDto> findAllDownloadsByEmployee(Long employeeId, Long companyId) {
        return documentDownloadRepository.findAllByEmployee_IdAndDocument_Company_IdOrderByDownloadDateTimeDesc(employeeId, companyId)
                .stream().map(documentDownloadMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DisplayDocumentDownloadDto> findAllDownloadsByDocument(Long documentId, Long companyId) {
        return documentDownloadRepository.findAllByDocument_IdAndDocument_Company_Id(documentId, companyId)
                .stream().map(documentDownloadMapper::toDto).toList();
    }

    @Override
    public DisplayDocumentDownloadDto saveAndCreate(CreateDocumentDownloadDto createDocumentDownloadDto, Long companyId) {

        Document document = documentRepository.findByIdAndAndCompany_Id(createDocumentDownloadDto.documentId(), companyId)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found"));

        Long versionId = createDocumentDownloadDto.versionId();

        DocumentVersion version = documentVersionRepository.findByIdAndDocument_Company_Id(versionId, companyId)
                .orElseThrow(() -> new DocumentVersionNotFoundException("Document version not found"));

        Employee employee = employeeRepository.findByIdAndCompany_Id(createDocumentDownloadDto.employeeId(), companyId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));

        DocumentDownload documentDownload = new DocumentDownload();
        documentDownload.setDocument(document);
        documentDownload.setEmployee(employee);
        documentDownload.setDocumentVersion(version);
        documentDownload.setDownloadDateTime(LocalDateTime.now());

        documentDownloadRepository.save(documentDownload);
        return documentDownloadMapper.toDto(documentDownload);
    }
}
