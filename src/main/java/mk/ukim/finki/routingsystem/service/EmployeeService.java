package mk.ukim.finki.routingsystem.service;

import mk.ukim.finki.routingsystem.model.dto.EmployeeDto;

import java.util.List;
import java.util.Optional;

public interface EmployeeService {

     List<EmployeeDto> listAll();

     Optional<EmployeeDto> findById(Long id);

     Optional<EmployeeDto> create(EmployeeDto employeeDto);

     Optional<EmployeeDto> update(Long employeeId, EmployeeDto employeeDto);

     boolean delete(Long employeeId);

}
