package mk.ukim.finki.routingsystem.model.exceptions;

public class RoutingRulesForTenantNotFound extends RuntimeException {
    public RoutingRulesForTenantNotFound(String message) {
        super(message);
    }
}
