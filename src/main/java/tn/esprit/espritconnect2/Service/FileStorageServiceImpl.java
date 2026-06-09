package tn.esprit.espritconnect2.Service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.espritconnect2.exception.BusinessRuleException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageServiceImpl implements IFileStorageService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    private static final String VERIFICATION_DOCS_FOLDER = "verification-documents";
    private static final List<String> ALLOWED_TYPES = Arrays.asList(
            "application/pdf",
            "image/jpeg",
            "image/jpg",
            "image/png"
    );
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("pdf", "jpg", "jpeg", "png");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 Mo

    private Path verificationDocsPath;

    @PostConstruct
    public void init() {
        try {
            verificationDocsPath = Paths.get(uploadDir, VERIFICATION_DOCS_FOLDER).toAbsolutePath().normalize();
            Files.createDirectories(verificationDocsPath);
            log.info("Dossier de stockage des documents créé: {}", verificationDocsPath);
        } catch (IOException e) {
            throw new RuntimeException("Impossible de créer le dossier de stockage des documents", e);
        }
    }

    @Override
    public String storeVerificationDocument(MultipartFile file, String userId) {
        if (!isValidDocumentType(file)) {
            throw new BusinessRuleException("Type de fichier non autorisé. Types acceptés: PDF, JPG, PNG");
        }
        if (!isValidDocumentSize(file)) {
            throw new BusinessRuleException("Le fichier dépasse la taille maximale autorisée (10 Mo)");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = getFileExtension(originalFilename);
        String newFilename = userId + "_" + UUID.randomUUID().toString().substring(0, 8) + "." + fileExtension;

        try {
            Path targetLocation = verificationDocsPath.resolve(newFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            log.info("Document de vérification enregistré: {}", newFilename);
            return targetLocation.toString();
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de l'enregistrement du document: " + originalFilename, e);
        }
    }

    @Override
    public Resource loadVerificationDocument(String filePath) {
        try {
            Path path = Paths.get(filePath).toAbsolutePath().normalize();
            Resource resource = new UrlResource(path.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new BusinessRuleException("Document non trouvé: " + filePath);
            }
        } catch (MalformedURLException e) {
            throw new BusinessRuleException("Document non trouvé: " + filePath);
        }
    }

    @Override
    public void deleteVerificationDocument(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return;
        }
        
        try {
            Path path = Paths.get(filePath).toAbsolutePath().normalize();
            if (Files.exists(path)) {
                Files.delete(path);
                log.info("Document de vérification supprimé: {}", filePath);
            }
        } catch (IOException e) {
            log.error("Erreur lors de la suppression du document: {}", filePath, e);
        }
    }

    @Override
    public boolean isValidDocumentType(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();
        String extension = getFileExtension(filename).toLowerCase();
        
        return ALLOWED_TYPES.contains(contentType) && ALLOWED_EXTENSIONS.contains(extension);
    }

    @Override
    public boolean isValidDocumentSize(MultipartFile file) {
        return file != null && file.getSize() <= MAX_FILE_SIZE;
    }

    @Override
    public String storeBase64Document(String base64Data, String userId) {
        if (base64Data == null || base64Data.trim().isEmpty()) {
            throw new BusinessRuleException("Données du document vides");
        }

        try {
            // Extraire le type MIME et les données base64
            String mimeType = "application/octet-stream";
            String base64Content = base64Data;
            
            if (base64Data.contains(",")) {
                // Format: data:image/png;base64,iVBORw0KGgo...
                String[] parts = base64Data.split(",");
                if (parts.length == 2) {
                    String header = parts[0]; // data:image/png;base64
                    base64Content = parts[1];
                    
                    if (header.contains(":") && header.contains(";")) {
                        mimeType = header.substring(header.indexOf(":") + 1, header.indexOf(";"));
                    }
                }
            }

            // Déterminer l'extension du fichier
            String extension = getExtensionFromMimeType(mimeType);
            if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
                throw new BusinessRuleException("Type de fichier non autorisé. Types acceptés: PDF, JPG, PNG");
            }

            // Décoder le base64
            byte[] decodedBytes = Base64.getDecoder().decode(base64Content);
            
            // Vérifier la taille
            if (decodedBytes.length > MAX_FILE_SIZE) {
                throw new BusinessRuleException("Le fichier dépasse la taille maximale autorisée (10 Mo)");
            }

            // Créer le nom de fichier
            String newFilename = userId + "_" + UUID.randomUUID().toString().substring(0, 8) + "." + extension;
            Path targetLocation = verificationDocsPath.resolve(newFilename);

            // Écrire le fichier
            Files.write(targetLocation, decodedBytes);
            log.info("Document base64 enregistré: {}", newFilename);
            
            return targetLocation.toString();
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException("Format base64 invalide: " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de l'enregistrement du document", e);
        }
    }

    private String getExtensionFromMimeType(String mimeType) {
        return switch (mimeType.toLowerCase()) {
            case "application/pdf" -> "pdf";
            case "image/jpeg", "image/jpg" -> "jpg";
            case "image/png" -> "png";
            default -> "bin";
        };
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
