package tn.esprit.espritconnect2.Entitie;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Singleton (id = 1): which OAuth / email registration paths are enabled and legal copy for registration.
 */
@Entity
@Table(name = "registration_platform_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationPlatformSettings {

    public static final Long SINGLETON_ID = 1L;

    @Id
    @Column(name = "id")
    private Long id = SINGLETON_ID;

    @Column(name = "linkedin_enabled", nullable = false)
    @Builder.Default
    private boolean linkedInEnabled = true;

    @Column(name = "facebook_enabled", nullable = false)
    @Builder.Default
    private boolean facebookEnabled = false;

    @Column(name = "google_enabled", nullable = false)
    @Builder.Default
    private boolean googleEnabled = true;

    @Column(name = "sso_enabled", nullable = false)
    @Builder.Default
    private boolean ssoEnabled = false;

    @Column(name = "email_enabled", nullable = false)
    @Builder.Default
    private boolean emailEnabled = true;

    @Column(name = "apple_enabled", nullable = false)
    @Builder.Default
    private boolean appleEnabled = true;

    @Lob
    @Column(name = "terms_and_privacy_html", columnDefinition = "LONGTEXT")
    @Builder.Default
    private String termsAndPrivacyHtml = "";

    @PrePersist
    void ensureId() {
        if (id == null) {
            id = SINGLETON_ID;
        }
    }
}
