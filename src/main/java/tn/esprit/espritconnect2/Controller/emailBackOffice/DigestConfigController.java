package tn.esprit.espritconnect2.Controller.emailBackOffice;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.espritconnect2.DTO.emailBackOffice.DigestConfigRequestDTO;
import tn.esprit.espritconnect2.DTO.emailBackOffice.DigestConfigResponseDTO;
import tn.esprit.espritconnect2.Service.emailBackOffice.DigestConfigServiceImpl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/digest-config")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DigestConfigController {

    private final DigestConfigServiceImpl service;

    @Value("${app.upload.root}")
    private String uploadRoot;

    @GetMapping
    public ResponseEntity<DigestConfigResponseDTO> getConfig() {
        return ResponseEntity.ok(service.getConfig());
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DigestConfigResponseDTO> updateConfig(@RequestBody DigestConfigRequestDTO dto) {
        return ResponseEntity.ok(service.updateConfig(dto));
    }

    @PostMapping("/reset")
    public ResponseEntity<DigestConfigResponseDTO> reset() {
        return ResponseEntity.ok(service.resetToDefault());
    }

    @DeleteMapping("/clear")
    public ResponseEntity<DigestConfigResponseDTO> clear() {
        return ResponseEntity.ok(service.clearTemplate());
    }

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, String>> uploadBanner(
            @RequestParam("file") MultipartFile file
    ) throws IOException {

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Fichier vide"));
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Le fichier doit etre une image"));
        }

        Path bannersDir = Paths.get(uploadRoot)
                .resolve("banners")
                .toAbsolutePath()
                .normalize();

        Files.createDirectories(bannersDir);

        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String ext = "";
        int dot = original.lastIndexOf('.');
        if (dot >= 0 && dot < original.length() - 1) {
            ext = original.substring(dot).toLowerCase(Locale.ROOT);
        }

        if (!ext.matches("\\.(png|jpg|jpeg|gif|webp)")) {
            if (contentType.contains("jpeg") || contentType.contains("jpg")) {
                ext = ".jpg";
            } else if (contentType.contains("webp")) {
                ext = ".webp";
            } else if (contentType.contains("gif")) {
                ext = ".gif";
            } else {
                ext = ".png";
            }
        }

        String filename = UUID.randomUUID() + ext;
        Path target = bannersDir.resolve(filename).normalize();

        try (var inputStream = file.getInputStream()) {
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        }

        String url = "http://localhost:8088/espritconnect/uploads/banners/" + filename;
        return ResponseEntity.ok(Map.of("url", url));
    }
}