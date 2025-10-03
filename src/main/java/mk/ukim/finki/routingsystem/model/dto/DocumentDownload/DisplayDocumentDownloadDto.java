package mk.ukim.finki.routingsystem.model.dto.DocumentDownload;

import java.time.LocalDateTime;

public record DisplayDocumentDownloadDto(
        Long downloadId,
        String documentTitle,
        String employee,
        String versionNumber,
        LocalDateTime downloadedAt
) {
}
