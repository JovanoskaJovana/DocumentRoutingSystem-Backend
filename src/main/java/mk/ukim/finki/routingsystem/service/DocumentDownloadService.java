package mk.ukim.finki.routingsystem.service;

import mk.ukim.finki.routingsystem.model.Employee;
import mk.ukim.finki.routingsystem.model.documentEntities.DocumentDownload;
import mk.ukim.finki.routingsystem.model.dto.DocumentDownload.CreateDocumentDownloadDto;
import mk.ukim.finki.routingsystem.model.dto.DocumentDownload.DisplayDocumentDownloadDto;

import java.util.List;

public interface DocumentDownloadService {

    List<DisplayDocumentDownloadDto> findAllDownloadsByEmployee(Long employeeId, Long companyId);
    List<DisplayDocumentDownloadDto> findAllDownloadsByDocument(Long documentId, Long companyId);

    DisplayDocumentDownloadDto saveAndCreate(CreateDocumentDownloadDto createDocumentDownloadDto, Long companyId);

}
