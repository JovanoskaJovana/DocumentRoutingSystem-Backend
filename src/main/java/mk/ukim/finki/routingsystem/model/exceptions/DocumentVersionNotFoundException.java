package mk.ukim.finki.routingsystem.model.exceptions;

public class DocumentVersionNotFoundException extends RuntimeException {
    public DocumentVersionNotFoundException(String message) {
        super(message);
    }
}
