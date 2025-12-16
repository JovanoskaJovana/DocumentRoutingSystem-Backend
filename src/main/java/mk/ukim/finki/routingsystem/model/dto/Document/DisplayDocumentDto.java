package mk.ukim.finki.routingsystem.model.dto.Document;

import java.time.LocalDateTime;

public record DisplayDocumentDto(
        
         Long documentId,
         String title,
         String uploadedByEmployee,
         LocalDateTime uploadedDateTime,
         String documentStatus,
         String currentVersion,
         String currentVersionDownloadUrl

) {} 
