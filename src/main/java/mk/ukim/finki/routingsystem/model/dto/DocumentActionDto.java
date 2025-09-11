package mk.ukim.finki.routingsystem.model.dto;

import java.time.LocalDateTime;

public record DocumentActionDto(

        Long actionId,
        Long documentId,
        String documentVersion,
        String performedByEmployee,
        String actionType,
        String fromStatus,
        String toStatus,
        String note,
        LocalDateTime dateTime

) { }
