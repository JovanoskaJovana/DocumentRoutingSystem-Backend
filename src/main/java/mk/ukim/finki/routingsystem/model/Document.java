package mk.ukim.finki.routingsystem.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
public class Document {

    @Id
    @GeneratedValue (strategy = GenerationType.SEQUENCE, generator = "seq_gen")
    private Long id;

    private String title;

    @ManyToOne (fetch = FetchType.LAZY)
    private Employee uploadedByEmployee;

    private LocalDateTime uploadDateTime;

    @ManyToOne (fetch = FetchType.LAZY)
    private Department routedToDepartment;

    @ManyToMany(fetch = FetchType.LAZY)
    private List<Employee> routedToEmployees;

    @ManyToOne
    private DocumentVersion currentDocumentVersion;

    public Document() {
    }

    public Document(String title, Employee uploadedByEmployee, LocalDateTime uploadDateTime, Department routedToDepartment, List<Employee> routedToEmployees, DocumentVersion currentDocumentVersion) {
        this.title = title;
        this.uploadedByEmployee = uploadedByEmployee;
        this.uploadDateTime = uploadDateTime;
        this.routedToDepartment = routedToDepartment;
        this.routedToEmployees = routedToEmployees;
        this.currentDocumentVersion = currentDocumentVersion;
    }
}
