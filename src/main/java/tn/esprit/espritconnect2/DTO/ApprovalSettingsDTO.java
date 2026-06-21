package tn.esprit.espritconnect2.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalSettingsDTO {
    
    @Builder.Default
    private boolean autoApproveEspritEmails = true;
    
    @Builder.Default
    private boolean emailNotificationsOnNewRegistration = true;
    
    @Builder.Default
    private boolean requireEmailVerification = false;
    
    @Builder.Default
    private boolean notifyUserOnApproval = true;
    
    @Builder.Default
    private boolean notifyUserOnDecline = true;
    
    @Builder.Default
    private String autoApproveDomain = "esprit.tn";
}
