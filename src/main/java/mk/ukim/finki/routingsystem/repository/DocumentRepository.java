package mk.ukim.finki.routingsystem.repository;

import mk.ukim.finki.routingsystem.model.documentEntities.Document;
import mk.ukim.finki.routingsystem.model.dto.Document.DocumentListRow;
import mk.ukim.finki.routingsystem.model.dto.Document.DocumentSignatoryName;
import mk.ukim.finki.routingsystem.model.enumerations.DocumentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Repository for managing {@link Document} entities.
 */

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

  /**
   * Retrieves a paginated list of documents routed to a given department within a company, ordered by upload date and time.
   * Eagerly loads the current document version.
   *
   * @param departmentId the id of the given department
   * @param companyId    the id of the given company
   * @param pageable     pagination and sorting information
   * @return a {@link Page} of {@link Document} matching the given department and company id
   */
  @EntityGraph(attributePaths = {"currentDocumentVersion"})
  Page<Document> findAllByRoutedToDepartment_IdAndCompany_IdOrderByUploadDateTime(Long departmentId, Long companyId, Pageable pageable);

  /**
   * Retrieves a paginated list of documents that match given statuses and are routed to a specific employee within a company, ordered by upload date and time.
   * Loads only the columns the list needs, in one query: the uploader name and the current version number, but not the file data.
   *
   * @param statuses   the given list of statuses
   * @param employeeId the id of the employee that documents are routed to
   * @param companyId  the id of the given company
   * @param pageable   pagination and sorting information
   * @return a {@link Page} of {@link DocumentListRow} matching the given statuses, employee and company
   */
  @Query(value = """
          select new mk.ukim.finki.routingsystem.model.dto.Document.DocumentListRow(
                  d.id, d.title, u.firstName, u.lastName, d.uploadDateTime, d.documentStatus, v.id, v.versionNumber)
          from Document d
          join d.uploadedByEmployee u
          left join d.currentDocumentVersion v
          join d.routedToEmployees r
          where d.documentStatus in :statuses and r.id = :employeeId and d.company.id = :companyId
          order by d.uploadDateTime
          """,
          countQuery = """
                  select count(d)
                  from Document d
                  join d.routedToEmployees r
                  where d.documentStatus in :statuses and r.id = :employeeId and d.company.id = :companyId
                  """)
  Page<DocumentListRow> findListRowsRoutedToEmployee(@Param("statuses") List<DocumentStatus> statuses,
                                                     @Param("employeeId") Long employeeId,
                                                     @Param("companyId") Long companyId,
                                                     Pageable pageable);

  /**
   * Lists the names of the employees that each of the given documents is routed to, in one query.
   *
   * @param documentIds the ids of the given documents
   * @return one {@link DocumentSignatoryName} per document and employee
   */
  @Query("""
          select new mk.ukim.finki.routingsystem.model.dto.Document.DocumentSignatoryName(d.id, e.firstName, e.lastName)
          from Document d
          join d.routedToEmployees e
          where d.id in :documentIds
          order by e.firstName, e.lastName
          """)
  List<DocumentSignatoryName> findSignatoryNames(@Param("documentIds") Collection<Long> documentIds);

  /**
   * Retrieves a paginated list of documents that match given statuses and are uploaded by a specific employee within a company, ordered by upload date and time.
   * Eagerly loads the current document version.
   *
   * @param statuses   the given list of statuses
   * @param employeeId the id of the employee that uploaded the documents
   * @param companyId  the id of the given company
   * @param pageable   pagination and sorting information
   * @return a {@link Page} of {@link Document} matching the given statuses, employee and company
   */
  @EntityGraph(attributePaths = {"currentDocumentVersion"})
  Page<Document> findAllByDocumentStatusInAndUploadedByEmployee_IdAndCompany_IdOrderByUploadDateTime(List<DocumentStatus> statuses, Long employeeId, Long companyId, Pageable pageable);

  /**
   * Returns the document by given id within a company.
   * Eagerly retrieves the current and all document versions.
   *
   * @param id        the id of the given document
   * @param companyId the id of the given company
   * @return an {@link Optional} of {@link Document} matching the given document and company
   */
  @EntityGraph(attributePaths = {"currentDocumentVersion", "allDocumentVersions"})
  Optional<Document> findWithVersionsByIdAndCompany_Id(Long id, Long companyId);

  /**
   * Returns the document by given id within a company.
   *
   * @param id        the id of the given document
   * @param companyId the id of the given company
   * @return an {@link Optional} of {@link Document} matching the given document and company
   */
  Optional<Document> findByIdAndCompany_Id(Long id, Long companyId);

}
 