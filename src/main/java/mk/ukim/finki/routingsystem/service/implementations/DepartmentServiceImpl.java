package mk.ukim.finki.routingsystem.service.implementations;

import mk.ukim.finki.routingsystem.model.Company;
import mk.ukim.finki.routingsystem.model.Department;
import mk.ukim.finki.routingsystem.model.documentEntities.Document;
import mk.ukim.finki.routingsystem.model.dto.CreateDisplayDepartmentDto;
import mk.ukim.finki.routingsystem.model.exceptions.CompanyNotFoundException;
import mk.ukim.finki.routingsystem.model.exceptions.DepartmentNotFoundException;
import mk.ukim.finki.routingsystem.model.exceptions.DocumentNotFoundException;
import mk.ukim.finki.routingsystem.repository.CompanyRepository;
import mk.ukim.finki.routingsystem.repository.DepartmentRepository;
import mk.ukim.finki.routingsystem.repository.DocumentRepository;
import mk.ukim.finki.routingsystem.service.DepartmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class DepartmentServiceImpl implements DepartmentService {


    private final DepartmentRepository departmentRepository;
    private final DocumentRepository documentRepository;
    private final CompanyRepository companyRepository;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository, DocumentRepository documentRepository, CompanyRepository companyRepository) {
        this.departmentRepository = departmentRepository;
        this.documentRepository = documentRepository;
        this.companyRepository = companyRepository;
    }

    @Transactional(readOnly = true)
    @Override
    public List<CreateDisplayDepartmentDto> listAll(Long companyId) {

        return departmentRepository.findAllByCompany_Id(companyId)
                .stream()
                .map(d -> new CreateDisplayDepartmentDto(d.getId(), d.getName(), d.getDepartmentKey(), d.getCompany().getId()))
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<CreateDisplayDepartmentDto> findById(Long departmentId, Long companyId) {

        return departmentRepository.findByIdAndCompany_Id(departmentId, companyId)
                .map(d -> new CreateDisplayDepartmentDto(d.getId(), d.getName(), d.getDepartmentKey(), d.getCompany().getId()));
    }

    @Transactional
    @Override
    public CreateDisplayDepartmentDto save(CreateDisplayDepartmentDto createDisplayDepartmentDto, Long companyId) {

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException("Company not found"));

        Department savedDepartment = departmentRepository
                .save(new Department(createDisplayDepartmentDto.name(), createDisplayDepartmentDto.key(), company));

        return new CreateDisplayDepartmentDto(savedDepartment.getId(), savedDepartment.getName(), savedDepartment.getDepartmentKey(), savedDepartment.getCompany().getId());

    }

    @Transactional
    @Override
    public Optional<CreateDisplayDepartmentDto> update(Long companyId, Long departmentId, CreateDisplayDepartmentDto createDisplayDepartmentDto) {

        return departmentRepository.findByIdAndCompany_Id(departmentId, companyId)
                .map(d -> {
                    d.setName(createDisplayDepartmentDto.name());
                    d.setDepartmentKey(createDisplayDepartmentDto.key());
                    departmentRepository.save(d);
                    return new CreateDisplayDepartmentDto(d.getId(), d.getName(), d.getDepartmentKey(), companyId);
                });
    }

    @Override
    public List<CreateDisplayDepartmentDto> getManualReviewDepartments(Long documentId, Long companyId) {

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found"));

        if (document.getSuggestedDepartments() != null && !document.getSuggestedDepartments().isEmpty()) {
            return departmentRepository.findAllByDepartmentKeyInAndCompany_Id(document.getSuggestedDepartments(), companyId)
                    .stream()
                    .map(d -> new CreateDisplayDepartmentDto(d.getId(), d.getName(), d.getDepartmentKey(), d.getCompany().getId()))
                    .toList();
        } else {
            return departmentRepository.findAllByCompany_Id(companyId)
                    .stream()
                    .map(d -> new CreateDisplayDepartmentDto(d.getId(), d.getName(), d.getDepartmentKey(), d.getCompany().getId()))
                    .toList();
        }

    }

    @Transactional
    @Override
    public boolean delete(Long companyId, Long departmentId) {

        Department department = departmentRepository.findByIdAndCompany_Id(departmentId, companyId)
                .orElseThrow(() -> new DepartmentNotFoundException("Department not found"));

        departmentRepository.delete(department);
        return true;

    }
}
