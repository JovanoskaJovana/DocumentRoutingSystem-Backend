package mk.ukim.finki.routingsystem.model;

import jakarta.persistence.*;
import lombok.Data;
import mk.ukim.finki.routingsystem.model.enumerations.ActionType;
import mk.ukim.finki.routingsystem.model.enumerations.DocumentStatus;

import java.time.LocalDateTime;

@Entity
@Data
public class DocumentAction {

    @Id
    @GeneratedValue (strategy = GenerationType.SEQUENCE, generator = "seq_gen")
    private Long id;

    @ManyToOne (fetch = FetchType.LAZY)
    private Document document;

    @ManyToOne (fetch = FetchType.LAZY)
    private Employee perfromedByEmployee;

    @Enumerated(EnumType.STRING)
    private ActionType performedAction;

    @Enumerated(EnumType.STRING)
    private DocumentStatus fromStatus;

    @Enumerated(EnumType.STRING)
    private DocumentStatus toStatus;

    private String notes;

    private LocalDateTime actionDateTime;

    public DocumentAction() {
    }

    public DocumentAction(Document document, Employee perfromedByEmployee, ActionType performedAction, DocumentStatus fromStatus, DocumentStatus toStatus, String notes, LocalDateTime actionDateTime) {
        this.document = document;
        this.perfromedByEmployee = perfromedByEmployee;
        this.performedAction = performedAction;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.notes = notes;
        this.actionDateTime = actionDateTime;
    }
}
