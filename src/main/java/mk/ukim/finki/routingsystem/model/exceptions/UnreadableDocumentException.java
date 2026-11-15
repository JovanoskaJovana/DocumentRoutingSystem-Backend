package mk.ukim.finki.routingsystem.model.exceptions;

/**
 * Thrown when the text of a document cannot be read: the file is missing, password-protected or damaged.
 */

public class UnreadableDocumentException extends RuntimeException {
  public UnreadableDocumentException(String message) {
    super(message);
  }

  public UnreadableDocumentException(String message, Throwable cause) {
    super(message, cause);
  }
}
