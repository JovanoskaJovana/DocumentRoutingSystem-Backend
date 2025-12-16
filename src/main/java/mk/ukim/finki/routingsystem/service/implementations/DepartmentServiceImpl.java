package mk.ukim.finki.routingsystem.service.implementations;

import mk.ukim.finki.routingsystem.model.Department;
import mk.ukim.finki.routingsystem.model.dto.CreateDisplayDepartmentDto;
import mk.ukim.finki.routingsystem.repository.DepartmentRepository;
import mk.ukim.finki.routingsystem.service.DepartmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class DepartmentServiceImpl implements DepartmentService {


    private final DepartmentRepository departmentRepository;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Transactional(readOnly = true)
    @Override
    public List<CreateDisplayDepartmentDto> listAll() {

        return departmentRepository.findAll()
                .stream()
                .map(d -> new CreateDisplayDepartmentDto(d.getId(), d.getName()))
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<CreateDisplayDepartmentDto> findById(Long departmentId) {

        return departmentRepository.findById(departmentId)
                .map(d -> new CreateDisplayDepartmentDto(d.getId(), d.getName()));
    }

    @Transactional
    @Override
    public CreateDisplayDepartmentDto save(CreateDisplayDepartmentDto createDisplayDepartmentDto) {

        Department savedDepartment = departmentRepository
                .save(new Department(createDisplayDepartmentDto.name()));

        return new CreateDisplayDepartmentDto(savedDepartment.getId(), savedDepartment.getName());

    }

    @Transactional
    @Override
    public Optional<CreateDisplayDepartmentDto> update(Long departmentId, CreateDisplayDepartmentDto createDisplayDepartmentDto) {

        return departmentRepository.findById(departmentId)
                .map(d -> {
                    d.setName(createDisplayDepartmentDto.name());
                    departmentRepository.save(d);
                    return new CreateDisplayDepartmentDto(d.getId(), d.getName());
                });
    }

    @Transactional
    @Override
    public boolean delete(Long departmentId) {

        if (!departmentRepository.existsById(departmentId)) return false;

        departmentRepository.deleteById(departmentId);
        return true;

    }
}
