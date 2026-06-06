package tn.esprit.espritconnect2.Entitie;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_devices")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UserDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "device_token", nullable = false, unique = true)
    private String deviceToken;

    @Column(name = "device_name")
    private String deviceName;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
}
