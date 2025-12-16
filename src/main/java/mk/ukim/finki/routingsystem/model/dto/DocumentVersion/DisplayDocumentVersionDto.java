package mk.ukim.finki.routingsystem.model.dto.DocumentVersion;

import java.time.LocalDateTime;

public record DisplayDocumentVersionDto(

        Long versionId,
        String document,
        String versionNumber,
        String fileName,
        String uploadedByEmployee,
        String changeNote,
        LocalDateTime uploadedDateTime,
        String downloadUrl

) {}
