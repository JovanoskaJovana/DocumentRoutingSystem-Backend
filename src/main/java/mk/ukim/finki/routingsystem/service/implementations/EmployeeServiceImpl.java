package mk.ukim.finki.routingsystem.service.implementations;


import mk.ukim.finki.routingsystem.model.Department;
import mk.ukim.finki.routingsystem.model.Employee;
import mk.ukim.finki.routingsystem.model.dto.EmployeeDto;
import mk.ukim.finki.routingsystem.model.exceptions.DepartmentNotFoundException;
import mk.ukim.finki.routingsystem.repository.DepartmentRepository;
import mk.ukim.finki.routingsystem.repository.EmployeeRepository;
import mk.ukim.finki.routingsystem.service.EmployeeService;
import mk.ukim.finki.routingsystem.service.mappers.EmployeeMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeMapper employeeMapper;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository, DepartmentRepository departmentRepository, PasswordEncoder passwordEncoder, EmployeeMapper employeeMapper) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.employeeMapper = employeeMapper;
    }


    @Override
    public List<EmployeeDto> listAll() {

        return employeeRepository.findAll()
                .stream().map(employeeMapper::toDto).toList();

    }

    @Override
    public Optional<EmployeeDto> findById(Long id) {
      
        return employeeRepository.findById(id)
                .map(employeeMapper::toDto);

    }

    @Transactional
    @Override
    public EmployeeDto save(EmployeeDto employeeDto) {

        if (employeeDto.departmentId() == null) {
            throw new DepartmentNotFoundException("Department ID is required");
        }

        Optional <Department> department = departmentRepository.findById(employeeDto.departmentId());
        if (department.isEmpty()) {
            throw new DepartmentNotFoundException("Department not found");
        }

        Employee employee = employeeMapper.toNewEntity(employeeDto);
        employee.setDepartment(department.get());

        if (employeeDto.password() != null && !employeeDto.password().isBlank()) {
            employee.setPasswordHash(passwordEncoder.encode(employeeDto.password()));
        }


        return employeeMapper.toDto(employeeRepository.save(employee));

    }

    @Transactional
    @Override
    public Optional<EmployeeDto> update(Long employeeId, EmployeeDto employeeDto) {

        Optional<Employee> optionalEmployee = employeeRepository.findById(employeeId);
        if (optionalEmployee.isEmpty()) {
            return Optional.empty();
        }
        Employee employee = optionalEmployee.get();

        if (employeeDto.departmentId() != null) {
            Optional <Department> department = departmentRepository.findById(employeeDto.departmentId());
            if (department.isEmpty()) {
                return Optional.empty();
            }
            employee.setDepartment(department.get());
        }

        employeeMapper.updateEntityFromDto(employeeDto, employee);


        if (employeeDto.password() != null && !employeeDto.password().isBlank()) {
            employee.setPasswordHash(passwordEncoder.encode(employeeDto.password()));
        }

        EmployeeDto savedDto = employeeMapper.toDto(employeeRepository.save(employee));
        return Optional.of(savedDto);

    }

    @Transactional
    @Override
    public boolean delete(Long employeeId) {

        if (!employeeRepository.existsById(employeeId)) {
            return false;
        }

        employeeRepository.deleteById(employeeId);
        return true;
    }


}
