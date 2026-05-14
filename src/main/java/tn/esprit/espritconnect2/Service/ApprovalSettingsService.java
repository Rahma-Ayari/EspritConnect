package tn.esprit.espritconnect2.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.esprit.espritconnect2.DTO.ApprovalSettingsDTO;

import java.util.concurrent.atomic.AtomicReference;

@Service
@Slf4j
public class ApprovalSettingsService {

    private final AtomicReference<ApprovalSettingsDTO> currentSettings = 
            new AtomicReference<>(ApprovalSettingsDTO.builder()
                    .autoApproveEspritEmails(true)
                    .emailNotificationsOnNewRegistration(true)
                    .requireEmailVerification(false)
                    .notifyUserOnApproval(true)
                    .notifyUserOnDecline(false)
                    .autoApproveDomain("esprit.tn")
                    .build());

    public ApprovalSettingsDTO getSettings() {
        return currentSettings.get();
    }

    public ApprovalSettingsDTO updateSettings(ApprovalSettingsDTO newSettings) {
        currentSettings.set(newSettings);
        log.info("Approval settings updated: autoApproveEspritEmails={}, domain={}", 
                newSettings.isAutoApproveEspritEmails(), 
                newSettings.getAutoApproveDomain());
        return currentSettings.get();
    }

    public boolean shouldAutoApprove(String email) {
        ApprovalSettingsDTO settings = currentSettings.get();
        
        if (!settings.isAutoApproveEspritEmails()) {
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
        
        boolean shouldApprove = emailLower.endsWith("@" + domainLower);
        
        log.debug("Auto-approve check for email '{}': domain='{}', result={}", 
                email, domain, shouldApprove);
        
        return shouldApprove;
    }

    public void resetToDefaults() {
        currentSettings.set(ApprovalSettingsDTO.builder()
                .autoApproveEspritEmails(true)
                .emailNotificationsOnNewRegistration(true)
                .requireEmailVerification(false)
                .notifyUserOnApproval(true)
                .notifyUserOnDecline(false)
                .autoApproveDomain("esprit.tn")
                .build());
        log.info("Approval settings reset to defaults");
    }
}
