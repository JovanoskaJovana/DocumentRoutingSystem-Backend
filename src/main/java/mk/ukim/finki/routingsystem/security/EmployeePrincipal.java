package mk.ukim.finki.routingsystem.security;

import lombok.Getter;
import mk.ukim.finki.routingsystem.model.enumerations.EmployeeType;
import mk.ukim.finki.routingsystem.model.enumerations.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class EmployeePrincipal implements UserDetails {

    private final Long employeeId;
    private final Role role;
    private final EmployeeType employeeType;
    private final Long departmentId;

    public EmployeePrincipal(Long employeeId, Role role, EmployeeType employeeType, Long departmentId) {
        this.employeeId = employeeId;
        this.role = role;
        this.employeeType = employeeType;
        this.departmentId = departmentId;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of((GrantedAuthority) () -> "ROLE_" + role.name());
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return employeeId.toString();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
