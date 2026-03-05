package mk.ukim.finki.routingsystem.repository;

import mk.ukim.finki.routingsystem.model.Employee;
import mk.ukim.finki.routingsystem.model.enumerations.EmployeeType;
import mk.ukim.finki.routingsystem.model.enumerations.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {


    Set<Employee> findAllByCompany_IdAndDepartment_IdAndType(Long companyId, Long departmentId, EmployeeType employeeType);

    Optional<Employee> findByEmailAndCompany_Code(String email, String companyCode);

    Optional<Employee> findByEmailAndRole(String email, Role role);

    List<Employee> findAllByCompany_Id(Long companyId);

    Optional<Employee> findByIdAndCompany_Id(Long id, Long companyId);
}
