package mk.ukim.finki.routingsystem.repository;

import mk.ukim.finki.routingsystem.model.ManualReviewAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ManualReviewActionRepository extends JpaRepository<ManualReviewAction, Long> {

    List<ManualReviewAction> findAllByCompany_IdAndManualChosenDepartment_Id(Long companyId, Long departmentId);
}
