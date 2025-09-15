package mk.ukim.finki.routingsystem.service;

import mk.ukim.finki.routingsystem.model.dto.EmployeeDto;

import java.util.List;

public interface EmployeeService {

     List<EmployeeDto> listAll();

     EmployeeDto findById(Long id);

     EmployeeDto create(EmployeeDto employeeDto);

     EmployeeDto update(Long employeeId, EmployeeDto employeeDto);

    void delete(Long employeeId);

}
