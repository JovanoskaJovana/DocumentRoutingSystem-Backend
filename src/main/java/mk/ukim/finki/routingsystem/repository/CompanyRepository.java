package mk.ukim.finki.routingsystem.repository;

import mk.ukim.finki.routingsystem.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    boolean existsByCode(String code);

}
