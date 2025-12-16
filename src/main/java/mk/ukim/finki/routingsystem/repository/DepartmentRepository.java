package mk.ukim.finki.routingsystem.repository;

import mk.ukim.finki.routingsystem.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {



}
