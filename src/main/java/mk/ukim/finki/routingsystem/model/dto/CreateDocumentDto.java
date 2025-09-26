package mk.ukim.finki.routingsystem.model.dto;

public record CreateDocumentDto(
        String title,
        Long uploadedByEmployee,
        byte[] fileData
) {}
