package tn.esprit.espritconnect2.Service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.esprit.espritconnect2.DTO.ApprovalSettingsDTO;
import tn.esprit.espritconnect2.Entitie.ApprovalSettings;
import tn.esprit.espritconnect2.Entitie.Role;
import tn.esprit.espritconnect2.Entitie.Status;
import tn.esprit.espritconnect2.Entitie.User;
import tn.esprit.espritconnect2.Entitie.VerificationStatus;
import tn.esprit.espritconnect2.Repository.ApprovalSettingsRepository;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Slf4j
@RequiredArgsConstructor
public class ApprovalSettingsService {

    private final ApprovalSettingsRepository repository;
    private final AtomicReference<ApprovalSettingsDTO> currentSettings = new AtomicReference<>();

    @PostConstruct
    public void init() {
        ApprovalSettings settings = repository.findById(1L).orElseGet(() -> {
            ApprovalSettings defaultSettings = ApprovalSettings.builder()
                    .id(1L)
                    .autoApproveEspritEmails(true)
                    .emailNotificationsOnNewRegistration(true)
                    .requireEmailVerification(true)
                    .notifyUserOnApproval(true)
                    .notifyUserOnDecline(true)
                    .autoApproveDomain("esprit.tn")
                    .build();
            return repository.save(defaultSettings);
        });
        if (!settings.isNotifyUserOnDecline()) {
            settings.setNotifyUserOnDecline(true);
            settings = repository.save(settings);
            log.info("Enabled notifyUserOnDecline in approval settings");
        }
        currentSettings.set(mapToDTO(settings));
        log.info("Approval settings loaded from database: autoApproveEspritEmails={}", settings.isAutoApproveEspritEmails());
    }

    public ApprovalSettingsDTO getSettings() {
        return currentSettings.get();
    }

    public ApprovalSettingsDTO updateSettings(ApprovalSettingsDTO dto) {
        dto.setAutoApproveDomain(normalizeDomain(dto.getAutoApproveDomain()));

        ApprovalSettings settings = ApprovalSettings.builder()
                .id(1L)
                .autoApproveEspritEmails(dto.isAutoApproveEspritEmails())
                .emailNotificationsOnNewRegistration(dto.isEmailNotificationsOnNewRegistration())
                .requireEmailVerification(dto.isRequireEmailVerification())
                .notifyUserOnApproval(dto.isNotifyUserOnApproval())
                .notifyUserOnDecline(dto.isNotifyUserOnDecline())
                .autoApproveDomain(dto.getAutoApproveDomain())
                .build();
        
        repository.save(settings);
        currentSettings.set(dto);
        
        log.info("Approval settings updated and persisted: autoApproveEspritEmails={}, domain={}", 
                dto.isAutoApproveEspritEmails(), 
                dto.getAutoApproveDomain());
        return dto;
    }

    public boolean shouldAutoApprove(String email) {
        ApprovalSettingsDTO settings = currentSettings.get();
        
        if (settings == null || !settings.isAutoApproveEspritEmails()) {
            return false;
        }
        
        if (email == null || email.isEmpty()) {
            return false;
        }
        
        String domainLower = normalizeDomain(settings.getAutoApproveDomain());
        String emailLower = email.toLowerCase().trim();

        return emailLower.endsWith("@" + domainLower);
    }

    /**
     * Applique l'auto-approbation complète (enabled + statut ACCEPTEE) si l'email correspond au domaine configuré.
     */
    public boolean applyAutoApproval(User user) {
        if (!shouldAutoApprove(user.getEmail())) {
            return false;
        }

        user.setEnabled(true);
        user.setStatus(Status.ACCEPTEE);
        if (user.getRole() == Role.ENTREPRISE) {
            user.setVerificationStatus(VerificationStatus.VERIFIED);
            if (user.getVerifiedAt() == null) {
                user.setVerifiedAt(LocalDateTime.now());
                user.setVerifiedBy("Auto Approved");
            }
        }

        log.info("Auto-approbation appliquée pour {}", user.getEmail());
        return true;
    }

    private String normalizeDomain(String domain) {
        if (domain == null || domain.isBlank()) {
            return "esprit.tn";
        }
        String normalized = domain.toLowerCase().trim();
        if (normalized.startsWith("@")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }

    public void resetToDefaults() {
        ApprovalSettingsDTO defaultDTO = ApprovalSettingsDTO.builder()
                .autoApproveEspritEmails(true)
                .emailNotificationsOnNewRegistration(true)
                .requireEmailVerification(false)
                .notifyUserOnApproval(true)
                .notifyUserOnDecline(true)
                .autoApproveDomain("esprit.tn")
                .build();
        updateSettings(defaultDTO);
    }

    private ApprovalSettingsDTO mapToDTO(ApprovalSettings entity) {
        return ApprovalSettingsDTO.builder()
                .autoApproveEspritEmails(entity.isAutoApproveEspritEmails())
                .emailNotificationsOnNewRegistration(entity.isEmailNotificationsOnNewRegistration())
                .requireEmailVerification(entity.isRequireEmailVerification())
                .notifyUserOnApproval(entity.isNotifyUserOnApproval())
                .notifyUserOnDecline(entity.isNotifyUserOnDecline())
                .autoApproveDomain(normalizeDomain(entity.getAutoApproveDomain()))
                .build();
    }
}
