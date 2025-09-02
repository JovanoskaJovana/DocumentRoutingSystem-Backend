package mk.ukim.finki.routingsystem.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class DocumentVersion {

    @Id
    @GeneratedValue (strategy = GenerationType.SEQUENCE, generator = "seq_gen")
    private Long id;

    @ManyToOne
    private Document document;

    private int version;

    private String fileName;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] fileData;

    private String changeNote;

    @ManyToOne
    private Employee uploadedByEmployee;

    private LocalDateTime uploadedDateTime;

    public DocumentVersion() {
    }

    public DocumentVersion(Document document, int version, String fileName, byte[] fileData, String changeNote, Employee uploadedByEmployee, LocalDateTime uploadedDateTime) {
        this.document = document;
        this.version = version;
        this.fileName = fileName;
        this.fileData = fileData;
        this.changeNote = changeNote;
        this.uploadedByEmployee = uploadedByEmployee;
        this.uploadedDateTime = uploadedDateTime;
    }
}
