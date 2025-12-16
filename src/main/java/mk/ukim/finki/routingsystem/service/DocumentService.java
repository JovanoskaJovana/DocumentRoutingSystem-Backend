package mk.ukim.finki.routingsystem.service;

import mk.ukim.finki.routingsystem.model.dto.Document.CreateDocumentDto;
import mk.ukim.finki.routingsystem.model.dto.Document.DisplayAdminDocumentDto;
import mk.ukim.finki.routingsystem.model.dto.Document.DisplayDocumentDto;
import mk.ukim.finki.routingsystem.model.enumerations.DocumentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface DocumentService {

    Page<DisplayDocumentDto> findAllByRoutedToDepartment(Long departmentId, Pageable pageable);

    Page<DisplayAdminDocumentDto> findAllByRoutedToDepartmentByAdmin(Long departmentId, Pageable pageable);

    Page<DisplayDocumentDto> findAllByRoutedToEmployee(List<DocumentStatus> documentStatuses, Long employeeId, Pageable pageable);

    Page<DisplayDocumentDto> findAllUploadedByEmployee (List<DocumentStatus> documentStatuses, Long employeeId, Pageable pageable);

    DisplayDocumentDto findAllWithVersions(Long documentId);

    DisplayDocumentDto createDocumentAndDocumentVersion(CreateDocumentDto documentDto, MultipartFile file, Long uploaderId) throws IOException;

    DisplayDocumentDto routeDocument(Long documentId, Long employeeId);

    boolean approveDocument(Long documentId, Long employeeId);

    boolean rejectDocument(Long documentId, Long employeeId);
}
