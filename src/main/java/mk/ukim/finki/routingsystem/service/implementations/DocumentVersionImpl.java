package mk.ukim.finki.routingsystem.service.implementations;

import mk.ukim.finki.routingsystem.model.Employee;
import mk.ukim.finki.routingsystem.model.documentEntities.Document;
import mk.ukim.finki.routingsystem.model.documentEntities.DocumentVersion;
import mk.ukim.finki.routingsystem.model.dto.DocumentVersion.CreateDocumentVersionDto;
import mk.ukim.finki.routingsystem.model.dto.DocumentVersion.DisplayDocumentVersionDto;
import mk.ukim.finki.routingsystem.model.dto.DocumentVersion.UpdateDocumentAndVersionDto;
import mk.ukim.finki.routingsystem.model.exceptions.*;
import mk.ukim.finki.routingsystem.repository.DocumentRepository;
import mk.ukim.finki.routingsystem.repository.DocumentVersionRepository;
import mk.ukim.finki.routingsystem.repository.EmployeeRepository;
import mk.ukim.finki.routingsystem.service.DocumentVersionService;
import mk.ukim.finki.routingsystem.service.mappers.DocumentVersionMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class DocumentVersionImpl implements DocumentVersionService {

    public final DocumentVersionRepository documentVersionRepository;
    private final DocumentVersionMapper documentVersionMapper;
    private final EmployeeRepository employeeRepository;
    private final DocumentRepository documentRepository;

    public DocumentVersionImpl(DocumentVersionRepository documentVersionRepository, DocumentVersionMapper documentVersionMapper, EmployeeRepository employeeRepository, DocumentRepository documentRepository) {
        this.documentVersionRepository = documentVersionRepository;
        this.documentVersionMapper = documentVersionMapper;
        this.employeeRepository = employeeRepository;
        this.documentRepository = documentRepository;
    }

    @Override
    public Page<DisplayDocumentVersionDto> listAllVersionsOfADocument(Long documentId, Pageable pageable) {
        return documentVersionRepository.findByDocument_IdOrderByVersionNumberDesc(documentId, pageable)
                .map(documentVersionMapper::toDto);
    }

    @Override
    @Transactional
    public DisplayDocumentVersionDto createAndSaveADocumentVersion(CreateDocumentVersionDto createDocumentVersionDto) {

        Employee employee = employeeRepository.findById(createDocumentVersionDto.editedByEmployee())
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));

        Document document = documentRepository.findById(createDocumentVersionDto.documentId())
                .orElseThrow(() -> new DocumentNotFoundException("Document not found"));

        DocumentVersion documentVersion = new DocumentVersion();
        documentVersion.setDocument(document);
        documentVersion.setUploadedByEmployee(employee);
        documentVersion.setVersionNumber(createDocumentVersionDto.versionNumber());
        documentVersion.setUploadedDateTime(LocalDateTime.now());
        documentVersion.setFileData(createDocumentVersionDto.fileData());
        documentVersion.setChangeNote(createDocumentVersionDto.changeNote());

        documentVersionRepository.save(documentVersion);

        document.setCurrentDocumentVersion(documentVersion);
        documentRepository.save(document);

        return documentVersionMapper.toDto(documentVersion);
    }

    @Override
    @Transactional
    public DisplayDocumentVersionDto updateADocumentVersion(Long documentId, UpdateDocumentAndVersionDto updateDto){

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found"));

        DocumentVersion currentVersion = document.getCurrentDocumentVersion();

        DocumentVersion documentVersion = documentVersionRepository.findById(currentVersion.getId())
                .orElseThrow(() -> new DocumentVersionNotFound("Version for document is not found"));

        boolean newFile = updateDto.fileData() != null && updateDto.fileData().length > 0;

        DocumentVersion nextVersion = new DocumentVersion();

        if (newFile && updateDto.changeNote() == null || updateDto.changeNote().isBlank()) {
            throw new RequiredChangeNoteException("Change Note is required when uploading a new file");
        }
        if (updateDto.editedByEmployeeId() == null) {
            throw new RequiredEmployeeIdException("Employee Id is required when editing a new file");
        }

        nextVersion.setVersionNumber(documentVersion.getVersionNumber() == 0 ? 1 : documentVersion.getVersionNumber() + 1);

        if (updateDto.title() != null && !updateDto.title().isBlank()) {
            nextVersion.setFileName(updateDto.title().trim());
            document.setTitle(updateDto.title().trim());
        } else {
            nextVersion.setFileName(documentVersion.getFileName());
        }

        if (newFile) {
            nextVersion.setFileData(updateDto.fileData());
        } else {
            nextVersion.setFileData(documentVersion.getFileData());
        }

        Employee employee = employeeRepository.findById(updateDto.editedByEmployeeId())
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));

        nextVersion.setUploadedByEmployee(employee);
        document.setUploadedByEmployee(employee);

        nextVersion.setChangeNote(updateDto.changeNote().trim());
        nextVersion.setUploadedDateTime(LocalDateTime.now());
        nextVersion.setDocument(document);


        documentVersionRepository.save(nextVersion);
        document.setCurrentDocumentVersion(nextVersion);
        documentRepository.save(document);

        return documentVersionMapper.toDto(nextVersion);
    }
}
