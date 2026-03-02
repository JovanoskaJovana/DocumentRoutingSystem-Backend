package mk.ukim.finki.routingsystem.model.exceptions;

public class CompanyNotFoundException extends RuntimeException {
    public CompanyNotFoundException(String message) {
        super(message);
    }
}
