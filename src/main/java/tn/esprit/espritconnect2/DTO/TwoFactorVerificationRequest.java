package tn.esprit.espritconnect2.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TwoFactorVerificationRequest {

    @Email(message = "Format email invalide")
    @NotBlank(message = "L'email est requis")
    private String email;

    @NotBlank(message = "Le code TOTP est requis")
    private String code;

    @NotBlank(message = "Le jeton de vérification MFA est requis")
    private String mfaPendingToken;

    private boolean rememberDevice;
}
