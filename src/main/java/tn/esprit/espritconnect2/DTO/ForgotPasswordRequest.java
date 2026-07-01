package tn.esprit.espritconnect2.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ForgotPasswordRequest {

    @Email(message = "Format email invalide")
    @NotBlank(message = "L'email est requis")
    private String email;

    @NotNull(message = "L'identifiant captcha est requis")
    private Long captchaId;

    @NotBlank(message = "Le jeton captcha est requis")
    private String captchaToken;
}
