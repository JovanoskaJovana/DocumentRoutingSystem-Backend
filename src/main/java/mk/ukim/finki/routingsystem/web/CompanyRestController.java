package mk.ukim.finki.routingsystem.web;

import mk.ukim.finki.routingsystem.model.dto.Company.CreateCompanyDto;
import mk.ukim.finki.routingsystem.model.dto.Company.ResponseCompanyDto;
import mk.ukim.finki.routingsystem.service.CompanyService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies")
public class CompanyRestController {

    private final CompanyService companyService;

    public CompanyRestController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseCompanyDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(companyService.findCompanyById(id));
    }

    @GetMapping
    public ResponseEntity<Page<ResponseCompanyDto>> findAll(Pageable pageable) {
        Page<ResponseCompanyDto> responseCompanyDtos = companyService.listAll(pageable);
        return ResponseEntity.ok(responseCompanyDtos);
    }

    @PreAuthorize("@companyAuth.canCreateCompany(authentication)")
    @PostMapping
    public ResponseEntity<ResponseCompanyDto> createCompany(@Validated @RequestBody CreateCompanyDto companyDto) {
        ResponseCompanyDto responseCompanyDto = companyService.save(companyDto);
        return ResponseEntity.ok(responseCompanyDto);
    }

    @PreAuthorize("@companyAuth.canChangeActivity(authentication)")
    @PutMapping("/{id}/activity")
    public ResponseEntity<ResponseCompanyDto> changeActivity(@PathVariable Long id) {
        ResponseCompanyDto responseCompanyDto = companyService.changeActivity(id);
        return ResponseEntity.ok(responseCompanyDto);
    }
}
