package mk.ukim.finki.routingsystem.model.dto.DocumentDownload;

import java.time.LocalDateTime;

public record CreateDocumentDownloadDto(
        Long documentId,
        String documentTitle,
        Long employeeId,
        Long versionId,
        LocalDateTime downloadedAt
) {}
