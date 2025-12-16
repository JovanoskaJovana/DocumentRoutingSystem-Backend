package mk.ukim.finki.routingsystem.events;

import java.time.LocalDateTime;

public record DocumentDownloadRequestedEvent(
        Long documentId,
        String documentTitle,
        Long employeeId,
        Long versionId,
        LocalDateTime downloadedAt
) {}
