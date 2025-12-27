package mk.ukim.finki.routingsystem.service.mappers;

import mk.ukim.finki.routingsystem.model.Employee;
import mk.ukim.finki.routingsystem.model.documentEntities.Document;
import mk.ukim.finki.routingsystem.model.documentEntities.DocumentVersion;
import mk.ukim.finki.routingsystem.model.dto.Document.DisplayAdminDocumentDto;
import mk.ukim.finki.routingsystem.model.dto.Document.DisplayDocumentDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DocumentMapper {


    @Mapping(target = "documentId", source = "id")
    @Mapping(target = "uploadedByEmployee", qualifiedByName = "fullName")
    @Mapping(target = "documentStatus", expression = "java(document.getDocumentStatus() != null ? document.getDocumentStatus().name() : null)")
    @Mapping(target = "currentVersion", source = "currentDocumentVersion", qualifiedByName = "versionLabel")
    @Mapping(target = "currentVersionDownloadUrl", source = "currentDocumentVersion", qualifiedByName = "downloadUrl")
    @Mapping(target = "routedToEmployees", source = "routedToEmployees", qualifiedByName = "fullNameList")
    @Mapping(target = "uploadedDateTime", source = "uploadDateTime")
    DisplayDocumentDto toDto(Document document);


    @Mapping(target = "documentId", source = "id")
    @Mapping(target = "uploadedByEmployee", qualifiedByName = "fullName")
    @Mapping(target = "documentStatus", expression = "java(document.getDocumentStatus() != null ? document.getDocumentStatus().name() : null)")
    @Mapping(target = "currentVersion", source = "currentDocumentVersion", qualifiedByName = "versionLabel")
    @Mapping(target = "routedToDepartment", expression = "java(document.getRoutedToDepartment() != null ? document.getRoutedToDepartment().getName() : null)")
    @Mapping(target = "currentVersionDownloadUrl", source = "currentDocumentVersion", qualifiedByName = "downloadUrl")
    @Mapping(target = "routedToEmployees", source = "routedToEmployees", qualifiedByName = "fullNameList")
    @Mapping(target = "uploadedDateTime", source = "uploadDateTime")
    DisplayAdminDocumentDto toAdminDto(Document document);

    @Named("fullName")
    default String fullName(Employee employee) {
        if (employee == null) {
            return null;
        }
        return employee.getFirstName() + " " + employee.getLastName();
    }

    @Named("fullNameList")
    default List<String> fullNames(Set<Employee> employees) {
        if (employees == null) {
            return List.of();
        }
        return employees.stream()
                .map(this::fullName)
                .toList();
    }

    @Named("versionLabel")
    default String versionLabel(DocumentVersion documentVersion) {
        if (documentVersion == null || documentVersion.getId() == null) {
            return null;
        }
        return "v" + documentVersion.getVersionNumber();
    }

    @Named("downloadUrl")
    default String downloadUrl(DocumentVersion documentVersion) {
        if (documentVersion == null || documentVersion.getId() == null) {
            return null;
        }

        Long documentId = documentVersion.getDocument() != null ? documentVersion.getDocument().getId() : null;

        return "/api/documents/" + documentId + "/versions/" +
                documentVersion.getId() + "/download";
    }

}
