package mk.ukim.finki.routingsystem.model.dto;

import java.time.LocalDateTime;

public record RoutedDocumentDto(
        
         Long documentId,
         String title,
         String uploadedByEmployee,
         LocalDateTime uploadedDateTime,
         String documentStatus,
         String currentVersion,
         String currentVersionDownloadUrl

) {} 
