package tn.esprit.espritconnect2.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

// ── Login ──────────────────────────────────────────────────────────────────────
@Data
class LoginRequest {
    @Email @NotBlank
    private String email;

    @NotBlank @Size(min = 6)
    private String password;
}

