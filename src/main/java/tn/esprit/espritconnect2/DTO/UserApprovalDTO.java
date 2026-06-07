package tn.esprit.espritconnect2.DTO;

import lombok.*;
import tn.esprit.espritconnect2.Entitie.Role;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserApprovalDTO {
    private UUID id;
    private String nom;
    private String email;
    private Role role;
    private String affiliation;
    private LocalDateTime registrationDate;
    private String status;
    private String avatarInitials;
    private String avatarColor;
}
