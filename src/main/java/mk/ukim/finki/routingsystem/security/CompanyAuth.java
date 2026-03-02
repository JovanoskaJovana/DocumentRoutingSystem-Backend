package mk.ukim.finki.routingsystem.security;

import mk.ukim.finki.routingsystem.model.enumerations.Role;
import mk.ukim.finki.routingsystem.repository.CompanyRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("companyAuth")
public class CompanyAuth {

    private final CompanyRepository companyRepository;

    public CompanyAuth(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public boolean canChangeActivity (Authentication authentication) {

        if (authentication == null || !(authentication.getPrincipal() instanceof EmployeePrincipal employeePrincipal)) {
            return false;
        }

        return employeePrincipal.getRole() == Role.SUPER_ADMIN;
    }

    public boolean canCreateCompany (Authentication authentication) {

        if (authentication == null || !(authentication.getPrincipal() instanceof EmployeePrincipal employeePrincipal)) {
            return false;
        }

        return employeePrincipal.getRole() == Role.SUPER_ADMIN;
    }

}
