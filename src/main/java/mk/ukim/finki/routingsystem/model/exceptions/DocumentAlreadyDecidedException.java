package mk.ukim.finki.routingsystem.model.exceptions;

/**
 * Thrown when a document that was already approved or rejected is approved or rejected again.
 */

public class DocumentAlreadyDecidedException extends RuntimeException {
  public DocumentAlreadyDecidedException(String message) {
    super(message);
  }
}
