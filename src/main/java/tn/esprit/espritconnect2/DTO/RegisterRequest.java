package tn.esprit.espritconnect2.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

// ── Register ───────────────────────────────────────────────────────────────────
@Data
class RegisterRequest {
    @NotBlank
    private String nom;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 6)
    private String password;

    private String niveau;
    private String filiere;
}
