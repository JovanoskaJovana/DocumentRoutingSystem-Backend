package mk.ukim.finki.routingsystem.service.implementations;


import mk.ukim.finki.routingsystem.model.Department;
import mk.ukim.finki.routingsystem.model.Employee;
import mk.ukim.finki.routingsystem.model.dto.EmployeeDto;
import mk.ukim.finki.routingsystem.model.exceptions.DepartmentNotFoundException;
import mk.ukim.finki.routingsystem.model.exceptions.EmployeeNotFoundException;
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

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));

        return Optional.of(employeeMapper.toDto(employee));

    }

    @Transactional
    @Override
    public Optional<EmployeeDto> create(EmployeeDto employeeDto) {

        Department department = departmentRepository.findById(employeeDto.departmentId())
                .orElseThrow(() -> new DepartmentNotFoundException("Department not found"));

        Employee employee = employeeMapper.toNewEntity(employeeDto);
        employee.setDepartment(department);

        if (employeeDto.password() != null && !employeeDto.password().isBlank()) {
            employee.setPasswordHash(passwordEncoder.encode(employeeDto.password()));
        }

        return Optional.of(employeeMapper.toDto(employeeRepository.save(employee)));

    }

    @Transactional
    @Override
    public Optional<EmployeeDto> update(Long employeeId, EmployeeDto employeeDto) {

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));

        Department department = departmentRepository.findById(employeeDto.departmentId())
                .orElseThrow(() -> new DepartmentNotFoundException("Department not found"));

        employeeMapper.updateEntityFromDto(employeeDto, employee);
        employee.setDepartment(department);

        if (employeeDto.password() != null && !employeeDto.password().isBlank()) {
            employee.setPasswordHash(passwordEncoder.encode(employee.getPasswordHash()));
        }

        return Optional.of(employeeMapper.toDto(employeeRepository.save(employee)));

    }

    @Transactional
    @Override
    public void delete(Long employeeId) {

        if (!employeeRepository.existsById(employeeId)) {
            throw new EmployeeNotFoundException("Employee not found");
        }

        employeeRepository.deleteById(employeeId);
    }


}
