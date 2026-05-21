package tn.esprit.espritconnect2.Service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface IFileStorageService {
    String storeVerificationDocument(MultipartFile file, String userId);
    Resource loadVerificationDocument(String filePath);
    void deleteVerificationDocument(String filePath);
    boolean isValidDocumentType(MultipartFile file);
    boolean isValidDocumentSize(MultipartFile file);
}
