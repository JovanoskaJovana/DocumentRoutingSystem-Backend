package mk.ukim.finki.routingsystem.service.mappers;

import jakarta.persistence.NamedQueries;
import mk.ukim.finki.routingsystem.model.Employee;
import mk.ukim.finki.routingsystem.model.documentEntities.DocumentDownload;
import mk.ukim.finki.routingsystem.model.documentEntities.DocumentVersion;
import mk.ukim.finki.routingsystem.model.dto.DocumentDownload.DisplayDocumentDownloadDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;


@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DocumentDownloadMapper {

    @Mapping(target = "downloadId", source = "id")
    @Mapping(target = "documentTitle", source = "document.title")
    @Mapping(target = "employee", qualifiedByName = "fullName")
    @Mapping(target = "versionNumber", source = "documentVersion", qualifiedByName = "versionLabel")
    @Mapping(target = "downloadedAt", source = "downloadDateTime")
    DisplayDocumentDownloadDto toDto(DocumentDownload documentDownload);


    @Named("fullName")
    default String fullName(Employee employee) {
        if (employee == null) {
            return null;
        }
        return employee.getFirstName() + " " + employee.getLastName();
    }

    @Named("versionLabel")
    default String versionLabel(DocumentVersion documentVersion) {
        if (documentVersion == null || documentVersion.getVersionNumber() == 0) {
            return null;
        }
        return "v" + documentVersion.getVersionNumber();
    }
}
