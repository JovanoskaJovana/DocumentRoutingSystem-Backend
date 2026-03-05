package mk.ukim.finki.routingsystem.service;

import mk.ukim.finki.routingsystem.model.dto.Company.CreateCompanyDto;
import mk.ukim.finki.routingsystem.model.dto.Company.ResponseCompanyDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface CompanyService {

    ResponseCompanyDto findCompanyById(Long companyId);

    Page<ResponseCompanyDto> listAll(Pageable pageable);

    ResponseCompanyDto save(CreateCompanyDto createCompanyDto);

    ResponseCompanyDto changeActivity(Long companyId);

}
