package tn.esprit.espritconnect2.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TwoFactorDisableRequest {

    @NotBlank(message = "Le mot de passe est requis")
    private String password;

    @NotBlank(message = "Le code TOTP est requis")
    private String code;
}
