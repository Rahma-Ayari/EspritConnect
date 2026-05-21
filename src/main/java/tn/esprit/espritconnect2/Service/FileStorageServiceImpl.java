package tn.esprit.espritconnect2.Service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.espritconnect2.Exception.BusinessRuleException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
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

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
