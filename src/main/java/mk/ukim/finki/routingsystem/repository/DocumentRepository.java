package mk.ukim.finki.routingsystem.repository;

import mk.ukim.finki.routingsystem.model.documentEntities.Document;
import mk.ukim.finki.routingsystem.model.enumerations.DocumentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

     // lists all the documents routed to the same department as the logged-in employee - only load current version
     @EntityGraph(attributePaths = {"currentDocumentVersion"})
     Page<Document> findAllByRoutedToDepartment_IdOrderByUploadDateTime(Long departmentId, Pageable pageable);

     // lists all the documents routed directly to the logged-in employee - only load current version
     @EntityGraph(attributePaths = {"currentDocumentVersion"})
     Page<Document> findAllByDocumentStatusInAndRoutedToEmployees_IdOrderByUploadDateTime(List<DocumentStatus> statuses, Long employeeId, Pageable pageable);

     @EntityGraph(attributePaths = {"currentDocumentVersion"})
     Page<Document> findAllByDocumentStatusInAndUploadedByEmployee_IdOrderByUploadDateTime(List<DocumentStatus> statuses, Long employeeId, Pageable pageable);

     // loads a document together with its full history - current + all versions
     @EntityGraph(attributePaths = {"currentDocumentVersion", "allDocumentVersions"})
     Optional<Document> findWithVersionsById(Long id);
     // findWithVersionsById fixes overriding findById

}
 