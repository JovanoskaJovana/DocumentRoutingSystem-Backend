package mk.ukim.finki.routingsystem.model.dto.Document;

/**
 * Projection of one employee a document is routed to.
 *
 * @param documentId the id of the document
 * @param firstName  the first name of the employee
 * @param lastName   the last name of the employee
 */
public record DocumentSignatoryName(
        Long documentId,
        String firstName,
        String lastName
) {
}
