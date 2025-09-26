package mk.ukim.finki.routingsystem.model.dto;

import java.time.LocalDateTime;

public record DocumentVersionHistoryDto(

        Long versionId,
        Long documentId,
        String versionNumber,
        String editedByEmployee,
        String changeNote,
        LocalDateTime uploadedDateTime,
        String downloadUrl

) {}
