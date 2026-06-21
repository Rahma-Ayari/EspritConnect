package tn.esprit.espritconnect2.Entitie;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "captcha_attempt_log", indexes = {
        @Index(name = "idx_captcha_log_ip", columnList = "client_ip"),
        @Index(name = "idx_captcha_log_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaptchaAttemptLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "captcha_id")
    private Long captchaId;

    @Column(name = "client_ip", length = 45)
    private String clientIp;

    @Column(nullable = false)
    private boolean success;

    @Column(name = "failure_reason", length = 255)
    private String failureReason;

    @Column(name = "captcha_type", length = 20)
    private String captchaType;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
