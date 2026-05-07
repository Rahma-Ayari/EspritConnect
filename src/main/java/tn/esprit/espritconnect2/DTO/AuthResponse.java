package tn.esprit.espritconnect2.DTO;

import lombok.Data;

// ── Auth Response ──────────────────────────────────────────────────────────────
@Data
class AuthResponse {
    private String token;
    private String role;
    private String nom;
    private String email;
    private int scoreReadiness;
}
