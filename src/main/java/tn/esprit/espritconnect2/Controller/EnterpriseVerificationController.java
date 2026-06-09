package tn.esprit.espritconnect2.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.espritconnect2.DTO.*;
import tn.esprit.espritconnect2.Entitie.Role;
import tn.esprit.espritconnect2.Entitie.User;
import tn.esprit.espritconnect2.Entitie.VerificationStatus;
import tn.esprit.espritconnect2.exception.BusinessRuleException;
import tn.esprit.espritconnect2.exception.NotFoundException;
import tn.esprit.espritconnect2.Repository.UserRepository;
import tn.esprit.espritconnect2.Service.IAutoVerificationService;
import tn.esprit.espritconnect2.Service.IFileStorageService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/enterprise-verification")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
public class EnterpriseVerificationController {

    private final UserRepository userRepository;
    private final IFileStorageService fileStorageService;
    private final IAutoVerificationService autoVerificationService;

    @PostMapping("/upload-document")
    @PreAuthorize("hasRole('ENTREPRISE')")
    public ResponseEntity<EnterpriseVerificationDTO> uploadVerificationDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "businessRegistrationNumber", required = false) String businessRegistrationNumber,
            @RequestParam(value = "companySector", required = false) String companySector,
            @RequestParam(value = "companyWebsite", required = false) String companyWebsite,
            @RequestParam(value = "companyDescription", required = false) String companyDescription,
            Authentication authentication) {
        
        User user = getCurrentUser(authentication);
        
        if (user.getRole() != Role.ENTREPRISE) {
            throw new BusinessRuleException("Seules les entreprises peuvent soumettre des documents de vérification");
        }

        if (user.getVerificationDocumentPath() != null) {
            fileStorageService.deleteVerificationDocument(user.getVerificationDocumentPath());
        }

        String filePath = fileStorageService.storeVerificationDocument(file, user.getId().toString());
        
        user.setVerificationDocumentPath(filePath);
        user.setVerificationDocumentName(file.getOriginalFilename());
        user.setVerificationStatus(VerificationStatus.PENDING);
        
        if (businessRegistrationNumber != null && !businessRegistrationNumber.trim().isEmpty()) {
            user.setBusinessRegistrationNumber(businessRegistrationNumber.trim());
        }
        if (companySector != null && !companySector.trim().isEmpty()) {
            user.setCompanySector(companySector.trim());
        }
        if (companyWebsite != null && !companyWebsite.trim().isEmpty()) {
            user.setCompanyWebsite(companyWebsite.trim());
        }
        if (companyDescription != null && !companyDescription.trim().isEmpty()) {
            user.setCompanyDescription(companyDescription.trim());
        }

        userRepository.save(user);
        
        autoVerificationService.performAutoVerification(user);
        
        log.info("Document de vérification uploadé pour l'entreprise: {}", user.getEmail());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(user));
    }

    @GetMapping("/my-status")
    @PreAuthorize("hasRole('ENTREPRISE')")
    public ResponseEntity<EnterpriseVerificationDTO> getMyVerificationStatus(Authentication authentication) {
        User user = getCurrentUser(authentication);
        
        // Synchronisation automatique si l'admin a accepté l'utilisateur via le panel User Management
        if (user.getStatus() == tn.esprit.espritconnect2.Entitie.Status.ACCEPTEE && 
            user.getVerificationStatus() != VerificationStatus.VERIFIED) {
            user.setVerificationStatus(VerificationStatus.VERIFIED);
            if (user.getVerifiedAt() == null) {
                user.setVerifiedAt(LocalDateTime.now());
                user.setVerifiedBy("System Sync");
            }
            userRepository.save(user);
        }
        
        return ResponseEntity.ok(toDTO(user));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EnterpriseVerificationDTO>> getPendingVerifications() {
        List<User> pendingEnterprises = userRepository
                .findByRoleAndVerificationStatus(Role.ENTREPRISE, VerificationStatus.PENDING);
        
        List<EnterpriseVerificationDTO> dtos = pendingEnterprises.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EnterpriseVerificationDTO>> getAllEnterprises(
            @RequestParam(required = false) VerificationStatus status,
            @RequestParam(required = false) String search) {
        
        List<User> enterprises;
        
        if (search != null && !search.trim().isEmpty()) {
            if (status != null) {
                enterprises = userRepository.searchEnterprisesByTextAndStatus(
                        Role.ENTREPRISE, search.trim(), status);
            } else {
                enterprises = userRepository.searchEnterprisesByText(Role.ENTREPRISE, search.trim());
            }
        } else if (status != null) {
            enterprises = userRepository.findByRoleAndVerificationStatus(Role.ENTREPRISE, status);
        } else {
            enterprises = userRepository.findByRole(Role.ENTREPRISE);
        }
        
        List<EnterpriseVerificationDTO> dtos = enterprises.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VerificationStatsDTO> getVerificationStats() {
        long total = userRepository.countByRole(Role.ENTREPRISE);
        long pending = userRepository.countByRoleAndVerificationStatus(Role.ENTREPRISE, VerificationStatus.PENDING);
        long verified = userRepository.countByRoleAndVerificationStatus(Role.ENTREPRISE, VerificationStatus.VERIFIED);
        long rejected = userRepository.countByRoleAndVerificationStatus(Role.ENTREPRISE, VerificationStatus.REJECTED);
        long notSubmitted = userRepository.countByRoleAndVerificationStatus(Role.ENTREPRISE, VerificationStatus.NOT_SUBMITTED);
        
        VerificationStatsDTO stats = VerificationStatsDTO.builder()
                .total(total)
                .pending(pending)
                .verified(verified)
                .rejected(rejected)
                .notSubmitted(notSubmitted)
                .build();
        
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/document/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Resource> getVerificationDocument(@PathVariable UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Utilisateur non trouvé"));
        
        if (user.getVerificationDocumentPath() == null) {
            throw new NotFoundException("Aucun document de vérification trouvé");
        }
        
        Resource resource = fileStorageService.loadVerificationDocument(user.getVerificationDocumentPath());
        
        String contentType = "application/octet-stream";
        String filename = user.getVerificationDocumentName() != null 
                ? user.getVerificationDocumentName() 
                : "document";
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }

    @PostMapping("/{userId}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EnterpriseVerificationDTO> verifyEnterprise(
            @PathVariable UUID userId,
            @Valid @RequestBody VerificationActionRequest request,
            Authentication authentication) {
        
        User enterprise = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Entreprise non trouvée"));
        
        if (enterprise.getRole() != Role.ENTREPRISE) {
            throw new BusinessRuleException("L'utilisateur n'est pas une entreprise");
        }
        
        User admin = getCurrentUser(authentication);
        
        enterprise.setVerificationStatus(request.getStatus());
        enterprise.setVerificationNotes(request.getNotes());
        enterprise.setVerifiedAt(LocalDateTime.now());
        enterprise.setVerifiedBy(admin.getNom());
        
        // Synchroniser le statut de compte global avec le statut de vérification
        if (request.getStatus() == VerificationStatus.VERIFIED) {
            enterprise.setStatus(tn.esprit.espritconnect2.Entitie.Status.ACCEPTEE);
            enterprise.setEnabled(true);
        } else if (request.getStatus() == VerificationStatus.REJECTED) {
            enterprise.setStatus(tn.esprit.espritconnect2.Entitie.Status.REFUSEE);
            enterprise.setEnabled(false);
        }
        
        userRepository.save(enterprise);
        
        log.info("Entreprise {} vérifiée par {}: statut={}", 
                enterprise.getEmail(), admin.getEmail(), request.getStatus());
        
        return ResponseEntity.ok(toDTO(enterprise));
    }

    @PostMapping("/{userId}/auto-verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AutoVerificationResultDTO> runAutoVerification(@PathVariable UUID userId) {
        User enterprise = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Entreprise non trouvée"));
        
        if (enterprise.getRole() != Role.ENTREPRISE) {
            throw new BusinessRuleException("L'utilisateur n'est pas une entreprise");
        }
        
        AutoVerificationResultDTO result = autoVerificationService.performAutoVerification(enterprise);
        
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{userId}/request-resubmission")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EnterpriseVerificationDTO> requestResubmission(
            @PathVariable UUID userId,
            @RequestParam(required = false) String reason,
            Authentication authentication) {
        
        User enterprise = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Entreprise non trouvée"));
        
        if (enterprise.getRole() != Role.ENTREPRISE) {
            throw new BusinessRuleException("L'utilisateur n'est pas une entreprise");
        }
        
        User admin = getCurrentUser(authentication);
        
        if (enterprise.getVerificationDocumentPath() != null) {
            fileStorageService.deleteVerificationDocument(enterprise.getVerificationDocumentPath());
            enterprise.setVerificationDocumentPath(null);
            enterprise.setVerificationDocumentName(null);
        }
        
        enterprise.setVerificationStatus(VerificationStatus.NOT_SUBMITTED);
        enterprise.setVerificationNotes(reason != null ? reason : "Re-soumission demandée par l'administrateur");
        enterprise.setVerifiedAt(LocalDateTime.now());
        enterprise.setVerifiedBy(admin.getNom());
        
        userRepository.save(enterprise);
        
        log.info("Re-soumission demandée pour l'entreprise {} par {}", 
                enterprise.getEmail(), admin.getEmail());
        
        return ResponseEntity.ok(toDTO(enterprise));
    }

    private User getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Utilisateur non trouvé"));
    }

    private EnterpriseVerificationDTO toDTO(User user) {
        return EnterpriseVerificationDTO.builder()
                .userId(user.getId())
                .nom(user.getNom())
                .email(user.getEmail())
                .businessRegistrationNumber(user.getBusinessRegistrationNumber())
                .companySector(user.getCompanySector())
                .companyWebsite(user.getCompanyWebsite())
                .companyDescription(user.getCompanyDescription())
                .verificationDocumentName(user.getVerificationDocumentName())
                .hasDocument(user.getVerificationDocumentPath() != null)
                .verificationStatus(user.getVerificationStatus())
                .verificationNotes(user.getVerificationNotes())
                .verifiedAt(user.getVerifiedAt())
                .verifiedBy(user.getVerifiedBy())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
