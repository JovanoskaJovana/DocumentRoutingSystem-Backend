package mk.ukim.finki.routingsystem.model.documentEntities;

import jakarta.persistence.*;
import lombok.Data;
import mk.ukim.finki.routingsystem.model.Company;
import mk.ukim.finki.routingsystem.model.Department;
import mk.ukim.finki.routingsystem.model.Employee;
import mk.ukim.finki.routingsystem.model.enumerations.DocumentStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
public class Document {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String title;

    @ManyToOne
    private Company company;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn (nullable = false)
    private Employee uploadedByEmployee;

    @org.hibernate.annotations.CreationTimestamp
    private LocalDateTime uploadDateTime;

    @ManyToOne (fetch = FetchType.LAZY)
    private Department routedToDepartment;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "document_routing",
            joinColumns = @JoinColumn(name = "document_id"),
            inverseJoinColumns = @JoinColumn(name = "employee_id")
    )
    private Set<Employee> routedToEmployees = new HashSet<>();

    @OneToMany (mappedBy = "document", fetch = FetchType.LAZY, cascade=CascadeType.ALL, orphanRemoval=true)
    @OrderBy("versionNumber DESC")
    private List<DocumentVersion> allDocumentVersions = new ArrayList<>();

    @OneToOne (fetch = FetchType.LAZY)
    @JoinColumn
    private DocumentVersion currentDocumentVersion;

    @Enumerated (EnumType.STRING)
    private DocumentStatus documentStatus;

    @ElementCollection
    private List<String> suggestedDepartments;

    public Document() {
    }

    public Document(String title, Employee uploadedByEmployee, LocalDateTime uploadDateTime, Department routedToDepartment, Set<Employee> routedToEmployees, List<DocumentVersion> allDocumentVersions, DocumentVersion currentDocumentVersion, DocumentStatus documentStatus) {
        this.title = title;
        this.uploadedByEmployee = uploadedByEmployee;
        this.uploadDateTime = uploadDateTime;
        this.routedToDepartment = routedToDepartment;
        this.routedToEmployees = routedToEmployees;
        this.allDocumentVersions = allDocumentVersions;
        this.currentDocumentVersion = currentDocumentVersion;
        this.documentStatus = documentStatus;
    }
}
