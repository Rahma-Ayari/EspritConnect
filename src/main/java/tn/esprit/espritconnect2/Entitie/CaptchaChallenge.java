package tn.esprit.espritconnect2.Entitie;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "captcha_challenge", indexes = {
        @Index(name = "idx_captcha_expiration", columnList = "expiration_date"),
        @Index(name = "idx_captcha_verified_token", columnList = "verified_token")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaptchaChallenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "captcha_type", nullable = false, length = 20)
    private CaptchaType captchaType;

    @Column(nullable = false, length = 500)
    private String question;

    /** JSON : options affichées côté client (images, logos, choix, métadonnées puzzle). */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String images;

    /** JSON : réponses correctes (jamais exposées au client). */
    @Column(name = "correct_answers", columnDefinition = "TEXT", nullable = false)
    private String correctAnswers;

    @Column(name = "expiration_date", nullable = false)
    private LocalDateTime expirationDate;

    @Column(nullable = false)
    @Builder.Default
    private boolean solved = false;

    @Column(nullable = false)
    @Builder.Default
    private int attempts = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "verified_token", length = 64)
    private String verifiedToken;

    @Column(nullable = false)
    @Builder.Default
    private boolean consumed = false;

    @Column(name = "client_ip", length = 45)
    private String clientIp;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expirationDate);
    }
}
