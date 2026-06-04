package tn.esprit.espritconnect2.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprit.espritconnect2.Entitie.SmartMailingSettings.NotificationMode;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SmartMailingSettingsDTO {
    
    @Builder.Default
    private boolean enabled = true;
    
    @Builder.Default
    private int batchThreshold = 5;
    
    @Builder.Default
    private int batchWindowMinutes = 30;
    
    @Builder.Default
    private NotificationMode notificationMode = NotificationMode.SMART;
    
    @Builder.Default
    private int dailyDigestHour = 8;
    
    @Builder.Default
    private boolean prioritizeEnterprise = true;
    
    @Builder.Default
    private boolean prioritizeAlumni = false;
    
    @Builder.Default
    private int smartThresholdPerHour = 10;
    
    @Builder.Default
    private boolean dashboardNotificationsEnabled = true;
}
