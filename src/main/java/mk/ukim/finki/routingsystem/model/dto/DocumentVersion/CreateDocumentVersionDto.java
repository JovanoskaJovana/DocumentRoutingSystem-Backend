package mk.ukim.finki.routingsystem.model.dto.DocumentVersion;

import mk.ukim.finki.routingsystem.model.Employee;

import java.time.LocalDateTime;

public record CreateDocumentVersionDto(

        Long versionId,
        Long documentId,
        int versionNumber,
        Long editedByEmployee,
        String changeNote,
        LocalDateTime uploadedDateTime,
        byte[] fileData,
        String downloadUrl
) {
}
