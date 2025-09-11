package mk.ukim.finki.routingsystem.model.dto;

import java.time.LocalDateTime;

public record DocumentDownloadDto(
        Long downloadId,
        Long documentId,
        String documentTitle,
        String employee,
        String versionNumber,
        LocalDateTime downloadedAt
) {}
