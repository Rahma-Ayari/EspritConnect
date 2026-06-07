package tn.esprit.espritconnect2.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AuthResponse {
    private String token;
    @Builder.Default
    private String type = "Bearer";
    private String role;
    private String nom;
    private String email;        // ✅ add this
    private int    scoreReadiness;
    private String userId;
    private boolean mfaRequired;
    /** Jeton court émis après mot de passe valide ; obligatoire pour verify-2fa-login. */
    private String mfaPendingToken;
    private String deviceToken;
}
