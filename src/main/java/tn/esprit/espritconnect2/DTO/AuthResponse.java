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
    private String email;
    private int    scoreReadiness;
    private String userId;
}
