package mk.ukim.finki.routingsystem.service.mappers;

import mk.ukim.finki.routingsystem.model.Employee;
import mk.ukim.finki.routingsystem.model.documentEntities.Document;
import mk.ukim.finki.routingsystem.model.documentEntities.DocumentVersion;
import mk.ukim.finki.routingsystem.model.dto.Document.DisplayAdminDocumentDto;
import mk.ukim.finki.routingsystem.model.dto.Document.DisplayDocumentDto;
import mk.ukim.finki.routingsystem.model.dto.Document.DocumentListRow;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.Set;

/**
 * MapStruct mapper for converting {@link Document} entities to DTOs.
 */

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

  /**
   * Combines the first and last name of the employee.
   *
   * @param employee the given employee
   * @return the full name of the employee, or null if the object is null
   */
  @Named("fullName")
  default String fullName(Employee employee) {
    if (employee == null) {
      return null;
    }
    return employee.getFirstName() + " " + employee.getLastName();
  }

  /**
   * Maps a set of employees to a list of their full names.
   *
   * @param employees the given set of employees
   * @return a list of full name strings for each employee, or an empty list if the set is null
   */
  @Named("fullNameList")
  default List<String> fullNames(Set<Employee> employees) {
    if (employees == null) {
      return List.of();
    }
    return employees.stream()
            .map(this::fullName)
            .toList();
  }

  /**
   * Formats the version number of a document version as a label (e.g. "v1", "v2").
   *
   * @param documentVersion the given document version
   * @return the formatted version number of a document, or null if the document version is null or 0
   */
  @Named("versionLabel")
  default String versionLabel(DocumentVersion documentVersion) {
    if (documentVersion == null || documentVersion.getId() == null) {
      return null;
    }
    return "v" + documentVersion.getVersionNumber();
  }

  /**
   * Builds the download URL for a given document version in the format
   * /api/documents/{documentId}/versions/{versionId}/download.
   *
   * @param documentVersion the given document version
   * @return the formatted download URL, or null if the version or its id is null
   */
  @Named("downloadUrl")
  default String downloadUrl(DocumentVersion documentVersion) {
    if (documentVersion == null || documentVersion.getId() == null) {
      return null;
    }

    Long documentId = documentVersion.getDocument() != null ? documentVersion.getDocument().getId() : null;

    return versionDownloadUrl(documentId, documentVersion.getId());
  }

  /**
   * Converts a {@link DocumentListRow} and the names of the employees the document is routed to into a DTO.
   * Gives the same result as {@link #toDto(Document)}, without loading the document entity.
   *
   * @param row               the given document row
   * @param routedToEmployees the full names of the employees the document is routed to
   * @return the {@link DisplayDocumentDto} for the row
   */
  default DisplayDocumentDto toDto(DocumentListRow row, List<String> routedToEmployees) {
    return new DisplayDocumentDto(
            row.documentId(),
            row.title(),
            row.uploaderFirstName() + " " + row.uploaderLastName(),
            routedToEmployees,
            row.uploadDateTime(),
            row.documentStatus() != null ? row.documentStatus().name() : null,
            row.versionId() != null ? "v" + row.versionNumber() : null,
            row.versionId() != null ? versionDownloadUrl(row.documentId(), row.versionId()) : null
    );
  }

  /**
   * Builds the download URL in the format /api/documents/{documentId}/versions/{versionId}/download.
   *
   * @param documentId the id of the document
   * @param versionId  the id of the document version
   * @return the formatted download URL
   */
  static String versionDownloadUrl(Long documentId, Long versionId) {
    return "/api/documents/" + documentId + "/versions/" + versionId + "/download";
  }

}
