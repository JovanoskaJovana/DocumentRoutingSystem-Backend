package mk.ukim.finki.routingsystem.repository;

import mk.ukim.finki.routingsystem.model.documentEntities.DocumentDownload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentDownloadRepository extends JpaRepository<DocumentDownload, Long> {

    // lists all the documents that the logged-in employee has downloaded
    List<DocumentDownload> findAllByEmployee_IdAndDocument_Company_IdOrderByDownloadDateTimeDesc(Long id, Long companyId);

    //lists all the downloads made from a specific document
    List<DocumentDownload> findAllByDocument_IdAndDocument_Company_Id(Long id, Long companyId);

}
