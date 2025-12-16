package mk.ukim.finki.routingsystem.model.dto.DocumentAction;

import mk.ukim.finki.routingsystem.model.enumerations.ActionType;
import mk.ukim.finki.routingsystem.model.enumerations.DocumentStatus;

import java.time.LocalDateTime;

public record CreateDocumentActionDto(

        Long actionId,
        Long documentId,
        Long documentVersion,
        Long performedByEmployeeId,
        ActionType actionType,
        DocumentStatus fromStatus,
        DocumentStatus toStatus,
        LocalDateTime dateTime

) { }
