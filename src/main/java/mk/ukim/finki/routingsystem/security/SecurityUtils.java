package mk.ukim.finki.routingsystem.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    private SecurityUtils () {}

    public static EmployeePrincipal me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof EmployeePrincipal employeePrincipal) {
            return employeePrincipal;
        }
        return null;
    }
}
