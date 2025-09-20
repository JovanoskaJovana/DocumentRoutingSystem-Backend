package mk.ukim.finki.routingsystem.service.implementations;

import mk.ukim.finki.routingsystem.model.Department;
import mk.ukim.finki.routingsystem.model.dto.DepartmentDto;
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
    public List<DepartmentDto> listAll() {

        return departmentRepository.findAll()
                .stream()
                .map(d -> new DepartmentDto(d.getId(), d.getName()))
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<DepartmentDto> findById(Long departmentId) {

        return departmentRepository.findById(departmentId)
                .map(d -> new DepartmentDto(d.getId(), d.getName()));
    }

    @Transactional
    @Override
    public DepartmentDto save(DepartmentDto departmentDto) {

        Department savedDepartment = departmentRepository
                .save(new Department(departmentDto.name()));

        return new DepartmentDto(savedDepartment.getId(), savedDepartment.getName());

    }

    @Transactional
    @Override
    public Optional<DepartmentDto> update(Long departmentId, DepartmentDto departmentDto) {

        return departmentRepository.findById(departmentId)
                .map(d -> {
                    d.setName(departmentDto.name());
                    departmentRepository.save(d);
                    return new DepartmentDto(d.getId(), d.getName());
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
