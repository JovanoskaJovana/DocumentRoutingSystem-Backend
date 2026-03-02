package mk.ukim.finki.routingsystem.model.dto;

public record CreateDisplayDepartmentDto(

        Long id,
        String name,
        String key,
        Long companyId
) {}
