package mk.ukim.finki.routingsystem.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import mk.ukim.finki.routingsystem.model.enumerations.EmployeeType;
import mk.ukim.finki.routingsystem.model.enumerations.Role;

public record CreateDisplayEmployeeDto(

        Long employeeId,
        String email,
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        String password,
        String firstName,
        String lastName,
        Long departmentId,
        Role role,
        EmployeeType employeeType

) {}
