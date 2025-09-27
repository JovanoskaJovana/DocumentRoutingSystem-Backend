package mk.ukim.finki.routingsystem.service.mappers;

import mk.ukim.finki.routingsystem.model.Employee;
import mk.ukim.finki.routingsystem.model.documentEntities.DocumentVersion;
import mk.ukim.finki.routingsystem.model.dto.DocumentVersion.DisplayDocumentVersionDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DocumentVersionMapper {

    @Mapping(target = "versionId", source = "id")
    @Mapping(target = "document", source = "document.title")
    @Mapping(target = "versionNumber", qualifiedByName = "versionLabel")
    @Mapping(target = "uploadedByEmployee", qualifiedByName = "fullName")
    DisplayDocumentVersionDto toDto (DocumentVersion documentVersion);

    @Named("fullName")
    default String fullName(Employee employee) {
        if (employee == null) {
            return null;
        }
        return employee.getFirstName() + " " + employee.getLastName();
    }


    @Named("versionLabel")
    default String versionLabel (int documentVersion) {
        if (documentVersion == 0) {
            return null;
        }
        return "v " + documentVersion;
    }

}
