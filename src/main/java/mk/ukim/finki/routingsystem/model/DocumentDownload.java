package mk.ukim.finki.routingsystem.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;


@Entity
@Data
public class DocumentDownload {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne (fetch = FetchType.LAZY)
    private Document document;

    @ManyToOne (fetch = FetchType.LAZY)
    private DocumentVersion documentVersion;

    @ManyToOne (fetch = FetchType.LAZY)
    private Employee employee;

    @org.hibernate.annotations.CreationTimestamp
    private LocalDateTime downloadDateTime;

    public DocumentDownload() {
    }

    public DocumentDownload(Document document, DocumentVersion documentVersion, Employee employee, LocalDateTime downloadDateTime) {
        this.document = document;
        this.documentVersion = documentVersion;
        this.employee = employee;
        this.downloadDateTime = downloadDateTime;
    }
}
