package tn.esprit.espritconnect2.Service.emailBackOffice;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DigestBannerStorageService {

    @Value("${app.uploads.dir:uploads}")
    private String uploadsDir;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    public String storeAndGetPublicUrl(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Fichier vide");
        }
        String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "banner" : file.getOriginalFilename());
        String ext = original.contains(".") ? original.substring(original.lastIndexOf(".")) : ".png";
        String name = "digest-banner-" + UUID.randomUUID() + ext;

        try {
            Path dir = Paths.get(uploadsDir, "digest");
            Files.createDirectories(dir);

            Path target = dir.resolve(name);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            // URL publique : /espritconnect/uploads/digest/<name>
            // (on expose /uploads/** dans WebConfig)
            String base = (contextPath == null ? "" : contextPath);
            return base + "/uploads/digest/" + name;

        } catch (IOException e) {
            throw new RuntimeException("Erreur stockage bannière: " + e.getMessage(), e);
        }
    }
}