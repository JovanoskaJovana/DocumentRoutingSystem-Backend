package mk.ukim.finki.routingsystem.web;

import mk.ukim.finki.routingsystem.model.dto.Employee.CreateDisplayEmployeeDto;
import mk.ukim.finki.routingsystem.security.EmployeePrincipal;
import mk.ukim.finki.routingsystem.service.DocumentActionService;
import mk.ukim.finki.routingsystem.service.EmployeeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeRestController {

    private final EmployeeService employeeService;

    public EmployeeRestController(EmployeeService employeeService, DocumentActionService documentActionService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public List<CreateDisplayEmployeeDto> findAll(@AuthenticationPrincipal EmployeePrincipal employeePrincipal){

        return employeeService.listAll(employeePrincipal.getCompanyId());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<CreateDisplayEmployeeDto> findById(@PathVariable Long id,
                                                             @AuthenticationPrincipal EmployeePrincipal employeePrincipal){

        return employeeService.findById(id, employeePrincipal.getCompanyId())
                .map(employee -> ResponseEntity.ok().body(employee))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<CreateDisplayEmployeeDto> save(@RequestBody CreateDisplayEmployeeDto createDisplayEmployeeDto,
                                                         @AuthenticationPrincipal EmployeePrincipal employeePrincipal){

        CreateDisplayEmployeeDto savedEmployee = employeeService.save(createDisplayEmployeeDto, employeePrincipal.getCompanyId());
        return ResponseEntity.status(HttpStatus.CREATED).body(savedEmployee);

    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<CreateDisplayEmployeeDto> update(@PathVariable Long id,
                                                           @RequestBody CreateDisplayEmployeeDto createDisplayEmployeeDto,
                                                           @AuthenticationPrincipal EmployeePrincipal employeePrincipal){

        return employeeService.update(id, createDisplayEmployeeDto, employeePrincipal.getCompanyId())
                .map(employee -> ResponseEntity.ok().body(employee))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @AuthenticationPrincipal EmployeePrincipal employeePrincipal){

        boolean deleted = employeeService.delete(id,employeePrincipal.getCompanyId());

        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();

    }

}
