package mk.ukim.finki.routingsystem.service;

import mk.ukim.finki.routingsystem.model.dto.DepartmentDto;

import java.util.List;
import java.util.Optional;

public interface DepartmentService {

    List<DepartmentDto> listAll();

    Optional<DepartmentDto> findById(Long departmentId);

    DepartmentDto save(DepartmentDto departmentDto);

    Optional<DepartmentDto> update(Long departmentId, DepartmentDto departmentDto);

    boolean delete(Long departmentId);
}
