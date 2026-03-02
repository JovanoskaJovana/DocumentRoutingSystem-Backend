package mk.ukim.finki.routingsystem.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Department {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @ManyToOne
    private Company company;

    @Column(unique = true)
    private String departmentKey;

    public Department() {
    }

    public Department(String name, String departmentKey, Company company) {
        this.name = name;
        this.departmentKey = departmentKey;
        this.company = company;
    }
}
