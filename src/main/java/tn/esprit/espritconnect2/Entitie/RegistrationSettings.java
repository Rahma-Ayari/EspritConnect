package tn.esprit.espritconnect2.Entitie;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "registration_settings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationSettings {

    @Id
    private Long id = 1L;

    @Builder.Default
    private boolean linkedInEnabled = true;
    @Builder.Default
    private boolean facebookEnabled = false;
    @Builder.Default
    private boolean googleEnabled = true;
    @Builder.Default
    private boolean ssoEnabled = false;
    @Builder.Default
    private boolean emailEnabled = true;
    @Builder.Default
    private boolean appleEnabled = false;

    @Column(columnDefinition = "TEXT")
    @Builder.Default
    private String termsAndPrivacyHtml = "<p>By registering on ESPRIT Connect you agree to our terms of service and privacy policy.</p>";
}
