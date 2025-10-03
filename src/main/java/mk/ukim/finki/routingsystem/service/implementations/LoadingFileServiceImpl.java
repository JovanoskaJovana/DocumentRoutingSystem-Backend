package mk.ukim.finki.routingsystem.service.implementations;

import mk.ukim.finki.routingsystem.model.documentEntities.Document;
import mk.ukim.finki.routingsystem.model.documentEntities.DocumentVersion;
import mk.ukim.finki.routingsystem.model.dto.DocumentDownload.FileResource;
import mk.ukim.finki.routingsystem.model.exceptions.DocumentNotFoundException;
import mk.ukim.finki.routingsystem.model.exceptions.DocumentVersionNotFoundException;
import mk.ukim.finki.routingsystem.repository.DocumentRepository;
import mk.ukim.finki.routingsystem.repository.DocumentVersionRepository;
import mk.ukim.finki.routingsystem.service.LoadingFileService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

@Service
public class LoadingFileServiceImpl implements LoadingFileService {


    private final DocumentRepository documentRepository;
    private final DocumentVersionRepository documentVersionRepository;

    public LoadingFileServiceImpl(DocumentRepository documentRepository, DocumentVersionRepository documentVersionRepository) {
        this.documentRepository = documentRepository;
        this.documentVersionRepository = documentVersionRepository;
    }

    @Override
    public FileResource loadFile(Long documentId, Long versionId) {

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found"));

        DocumentVersion documentVersion = documentVersionRepository.findById(versionId)
                .orElseThrow(() -> new DocumentVersionNotFoundException("Document version not found"));

        if (!documentVersion.getDocument().equals(document)) {
            throw new DocumentVersionNotFoundException("Document version doesn't match");
        }

        byte[] bytes = documentVersion.getFileData();

        if (bytes == null || bytes.length == 0) {
            throw new DocumentVersionNotFoundException("Document version doesn't contain any data");
        }

        boolean hasName = documentVersion.getFileName() != null && !documentVersion.getFileName().isBlank();

        String base;

        if (hasName) {
            base = documentVersion.getFileName();
        } else if (documentVersion.getDocument().getTitle() != null){
            base = documentVersion.getDocument().getTitle();
        } else {
            base = "document - " + documentVersion.getDocument().getId();
        }

        String fileName = base.toLowerCase().endsWith(".pdf") ? base : base + ".pdf";

        return new FileResource(new ByteArrayResource(bytes), fileName, MediaType.APPLICATION_PDF, bytes.length);
    }
}
