package mk.ukim.finki.routingsystem.model.dto.Document;

import java.time.LocalDateTime;
import java.util.List;

public record DisplayAdminDocumentDto(

        Long documentId,
        String title,
        String uploadedByEmployee,
        String routedToDepartment,
        List<String> routedToEmployees,
        LocalDateTime uploadedDateTime,
        String documentStatus,
        String currentVersion,
        String currentVersionDownloadUrl
) {}
