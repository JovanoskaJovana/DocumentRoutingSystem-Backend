package mk.ukim.finki.routingsystem.model.dto.Company;

public record ResponseCompanyDto(
        String name,
        String code,
        Boolean active
) { }
