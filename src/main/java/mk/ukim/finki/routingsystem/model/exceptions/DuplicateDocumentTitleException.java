package mk.ukim.finki.routingsystem.model.exceptions;

/**
 * Thrown when a company already has a document with the given title.
 */

public class DuplicateDocumentTitleException extends RuntimeException {

  public static final String MESSAGE = "A document with this title already exists.";

  public DuplicateDocumentTitleException() {
    super(MESSAGE);
  }
}
