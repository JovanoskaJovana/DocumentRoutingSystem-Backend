package mk.ukim.finki.routingsystem.model.dto;

import java.time.LocalDateTime;

public record DocumentVersionHistoryDto(

        Long versionId,
        String versionNumber,
        String editedBy,
        String changeNote,
        LocalDateTime uploadedDateTime,
        String downloadUrl

        ) {}
