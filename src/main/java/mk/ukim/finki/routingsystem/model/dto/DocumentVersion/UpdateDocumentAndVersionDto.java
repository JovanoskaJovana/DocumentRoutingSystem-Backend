package mk.ukim.finki.routingsystem.model.dto.DocumentVersion;


import java.time.LocalDateTime;

public record UpdateDocumentAndVersionDto(
        String title,
        Long editedByEmployeeId,
        String changeNote,
        byte[] fileData
) {}
