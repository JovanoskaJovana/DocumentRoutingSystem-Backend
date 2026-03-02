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

    Page<DisplayDocumentDto> findAllByRoutedToDepartment(Long departmentId, Long companyId, Pageable pageable);

    Page<DisplayAdminDocumentDto> findAllByRoutedToDepartmentByAdmin(Long departmentId, Long companyId, Pageable pageable);

    Page<DisplayDocumentDto> findAllByRoutedToEmployee(List<DocumentStatus> documentStatuses, Long employeeId, Long companyId, Pageable pageable);

    Page<DisplayDocumentDto> findAllUploadedByEmployee (List<DocumentStatus> documentStatuses, Long employeeId, Long companyId, Pageable pageable);

    DisplayDocumentDto findAllWithVersions(Long documentId, Long companyId);

    DisplayDocumentDto createDocumentAndDocumentVersion(CreateDocumentDto documentDto, MultipartFile file, Long uploaderId, Long companyId) throws IOException;

    DisplayDocumentDto routeDocument(Long documentId, Long employeeId, Long companyId);

    DisplayDocumentDto manualRouteDocument(Long documentId, Long employeeId, Long companyId, String departmentKey);

    boolean approveDocument(Long documentId, Long employeeId, Long companyId);

    boolean rejectDocument(Long documentId, Long employeeId, Long companyId);
}
