package mk.ukim.finki.routingsystem.service;

import mk.ukim.finki.routingsystem.model.dto.Employee.CreateDisplayEmployeeDto;

import java.util.List;
import java.util.Optional;

public interface EmployeeService {

     List<CreateDisplayEmployeeDto> listAll();

     Optional<CreateDisplayEmployeeDto> findById(Long id);
  
     CreateDisplayEmployeeDto save(CreateDisplayEmployeeDto createDisplayEmployeeDto);

     Optional<CreateDisplayEmployeeDto> update(Long employeeId, CreateDisplayEmployeeDto createDisplayEmployeeDto);

     boolean delete(Long employeeId);

}
