package mk.ukim.finki.routingsystem.service.implementations;


import mk.ukim.finki.routingsystem.model.Department;
import mk.ukim.finki.routingsystem.model.Employee;
import mk.ukim.finki.routingsystem.model.dto.Employee.CreateDisplayEmployeeDto;
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
    public List<CreateDisplayEmployeeDto> listAll() {

        return employeeRepository.findAll()
                .stream().map(employeeMapper::toDto).toList();

    }

    @Override
    public Optional<CreateDisplayEmployeeDto> findById(Long id) {
      
        return employeeRepository.findById(id)
                .map(employeeMapper::toDto);

    }

    @Transactional
    @Override
    public CreateDisplayEmployeeDto save(CreateDisplayEmployeeDto createDisplayEmployeeDto) {

        if (createDisplayEmployeeDto.departmentId() == null) {
            throw new DepartmentNotFoundException("Department ID is required");
        }

        Optional <Department> department = departmentRepository.findById(createDisplayEmployeeDto.departmentId());
        if (department.isEmpty()) {
            throw new DepartmentNotFoundException("Department not found");
        }

        Employee employee = employeeMapper.toNewEntity(createDisplayEmployeeDto);
        employee.setDepartment(department.get());

        if (createDisplayEmployeeDto.password() != null && !createDisplayEmployeeDto.password().isBlank()) {
            employee.setPasswordHash(passwordEncoder.encode(createDisplayEmployeeDto.password()));
        }


        return employeeMapper.toDto(employeeRepository.save(employee));

    }

    @Transactional
    @Override
    public Optional<CreateDisplayEmployeeDto> update(Long employeeId, CreateDisplayEmployeeDto createDisplayEmployeeDto) {

        Optional<Employee> optionalEmployee = employeeRepository.findById(employeeId);
        if (optionalEmployee.isEmpty()) {
            return Optional.empty();
        }
        Employee employee = optionalEmployee.get();

        if (createDisplayEmployeeDto.departmentId() != null) {
            Optional <Department> department = departmentRepository.findById(createDisplayEmployeeDto.departmentId());
            if (department.isEmpty()) {
                return Optional.empty();
            }
            employee.setDepartment(department.get());
        }

        employeeMapper.updateEntityFromDto(createDisplayEmployeeDto, employee);


        if (createDisplayEmployeeDto.password() != null && !createDisplayEmployeeDto.password().isBlank()) {
            employee.setPasswordHash(passwordEncoder.encode(createDisplayEmployeeDto.password()));
        }

        CreateDisplayEmployeeDto savedDto = employeeMapper.toDto(employeeRepository.save(employee));
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
