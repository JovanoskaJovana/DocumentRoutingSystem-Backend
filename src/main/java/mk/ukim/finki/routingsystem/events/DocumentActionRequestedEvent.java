package mk.ukim.finki.routingsystem.events;

import mk.ukim.finki.routingsystem.model.enumerations.ActionType;
import mk.ukim.finki.routingsystem.model.enumerations.DocumentStatus;

import java.time.LocalDateTime;

public record DocumentActionRequestedEvent(
        Long documentId,
        Long versionId,
        Long employeeId,
        ActionType actionType,
        DocumentStatus fromStatus,
        DocumentStatus toStatus,
        LocalDateTime dateTime
) {}
