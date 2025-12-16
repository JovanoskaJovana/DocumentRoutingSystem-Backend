package mk.ukim.finki.routingsystem.service;

import mk.ukim.finki.routingsystem.model.documentEntities.DocumentAction;
import mk.ukim.finki.routingsystem.model.dto.DocumentAction.CreateDocumentActionDto;
import mk.ukim.finki.routingsystem.model.dto.DocumentAction.DisplayDocumentActionDto;

import java.util.List;

public interface DocumentActionService {

    List<DisplayDocumentActionDto> findAllForADocument(Long documentId);

    List<DisplayDocumentActionDto> findAllBySpecificEmployee(Long employeeId, Long documentId);

    DisplayDocumentActionDto createAndSaveActionForADocument(CreateDocumentActionDto createDocumentActionDto);


}
