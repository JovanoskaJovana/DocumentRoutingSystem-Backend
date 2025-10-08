package mk.ukim.finki.routingsystem.model.documentEntities;

import jakarta.persistence.*;
import lombok.Data;
import mk.ukim.finki.routingsystem.model.Employee;
import mk.ukim.finki.routingsystem.model.enumerations.ActionType;
import mk.ukim.finki.routingsystem.model.enumerations.DocumentStatus;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
public class DocumentAction {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(nullable=false)
    private Document document;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(nullable=false)
    private DocumentVersion documentVersion;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(nullable=false)
    private Employee performedByEmployee;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private ActionType performedAction;

    @Enumerated(EnumType.STRING)
    private DocumentStatus fromStatus;

    @Enumerated(EnumType.STRING)
    private DocumentStatus toStatus;

    private String notes;

    @CreationTimestamp
    private LocalDateTime actionDateTime;

    public DocumentAction() {
    }

    public DocumentAction(Document document, DocumentVersion documentVersion, Employee performedByEmployee, ActionType performedAction, DocumentStatus fromStatus, DocumentStatus toStatus, String notes, LocalDateTime actionDateTime) {
        this.document = document;
        this.documentVersion = documentVersion;
        this.performedByEmployee = performedByEmployee;
        this.performedAction = performedAction;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.notes = notes;
        this.actionDateTime = actionDateTime;
    }
}
