package mk.ukim.finki.routingsystem.model.dto.Document;


import mk.ukim.finki.routingsystem.model.Employee;

import java.util.List;

public record RoutingResultDto(

        Long departmentId,
        List<Employee> routedToEmployees
) {}
