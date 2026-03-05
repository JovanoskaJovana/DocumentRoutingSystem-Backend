package mk.ukim.finki.routingsystem.service;

import mk.ukim.finki.routingsystem.model.dto.Employee.CreateDisplayEmployeeDto;

import java.util.List;
import java.util.Optional;

public interface EmployeeService {

     List<CreateDisplayEmployeeDto> listAll(Long companyId);

     Optional<CreateDisplayEmployeeDto> findById(Long id, Long companyId);
  
     CreateDisplayEmployeeDto save(CreateDisplayEmployeeDto createDisplayEmployeeDto, Long companyId);

     Optional<CreateDisplayEmployeeDto> update(Long employeeId, CreateDisplayEmployeeDto createDisplayEmployeeDto, Long companyId);

     boolean delete(Long employeeId, Long companyId);

}
