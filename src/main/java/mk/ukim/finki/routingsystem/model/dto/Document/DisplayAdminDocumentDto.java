package mk.ukim.finki.routingsystem.model.dto.Document;

import java.time.LocalDateTime;

public record AdminRoutedDocumentDto(

        Long documentId,
        String title,
        String uploadedByEmployee,
        String routedToDepartment,
        LocalDateTime uploadedDateTime,
        String documentStatus,
        String currentVersion,
        String currentVersionDownloadUrl
) {}
