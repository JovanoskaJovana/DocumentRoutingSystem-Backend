package mk.ukim.finki.routingsystem.web;

import mk.ukim.finki.routingsystem.model.dto.CreateDisplayDepartmentDto;
import mk.ukim.finki.routingsystem.security.EmployeePrincipal;
import mk.ukim.finki.routingsystem.service.DepartmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping ("api/departments")
public class DepartmentRestController {

    private final DepartmentService departmentService;

    public DepartmentRestController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    public List<CreateDisplayDepartmentDto> listAll(@AuthenticationPrincipal EmployeePrincipal employeePrincipal) {
        return departmentService.listAll(employeePrincipal.getCompanyId());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<CreateDisplayDepartmentDto> findById(@PathVariable Long id,
                                                               @AuthenticationPrincipal EmployeePrincipal employeePrincipal) {

        return departmentService.findById(id, employeePrincipal.getCompanyId())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());

    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<CreateDisplayDepartmentDto> save(@RequestBody CreateDisplayDepartmentDto createDisplayDepartmentDto,
                                                           @AuthenticationPrincipal EmployeePrincipal employeePrincipal) {

        CreateDisplayDepartmentDto saved = departmentService.save(createDisplayDepartmentDto, employeePrincipal.getCompanyId());

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<CreateDisplayDepartmentDto> update(@PathVariable Long id,
                                                             @RequestBody CreateDisplayDepartmentDto createDisplayDepartmentDto,
                                                             @AuthenticationPrincipal EmployeePrincipal employeePrincipal) {

        return departmentService.update(employeePrincipal.getCompanyId(), id, createDisplayDepartmentDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());

    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @AuthenticationPrincipal EmployeePrincipal employeePrincipal) {

        boolean deleted = departmentService.delete(employeePrincipal.getCompanyId(), id);

        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();

    }








}
