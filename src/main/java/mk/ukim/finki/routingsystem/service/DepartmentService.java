package mk.ukim.finki.routingsystem.service;

import mk.ukim.finki.routingsystem.model.dto.CreateDisplayDepartmentDto;

import java.util.List;
import java.util.Optional;

public interface DepartmentService {

    List<CreateDisplayDepartmentDto> listAll(Long companyId);

    Optional<CreateDisplayDepartmentDto> findById(Long departmentId, Long companyId);

    CreateDisplayDepartmentDto save(CreateDisplayDepartmentDto createDisplayDepartmentDto, Long companyId);

    Optional<CreateDisplayDepartmentDto> update(Long companyId, Long departmentId, CreateDisplayDepartmentDto createDisplayDepartmentDto);

    List<CreateDisplayDepartmentDto> getManualReviewDepartments(Long documentId, Long companyId);

    boolean delete(Long companyId, Long departmentId);
}
