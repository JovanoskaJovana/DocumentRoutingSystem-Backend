package mk.ukim.finki.routingsystem.service;

import mk.ukim.finki.routingsystem.model.dto.CreateDisplayDepartmentDto;

import java.util.List;
import java.util.Optional;

public interface DepartmentService {

    List<CreateDisplayDepartmentDto> listAll();

    Optional<CreateDisplayDepartmentDto> findById(Long departmentId);

    CreateDisplayDepartmentDto save(CreateDisplayDepartmentDto createDisplayDepartmentDto);

    Optional<CreateDisplayDepartmentDto> update(Long departmentId, CreateDisplayDepartmentDto createDisplayDepartmentDto);

    boolean delete(Long departmentId);
}
