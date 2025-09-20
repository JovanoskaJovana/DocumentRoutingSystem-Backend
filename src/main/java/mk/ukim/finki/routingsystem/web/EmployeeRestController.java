package mk.ukim.finki.routingsystem.web;

import mk.ukim.finki.routingsystem.model.dto.EmployeeDto;
import mk.ukim.finki.routingsystem.service.EmployeeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeRestController {

    private final EmployeeService employeeService;

    public EmployeeRestController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public List<EmployeeDto> findAll(){

        return employeeService.listAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDto> findById(@PathVariable Long id){

        return employeeService.findById(id)
                .map(employee -> ResponseEntity.ok().body(employee))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<EmployeeDto> save(@RequestBody EmployeeDto employeeDto){

        EmployeeDto savedEmployee = employeeService.save(employeeDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedEmployee);

    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeDto> update(@PathVariable Long id,
                                            @RequestBody EmployeeDto employeeDto){

        return employeeService.update(id, employeeDto)
                .map(employee -> ResponseEntity.ok().body(employee))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){

        boolean deleted = employeeService.delete(id);

        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();

    }

}
