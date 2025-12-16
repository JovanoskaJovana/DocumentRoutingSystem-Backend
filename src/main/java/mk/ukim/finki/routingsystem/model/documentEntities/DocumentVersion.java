package mk.ukim.finki.routingsystem.model.documentEntities;

import jakarta.persistence.*;
import lombok.Data;
import mk.ukim.finki.routingsystem.model.Employee;
import mk.ukim.finki.routingsystem.model.documentEntities.Document;

import java.time.LocalDateTime;

@Entity
@Data
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "document_id", "version_number" }) })
public class DocumentVersion {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn (nullable = false)
    private Document document;

    private int versionNumber;

    private String fileName;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column (nullable = false)
    private byte[] fileData;

    private String changeNote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (nullable = false)
    private Employee uploadedByEmployee;

    private LocalDateTime uploadedDateTime;

    public DocumentVersion() {
    }

    public DocumentVersion(Document document, int versionNumber, String fileName, byte[] fileData, String changeNote, Employee uploadedByEmployee, LocalDateTime uploadedDateTime) {
        this.document = document;
        this.versionNumber = versionNumber;
        this.fileName = fileName;
        this.fileData = fileData;
        this.changeNote = changeNote;
        this.uploadedByEmployee = uploadedByEmployee;
        this.uploadedDateTime = uploadedDateTime;
    }
}
