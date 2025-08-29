package mk.ukim.finki.routingsystem.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class Department {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    public Department() {
    }

    public Department(String name) {
        this.name = name;
    }
}
