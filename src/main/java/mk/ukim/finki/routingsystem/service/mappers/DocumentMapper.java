package mk.ukim.finki.routingsystem.service.mappers;

import mk.ukim.finki.routingsystem.model.Employee;
import mk.ukim.finki.routingsystem.model.documentEntities.Document;
import mk.ukim.finki.routingsystem.model.documentEntities.DocumentVersion;
import mk.ukim.finki.routingsystem.model.dto.AdminRoutedDocumentDto;
import mk.ukim.finki.routingsystem.model.dto.RoutedDocumentDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.List;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DocumentMapper {

    // DocumentDto for responses

    @Mapping(target = "documentId", source = "id")
    @Mapping(target = "uploadedDateTime", source = "uploadDateTime")
    @Mapping(target = "currentVersion", source = "currentDocumentVersion", qualifiedByName = "version")
    @Mapping(target = "uploadedByEmployee", source = "uploadedByEmployee", qualifiedByName = "fullName")
    @Mapping(target = "currentVersionDownloadUrl", source = "currentDocumentVersion", qualifiedByName = "downloadUrl")
    @Mapping(target = "documentStatus",     expression = "java(document.getDocumentStatus() != null ? document.getDocumentStatus().name() : null)")
    RoutedDocumentDto toDto(Document document);

    // AdminDocumentDto for responses

    @Mapping(target = "documentId", source = "id")
    @Mapping(target = "uploadedDateTime", source = "uploadDateTime")
    @Mapping(target = "currentVersion", source = "currentDocumentVersion", qualifiedByName = "version")
    @Mapping(target = "uploadedByEmployee", source = "uploadedByEmployee", qualifiedByName = "fullName")
    @Mapping(target = "currentVersionDownloadUrl", source = "currentDocumentVersion", qualifiedByName = "downloadUrl")
    @Mapping(
            target = "routedToDepartment",
            expression = "java(document.getRoutedToDepartment() != null ? document.getRoutedToDepartment().getName() : null)"
    )
    @Mapping(target = "documentStatus",     expression = "java(document.getDocumentStatus() != null ? document.getDocumentStatus().name() : null)")
    AdminRoutedDocumentDto toAdminDto(Document document);


    // default implementation for listing an employee's full name

    @Named("fullName")
    default String fullName(Employee employee) {
        if (employee == null) {
            return null;
        }

        return (employee.getFirstName() + " " + employee.getLastName()).trim();
    }

    List<RoutedDocumentDto> toRoutedDtoList(List<Document> docs);

    List<AdminRoutedDocumentDto> toAdminDtoList(List<Document> docs);

    // default implementation for listing a document's version

    @Named("version")
    default String versionLabel(DocumentVersion documentVersion) {
        if ((documentVersion == null) || ((documentVersion.getVersionNumber()) == 0)) {
            return null;
        }
        return "v " + documentVersion.getVersionNumber();
    }

    // default implementation for providing the url for downloading the latest document version

    @Named("downloadUrl")
    default String downloadUrl(DocumentVersion documentVersion) {
        if (documentVersion == null || documentVersion.getId() == null) {
            return null;
        }
        return "/api/documents/versions/" + documentVersion.getId() + "/download";
    }


}
