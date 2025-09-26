package mk.ukim.finki.routingsystem.web;

import mk.ukim.finki.routingsystem.model.dto.CreateDisplayEmployeeDto;
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
    public List<CreateDisplayEmployeeDto> findAll(){

        return employeeService.listAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CreateDisplayEmployeeDto> findById(@PathVariable Long id){

        return employeeService.findById(id)
                .map(employee -> ResponseEntity.ok().body(employee))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CreateDisplayEmployeeDto> save(@RequestBody CreateDisplayEmployeeDto createDisplayEmployeeDto){

        CreateDisplayEmployeeDto savedEmployee = employeeService.save(createDisplayEmployeeDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedEmployee);

    }

    @PutMapping("/{id}")
    public ResponseEntity<CreateDisplayEmployeeDto> update(@PathVariable Long id,
                                                           @RequestBody CreateDisplayEmployeeDto createDisplayEmployeeDto){

        return employeeService.update(id, createDisplayEmployeeDto)
                .map(employee -> ResponseEntity.ok().body(employee))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){

        boolean deleted = employeeService.delete(id);

        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();

    }

}
