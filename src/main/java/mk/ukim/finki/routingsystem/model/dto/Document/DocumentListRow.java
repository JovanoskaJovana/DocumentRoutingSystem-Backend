package mk.ukim.finki.routingsystem.model.dto.Document;

import mk.ukim.finki.routingsystem.model.enumerations.DocumentStatus;

import java.time.LocalDateTime;

/**
 * Projection of one document in a list, loaded with a single query and without the file data.
 *
 * @param documentId        the id of the document
 * @param title             the title of the document
 * @param uploaderFirstName the first name of the employee that uploaded the document
 * @param uploaderLastName  the last name of the employee that uploaded the document
 * @param uploadDateTime    the upload date and time
 * @param documentStatus    the status of the document
 * @param versionId         the id of the current version, or null if there is none
 * @param versionNumber     the number of the current version, or null if there is none
 */
public record DocumentListRow(
        Long documentId,
        String title,
        String uploaderFirstName,
        String uploaderLastName,
        LocalDateTime uploadDateTime,
        DocumentStatus documentStatus,
        Long versionId,
        Integer versionNumber
) {
}
