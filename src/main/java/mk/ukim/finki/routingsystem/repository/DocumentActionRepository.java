package mk.ukim.finki.routingsystem.repository;

import mk.ukim.finki.routingsystem.model.documentEntities.DocumentAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentActionRepository extends JpaRepository<DocumentAction, Long> {

    // lists all the actions made on a document
    List<DocumentAction> findByDocument_IdAndDocument_Company_IdOrderByActionDateTime(Long documentId, Long companyId);

    // lists all the actions made by a specific employee
    List<DocumentAction> findByDocument_IdAndPerformedByEmployee_IdAndDocument_Company_IdOrderByActionDateTime(Long documentId, Long performedByEmployeeId, Long companyId);

}
