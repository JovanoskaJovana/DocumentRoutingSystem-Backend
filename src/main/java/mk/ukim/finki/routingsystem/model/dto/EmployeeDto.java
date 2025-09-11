package mk.ukim.finki.routingsystem.model.dto;

public record EmployeeDto(

        Long employeeId,
        String email,
        String firstName,
        String lastName,
        String department,
        String role,
        String employeeType

) {}
