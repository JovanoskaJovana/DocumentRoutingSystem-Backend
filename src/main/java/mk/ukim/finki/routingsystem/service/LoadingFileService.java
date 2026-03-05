package mk.ukim.finki.routingsystem.service;

import mk.ukim.finki.routingsystem.model.dto.DocumentDownload.FileResource;

public interface LoadingFileService {

    FileResource loadFile(Long documentId, Long versionId, Long companyId);

}
