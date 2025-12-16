package mk.ukim.finki.routingsystem.model.dto.DocumentVersion;

public record UpdateDocumentAndVersionDto(
        String title,
        Long editedByEmployeeId,
        String changeNote,
        byte[] fileData
) {}
