package mk.ukim.finki.routingsystem.service.implementations;

import mk.ukim.finki.routingsystem.model.Company;
import mk.ukim.finki.routingsystem.model.dto.Company.CreateCompanyDto;
import mk.ukim.finki.routingsystem.model.dto.Company.ResponseCompanyDto;
import mk.ukim.finki.routingsystem.model.exceptions.CompanyAlreadyExistsException;
import mk.ukim.finki.routingsystem.model.exceptions.CompanyNotFoundException;
import mk.ukim.finki.routingsystem.repository.CompanyRepository;
import mk.ukim.finki.routingsystem.service.CompanyService;
import mk.ukim.finki.routingsystem.service.mappers.CompanyMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;

    public CompanyServiceImpl(CompanyRepository companyRepository, CompanyMapper companyMapper) {
        this.companyRepository = companyRepository;
        this.companyMapper = companyMapper;
    }

    @Override
    public ResponseCompanyDto findCompanyById(Long companyId) {
        return companyMapper.toDto(companyRepository.findById(companyId).orElseThrow(() -> new CompanyNotFoundException("Company not found")));
    }

    @Override
    public Page<ResponseCompanyDto> listAll(Pageable pageable) {
        return companyRepository.findAll(pageable).map(companyMapper::toDto);
    }

    @Override
    public ResponseCompanyDto save(CreateCompanyDto createCompanyDto) {

        String code = createCompanyDto.code().trim().toLowerCase();

        if (companyRepository.existsByCode(code)) {
            throw new CompanyAlreadyExistsException("Company already exists");
        }

        Company company = new Company();
        company.setName(createCompanyDto.name());
        company.setCode(createCompanyDto.code());
        company.setActive(true);

        companyRepository.save(company);

        return companyMapper.toDto(company);
    }

    @Override
    public ResponseCompanyDto changeActivity(Long companyId) {

        Company company = companyRepository.findById(companyId).orElseThrow(() -> new CompanyNotFoundException("Company not found"));

        company.setActive(Boolean.FALSE.equals(company.getActive()));

        companyRepository.save(company);

        return companyMapper.toDto(company);
    }
}
