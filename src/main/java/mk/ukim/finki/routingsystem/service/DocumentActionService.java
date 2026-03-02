package mk.ukim.finki.routingsystem.service;

import mk.ukim.finki.routingsystem.model.documentEntities.DocumentAction;
import mk.ukim.finki.routingsystem.model.dto.DocumentAction.CreateDocumentActionDto;
import mk.ukim.finki.routingsystem.model.dto.DocumentAction.DisplayDocumentActionDto;

import java.util.List;

public interface DocumentActionService {

    List<DisplayDocumentActionDto> findAllForADocument(Long documentId, Long companyId);

    List<DisplayDocumentActionDto> findAllBySpecificEmployee(Long employeeId, Long documentId, Long companyId);

    DisplayDocumentActionDto createAndSaveActionForADocument(CreateDocumentActionDto createDocumentActionDto,  Long companyId);


}
