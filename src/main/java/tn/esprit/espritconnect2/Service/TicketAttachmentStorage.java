package tn.esprit.espritconnect2.Service;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.espritconnect2.Config.ApiOfficePaths;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class TicketAttachmentStorage {

    private final Path fileStorageLocation = Paths.get("uploads/tickets").toAbsolutePath().normalize();

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("Could not create ticket upload directory", ex);
        }
    }

    public Map<String, String> store(MultipartFile file) {
        String originalFileName = org.springframework.util.StringUtils.cleanPath(file.getOriginalFilename());
        if (originalFileName.contains("..")) {
            throw new RuntimeException("Invalid filename: " + originalFileName);
        }
        String extension = "";
        int dot = originalFileName.lastIndexOf('.');
        if (dot > 0) {
            extension = originalFileName.substring(dot);
        }
        String fileName = System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;
        try {
            Path target = fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            Map<String, String> response = new HashMap<>();
            response.put("fileName", fileName);
            response.put("url", "/api/front/support/files/" + fileName);
            response.put("legacyUrl", "/api/support/files/" + fileName);
            response.put("status", "success");
            return response;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + originalFileName, ex);
        }
    }

    public Resource loadAsResource(String filename, HttpServletRequest request) throws IOException {
        Path filePath = fileStorageLocation.resolve(filename).normalize();
        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists()) {
            return null;
        }
        return resource;
    }

    public MediaType resolveMediaType(Resource resource, HttpServletRequest request) throws IOException {
        String contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        if (contentType == null) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
        return MediaType.parseMediaType(contentType);
    }
}
