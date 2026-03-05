package mk.ukim.finki.routingsystem.repository;

import mk.ukim.finki.routingsystem.model.documentEntities.DocumentVersion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, Long> {

     // lists all versions of a document, ordered by the newest first
     Page<DocumentVersion> findByDocument_IdAndDocument_Company_IdOrderByVersionNumberDesc(Long documentId, Long companyId, Pageable pageable);

     Optional<DocumentVersion> findByIdAndDocument_Company_Id(Long documentId, Long companyId);

}
