package mk.ukim.finki.routingsystem.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import mk.ukim.finki.routingsystem.model.documentEntities.Document;
import mk.ukim.finki.routingsystem.model.enumerations.EmployeeType;
import mk.ukim.finki.routingsystem.model.enumerations.Role;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Getter
@Setter
public class Employee {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, length = 100)
    private String passwordHash;

    private String firstName;

    private String lastName;

    @ManyToOne (fetch = FetchType.LAZY)
    private Department department;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated (EnumType.STRING)
    @Column(nullable = false)
    private EmployeeType type;

    @ManyToMany(mappedBy = "routedToEmployees")
    private List <Document> documents = new ArrayList<>();

    public Employee() {
    }

    public Employee(String email, String passwordHash, String firstName, String lastName, Department department, Role role, EmployeeType type) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.department = department;
        this.role = role;
        this.type = type;
    }
}
