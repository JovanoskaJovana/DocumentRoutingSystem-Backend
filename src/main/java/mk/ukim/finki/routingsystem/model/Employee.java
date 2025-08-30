package mk.ukim.finki.routingsystem.model;

import jakarta.persistence.*;
import lombok.Data;
import mk.ukim.finki.routingsystem.model.enumerations.EmployeeType;
import mk.ukim.finki.routingsystem.model.enumerations.Role;

@Entity
@Data
public class Employee {

    @Id
    @GeneratedValue (strategy = GenerationType.SEQUENCE, generator = "seq_gen")
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String firstName;

    private String lastName;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name="department_id")
    private Department department;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated (EnumType.STRING)
    private EmployeeType type;

    public Employee() {
    }

    public Employee(String email, String password, String firstName, String lastName, Department department, Role role, EmployeeType type) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.department = department;
        this.role = role;
        this.type = type;
    }
}
