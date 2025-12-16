package mk.ukim.finki.routingsystem.model.dto.DocumentAction;

import java.time.LocalDateTime;

public record DisplayDocumentActionDto(

        Long actionId,
        String document,
        String documentVersion,
        String performedByEmployee,
        String actionType,
        String fromStatus,
        String toStatus,
        String note,
        LocalDateTime dateTime

) { }
