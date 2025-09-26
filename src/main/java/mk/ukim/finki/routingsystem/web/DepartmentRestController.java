package mk.ukim.finki.routingsystem.web;

import mk.ukim.finki.routingsystem.model.dto.CreateDisplayDepartmentDto;
import mk.ukim.finki.routingsystem.service.DepartmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public List<CreateDisplayDepartmentDto> listAll() {
        return departmentService.listAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CreateDisplayDepartmentDto> findById(@PathVariable Long id) {

        return departmentService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());

    }

    @PostMapping
    public ResponseEntity<CreateDisplayDepartmentDto> save(@RequestBody CreateDisplayDepartmentDto createDisplayDepartmentDto) {

        CreateDisplayDepartmentDto saved = departmentService.save(createDisplayDepartmentDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CreateDisplayDepartmentDto> update(@PathVariable Long id , @RequestBody CreateDisplayDepartmentDto createDisplayDepartmentDto) {

        return departmentService.update(id, createDisplayDepartmentDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        boolean deleted = departmentService.delete(id);

        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();

    }








}
