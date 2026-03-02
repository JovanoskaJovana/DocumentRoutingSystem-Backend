package mk.ukim.finki.routingsystem.service;

import mk.ukim.finki.routingsystem.model.dto.DocumentVersion.CreateDocumentVersionDto;
import mk.ukim.finki.routingsystem.model.dto.DocumentVersion.DisplayDocumentVersionDto;
import mk.ukim.finki.routingsystem.model.dto.DocumentVersion.UpdateDocumentAndVersionDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DocumentVersionService {

    Page<DisplayDocumentVersionDto> listAllVersionsOfADocument(Long documentId, Long companyId, Pageable pageable);

    DisplayDocumentVersionDto createAndSaveADocumentVersion(CreateDocumentVersionDto createDocumentVersionDto, Long companyId);

    DisplayDocumentVersionDto updateAndSaveDocumentVersion(Long documentId, UpdateDocumentAndVersionDto updateDocumentAndVersionDto, Long companyId);

    DisplayDocumentVersionDto getDocumentVersion(Long documentVersionId, Long companyId);

}
