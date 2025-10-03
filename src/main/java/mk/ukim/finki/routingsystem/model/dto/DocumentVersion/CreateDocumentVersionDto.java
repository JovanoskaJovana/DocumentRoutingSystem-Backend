package mk.ukim.finki.routingsystem.model.dto.DocumentVersion;

import mk.ukim.finki.routingsystem.model.Employee;

import java.time.LocalDateTime;

public record CreateDocumentVersionDto(
        Long documentId,
        int versionNumber,
        Long editedByEmployee,
        String fileName,
        String changeNote,
        LocalDateTime uploadedDateTime,
        byte[] fileData
) {
}
