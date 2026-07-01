package tn.esprit.espritconnect2.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.espritconnect2.exception.BusinessRuleException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ForumMediaService {

    private static final Set<String> IMAGE_TYPES = Set.of("image/jpeg", "image/jpg", "image/png", "image/webp");
    private static final Set<String> VIDEO_TYPES = Set.of("video/mp4", "video/webm");
    private static final Set<String> PDF_TYPES = Set.of("application/pdf");
    private static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024;
    private static final long MAX_VIDEO_BYTES = 50L * 1024 * 1024;
    private static final long MAX_PDF_BYTES = 10L * 1024 * 1024;

    @Value("${app.upload.root}")
    private String uploadRoot;

    @Value("${server.servlet.context-path:/}")
    private String contextPath;

    public String uploadImage(MultipartFile file) {
        validateFile(file, "image", IMAGE_TYPES, MAX_IMAGE_BYTES);
        return storeFile(file, "images");
    }

    public String uploadVideo(MultipartFile file) {
        validateFile(file, "video", VIDEO_TYPES, MAX_VIDEO_BYTES);
        return storeFile(file, "videos");
    }

    public String uploadPdf(MultipartFile file) {
        validateFile(file, "pdf", PDF_TYPES, MAX_PDF_BYTES);
        return storeFile(file, "pdfs");
    }

    public String extractPdfText(MultipartFile file) {
        validateFile(file, "pdf", PDF_TYPES, MAX_PDF_BYTES);
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document).trim();
            if (!StringUtils.hasText(text)) {
                throw new BusinessRuleException("No readable text found in the PDF document.");
            }
            return text.length() > 10000 ? text.substring(0, 10000) : text;
        } catch (IOException e) {
            log.warn("PDF extraction failed: {}", e.getMessage());
            throw new BusinessRuleException("Unable to read the PDF file.");
        }
    }

    public int countPdfPages(MultipartFile file) {
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            return document.getNumberOfPages();
        } catch (IOException e) {
            return 0;
        }
    }

    private void validateFile(MultipartFile file, String kind, Set<String> allowedTypes, long maxBytes) {
        if (file == null || file.isEmpty()) {
            throw new BusinessRuleException("A " + kind + " file is required.");
        }
        String contentType = file.getContentType() != null ? file.getContentType().toLowerCase(Locale.ROOT) : "";
        if (!allowedTypes.contains(contentType)) {
            throw new BusinessRuleException("Invalid " + kind + " type. Allowed: " + String.join(", ", allowedTypes));
        }
        if (file.getSize() > maxBytes) {
            throw new BusinessRuleException(kind.substring(0, 1).toUpperCase(Locale.ROOT) + kind.substring(1)
                    + " exceeds maximum size (" + (maxBytes / 1024 / 1024) + " MB).");
        }
    }

    private String storeFile(MultipartFile file, String subFolder) {
        try {
            Path uploadPath = Path.of(uploadRoot, "forum", subFolder).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
            String extension = original.contains(".")
                    ? original.substring(original.lastIndexOf('.')).toLowerCase(Locale.ROOT)
                    : "";
            String fileName = UUID.randomUUID() + extension;

            file.transferTo(uploadPath.resolve(fileName));
            // Return URL with context path for proper resolution
            String contextPrefix = contextPath.startsWith("/") ? contextPath : "/" + contextPath;
            return contextPrefix + "/uploads/forum/" + subFolder + "/" + fileName;
        } catch (IOException ex) {
            throw new BusinessRuleException("Unable to store the uploaded file.");
        }
    }
}
