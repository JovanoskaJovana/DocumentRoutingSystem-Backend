package mk.ukim.finki.routingsystem.service.implementations;

import mk.ukim.finki.routingsystem.model.documentEntities.DocumentVersion;
import mk.ukim.finki.routingsystem.model.dto.DocumentDownload.FileResource;
import mk.ukim.finki.routingsystem.model.exceptions.DocumentVersionNotFoundException;
import mk.ukim.finki.routingsystem.repository.DocumentVersionRepository;
import mk.ukim.finki.routingsystem.service.LoadingFileService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoadingFileServiceImpl implements LoadingFileService {


    private final DocumentVersionRepository documentVersionRepository;

    public LoadingFileServiceImpl(DocumentVersionRepository documentVersionRepository) {
        this.documentVersionRepository = documentVersionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public FileResource loadFile(Long documentId, Long versionId) {


        DocumentVersion documentVersion = documentVersionRepository.findById(versionId)
                .orElseThrow(() -> new DocumentVersionNotFoundException("Document version not found"));

        if (!documentVersion.getDocument().getId().equals(documentId)) {
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
