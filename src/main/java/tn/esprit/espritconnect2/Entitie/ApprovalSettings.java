package tn.esprit.espritconnect2.Entitie;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "approval_settings")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ApprovalSettings {

    @Id
    private Long id = 1L;

    @Column(nullable = false)
    private boolean autoApproveEspritEmails;

    @Column(nullable = false)
    private boolean emailNotificationsOnNewRegistration;

    @Column(nullable = false)
    private boolean requireEmailVerification;

    @Column(nullable = false)
    private boolean notifyUserOnApproval;

    @Column(nullable = false)
    private boolean notifyUserOnDecline;

    @Column(nullable = false)
    private String autoApproveDomain;
}
