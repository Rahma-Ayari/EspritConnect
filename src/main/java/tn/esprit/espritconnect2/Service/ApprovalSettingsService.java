package tn.esprit.espritconnect2.Service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.esprit.espritconnect2.DTO.ApprovalSettingsDTO;
import tn.esprit.espritconnect2.Entitie.ApprovalSettings;
import tn.esprit.espritconnect2.Repository.ApprovalSettingsRepository;

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
                    .requireEmailVerification(false)
                    .notifyUserOnApproval(true)
                    .notifyUserOnDecline(false)
                    .autoApproveDomain("esprit.tn")
                    .build();
            return repository.save(defaultSettings);
        });
        currentSettings.set(mapToDTO(settings));
        log.info("Approval settings loaded from database: autoApproveEspritEmails={}", settings.isAutoApproveEspritEmails());
    }

    public ApprovalSettingsDTO getSettings() {
        return currentSettings.get();
    }

    public ApprovalSettingsDTO updateSettings(ApprovalSettingsDTO dto) {
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
        
        String domain = settings.getAutoApproveDomain();
        if (domain == null || domain.isEmpty()) {
            domain = "esprit.tn";
        }
        
        String emailLower = email.toLowerCase().trim();
        String domainLower = domain.toLowerCase().trim();
        
        return emailLower.endsWith("@" + domainLower);
    }

    public void resetToDefaults() {
        ApprovalSettingsDTO defaultDTO = ApprovalSettingsDTO.builder()
                .autoApproveEspritEmails(true)
                .emailNotificationsOnNewRegistration(true)
                .requireEmailVerification(false)
                .notifyUserOnApproval(true)
                .notifyUserOnDecline(false)
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
                .autoApproveDomain(entity.getAutoApproveDomain())
                .build();
    }
}
