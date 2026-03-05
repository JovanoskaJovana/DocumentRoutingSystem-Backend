package mk.ukim.finki.routingsystem.repository;

import mk.ukim.finki.routingsystem.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {


    Department findByDepartmentKey(String departmentKey);

    List<Department> findAllByDepartmentKeyInAndCompany_Id(List<String> departmentKey, Long companyId);

    List<Department> findAllByCompany_Id(Long companyId);

    Optional<Department> findByIdAndCompany_Id(Long id, Long companyId);

    Optional<Department> findByDepartmentKeyAndCompany_Id(String departmentKey, Long companyId);

}
