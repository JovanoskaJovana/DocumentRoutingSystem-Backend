package mk.ukim.finki.routingsystem.model.dto.DocumentDownload;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;

public record FileResource(
        Resource resource,
        String filename,
        MediaType mediaType,
        int length
) {}
