package mk.ukim.finki.routingsystem.model.exceptions;

public class CompanyAlreadyExistsException extends RuntimeException {
    public CompanyAlreadyExistsException(String message) {
        super(message);
    }
}
