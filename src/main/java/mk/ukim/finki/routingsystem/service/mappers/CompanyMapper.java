package mk.ukim.finki.routingsystem.service.mappers;

import mk.ukim.finki.routingsystem.model.Company;
import mk.ukim.finki.routingsystem.model.dto.Company.CreateCompanyDto;
import mk.ukim.finki.routingsystem.model.dto.Company.ResponseCompanyDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CompanyMapper {


    ResponseCompanyDto toDto(Company company);

    @Mapping(target = "active", ignore = true)
    Company toEntity(CreateCompanyDto createCompanyDto);
}
