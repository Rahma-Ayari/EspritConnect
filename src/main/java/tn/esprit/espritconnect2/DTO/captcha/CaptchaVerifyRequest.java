package tn.esprit.espritconnect2.DTO.captcha;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaptchaVerifyRequest {

    @NotNull(message = "L'identifiant du captcha est requis")
    private Long captchaId;

    @NotEmpty(message = "Les réponses sont requises")
    private List<String> answers;
}
