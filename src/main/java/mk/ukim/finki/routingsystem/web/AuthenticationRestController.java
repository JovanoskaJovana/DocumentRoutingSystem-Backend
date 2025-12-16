package mk.ukim.finki.routingsystem.web;

import mk.ukim.finki.routingsystem.model.Employee;
import mk.ukim.finki.routingsystem.model.dto.Employee.LoginRequestDto;
import mk.ukim.finki.routingsystem.model.dto.Employee.LoginResponseDto;
import mk.ukim.finki.routingsystem.repository.EmployeeRepository;
import mk.ukim.finki.routingsystem.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationRestController {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthenticationRestController(EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }


    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequestDto) {

        Employee employee = employeeRepository.findByEmail(loginRequestDto.email())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(loginRequestDto.password(), employee.getPasswordHash())) {
            return ResponseEntity.status(401).build();
        }

        Long departmentId = employee.getDepartment() != null ? employee.getDepartment().getId() : null;

        String token = jwtUtil.generateToken(
                employee.getId(),
                employee.getFirstName(),
                employee.getRole(),
                employee.getType(),
                departmentId
        );
        return ResponseEntity.ok(new LoginResponseDto(token));

    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.ok().build();
    }
}
