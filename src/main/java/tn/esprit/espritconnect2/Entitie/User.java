package tn.esprit.espritconnect2.Entitie;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@Inheritance(strategy = InheritanceType.JOINED)
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Column(nullable = false)
    private String nom;

    @Email @NotBlank
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "enabled")
    @Builder.Default
    private boolean enabled = false;

    @Column(name = "email_verified")
    @Builder.Default
    private Boolean emailVerified = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private Status status = Status.EN_ATTENTE;

    // Champs de vérification entreprise
    @Column(name = "verification_document_path")
    private String verificationDocumentPath;

    @Column(name = "verification_document_name")
    private String verificationDocumentName;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status")
    @Builder.Default
    private VerificationStatus verificationStatus = VerificationStatus.NOT_SUBMITTED;

    @Column(name = "business_registration_number")
    private String businessRegistrationNumber;

    @Column(name = "company_sector")
    private String companySector;

    @Column(name = "company_website")
    private String companyWebsite;

    @Column(name = "company_description", length = 1000)
    private String companyDescription;

    @Column(name = "verification_notes", length = 500)
    private String verificationNotes;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "verified_by")
    private String verifiedBy;

    @Column(name = "two_factor_enabled")
    @Builder.Default
    private Boolean twoFactorEnabled = false;

    @Column(name = "two_factor_secret")
    private String twoFactorSecret;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_backup_codes", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "backup_code")
    @Builder.Default
    private List<String> backupCodes = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (verificationStatus == null) {
            verificationStatus = VerificationStatus.NOT_SUBMITTED;
        }
        if (twoFactorEnabled == null) {
            twoFactorEnabled = false;
        }
        if (emailVerified == null) {
            emailVerified = false;
        }
    }

    @PostLoad
    private void normalizeNullableBooleans() {
        if (twoFactorEnabled == null) {
            twoFactorEnabled = false;
        }
        if (emailVerified == null) {
            emailVerified = false;
        }
    }

    /** Null-safe (colonnes NULL en base → non vérifié). */
    public boolean isEmailVerified() {
        return Boolean.TRUE.equals(emailVerified);
    }

    /** Null-safe (colonnes NULL en base → désactivé). */
    public boolean isTwoFactorEnabled() {
        return Boolean.TRUE.equals(twoFactorEnabled);
    }

    /** Compte admin : pas de vérification email obligatoire. */
    public boolean requiresEmailVerification() {
        return role != Role.ADMIN;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override public String getUsername()             { return email; }
    @Override public boolean isAccountNonExpired()    { return true; }
    @Override public boolean isAccountNonLocked()     { return true; }
    @Override public boolean isCredentialsNonExpired(){ return true; }
    @Override public boolean isEnabled()              { return enabled; }
}
