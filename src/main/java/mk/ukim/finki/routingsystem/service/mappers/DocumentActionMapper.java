package mk.ukim.finki.routingsystem.service.mappers;

import mk.ukim.finki.routingsystem.model.Employee;
import mk.ukim.finki.routingsystem.model.documentEntities.DocumentAction;
import mk.ukim.finki.routingsystem.model.documentEntities.DocumentVersion;
import mk.ukim.finki.routingsystem.model.dto.DocumentAction.DisplayDocumentActionDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DocumentActionMapper {

    @Mapping(target = "actionId", source = "id")
    @Mapping(target = "document", source = "document.id")
    @Mapping(target = "actionType", source = "performedAction", qualifiedByName = "enumName")
    @Mapping(target = "note", source = "notes")
    @Mapping(target = "dateTime", source = "actionDateTime")
    @Mapping(target = "performedByEmployee", qualifiedByName = "fullName")
    @Mapping(target = "documentVersion", qualifiedByName = "versionLabel")
    DisplayDocumentActionDto toDto(DocumentAction documentAction);


    @Named("fullName")
    default String fullName(Employee employee) {
        if (employee == null) {
            return null;
        }

        return (employee.getFirstName() + " " + employee.getLastName()).trim();
    }

    @Named("versionLabel")
    default String versionLabel(DocumentVersion documentVersion) {
        if ((documentVersion == null) || ((documentVersion.getVersionNumber()) == 0)) {
            return null;
        }
        return "v " + documentVersion.getVersionNumber();
    }

    @Named("enumName")
    default String enumName(Enum<?> enumValue) {

        return (enumValue == null) ? null : enumValue.name();
    }
}
