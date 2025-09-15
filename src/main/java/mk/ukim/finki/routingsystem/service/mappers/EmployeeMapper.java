package mk.ukim.finki.routingsystem.service.mappers;

import mk.ukim.finki.routingsystem.model.Employee;
import mk.ukim.finki.routingsystem.model.dto.EmployeeDto;
import org.mapstruct.*;

@Mapper (componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EmployeeMapper {

    // employeeDto for responses (never expose the password)

    @Mapping(target = "employeeId", source = "id")
    @Mapping(target = "departmentId", source = "department.id")
    @Mapping(target = "employeeType", source = "type")
    @Mapping(target = "password", ignore = true)
    EmployeeDto toDto(Employee employee);

    // create a new Employee from employeeDto

    @Mapping(target = "id",          ignore = true)
    @Mapping(target = "type",        source = "employeeType")
    @Mapping(target = "department",  ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    Employee toNewEntity(EmployeeDto employeeDto);


    // update Employee entity from employeeDto (in-place)

    @BeanMapping(ignoreByDefault = true)
    @Mappings({
            @Mapping(target = "email", source = "email"),
            @Mapping(target = "firstName", source = "firstName"),
            @Mapping(target = "lastName", source = "lastName"),
            @Mapping(target = "role", source = "role"),
            @Mapping(target = "type", source = "employeeType"),
    })
    void updateEntityFromDto(EmployeeDto employeeDto, @MappingTarget Employee employeeTarget);

}
