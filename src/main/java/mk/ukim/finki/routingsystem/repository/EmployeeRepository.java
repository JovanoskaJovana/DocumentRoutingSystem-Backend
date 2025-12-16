package mk.ukim.finki.routingsystem.repository;

import mk.ukim.finki.routingsystem.model.Employee;
import mk.ukim.finki.routingsystem.model.enumerations.EmployeeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // finds an employee by email
    Optional<Employee> findByEmail(String email);

    // finds all employees by specific department and employeeType
    List<Employee> findAllByDepartment_IdAndType(Long departmentId, EmployeeType employeeType);
}
