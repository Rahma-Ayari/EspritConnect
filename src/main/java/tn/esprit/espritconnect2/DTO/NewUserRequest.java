package tn.esprit.espritconnect2.DTO;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import tn.esprit.espritconnect2.Entitie.Role;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewUserRequest {
    private String nom;
    private String email;
    private String role;
    private String affiliation;

    @JsonIgnore
    public Role getRoleEnum() {
        if (role == null || role.trim().isEmpty()) {
            return Role.ETUDIANT;
        }
        try {
            return Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Role.ETUDIANT;
        }
    }
}
