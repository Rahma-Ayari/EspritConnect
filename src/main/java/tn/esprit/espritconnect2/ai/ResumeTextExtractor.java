package tn.esprit.espritconnect2.ai;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Extracts plain text from uploaded resume files (PDF, DOCX, DOC, TXT).
 */
@Component
@Slf4j
public class ResumeTextExtractor {

    public String extract(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No file provided");
        }
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase(Locale.ROOT);
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);

        try (InputStream in = file.getInputStream()) {
            String text;
            if (name.endsWith(".pdf") || contentType.contains("pdf")) {
                text = extractPdf(in);
            } else if (name.endsWith(".docx") || contentType.contains("wordprocessingml")) {
                text = extractDocx(in);
            } else if (name.endsWith(".txt") || contentType.startsWith("text/")) {
                text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            } else {
                throw new IllegalArgumentException("Unsupported file type. Upload PDF, DOCX, or TXT.");
            }
            String cleaned = clean(text);
            if (cleaned.isBlank()) {
                throw new IllegalArgumentException(
                        "Could not read text from this file. It may be a scanned image. Paste the text manually instead.");
            }
            return cleaned;
        } catch (IOException e) {
            log.error("Resume extraction failed for {}: {}", name, e.getMessage());
            throw new IllegalArgumentException("Failed to read the file: " + e.getMessage());
        }
    }

    private String extractPdf(InputStream in) throws IOException {
        try (PDDocument document = PDDocument.load(in)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return stripper.getText(document);
        }
    }

    private String extractDocx(InputStream in) throws IOException {
        try (XWPFDocument document = new XWPFDocument(in);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        }
    }

    private String clean(String raw) {
        if (raw == null) return "";
        return raw
                .replaceAll("\\r\\n", "\n")
                .replaceAll("[\\t ]+", " ")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }
}
