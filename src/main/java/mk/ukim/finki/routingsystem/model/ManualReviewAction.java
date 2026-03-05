package mk.ukim.finki.routingsystem.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class ManualReviewAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Company company;

    @ManyToOne
    private Department manualChosenDepartment;

    private String documentTitle;

    private String documentText;

    private LocalDateTime timestamp;


    public ManualReviewAction() {
    }

    public ManualReviewAction(Company company, Department manualChosenDepartment, String documentTitle, String documentText, LocalDateTime timestamp) {
        this.company = company;
        this.manualChosenDepartment = manualChosenDepartment;
        this.documentTitle = documentTitle;
        this.documentText = documentText;
        this.timestamp = timestamp;
    }
}
