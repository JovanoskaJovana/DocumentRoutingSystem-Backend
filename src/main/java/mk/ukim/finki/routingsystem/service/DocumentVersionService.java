package mk.ukim.finki.routingsystem.service;

import mk.ukim.finki.routingsystem.model.dto.DocumentVersion.CreateDocumentVersionDto;
import mk.ukim.finki.routingsystem.model.dto.DocumentVersion.DisplayDocumentVersionDto;
import mk.ukim.finki.routingsystem.model.dto.DocumentVersion.UpdateDocumentAndVersionDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DocumentVersionService {

    Page<DisplayDocumentVersionDto> listAllVersionsOfADocument(Long documentId, Pageable pageable);

    DisplayDocumentVersionDto createAndSaveADocumentVersion(CreateDocumentVersionDto createDocumentVersionDto);

    DisplayDocumentVersionDto updateADocumentVersion(Long documentId, UpdateDocumentAndVersionDto updateDocumentAndVersionDto);

}
