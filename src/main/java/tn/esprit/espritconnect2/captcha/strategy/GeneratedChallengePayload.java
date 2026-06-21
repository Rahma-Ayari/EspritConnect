package tn.esprit.espritconnect2.captcha.strategy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprit.espritconnect2.DTO.captcha.CaptchaGenerateResponse;
import tn.esprit.espritconnect2.Entitie.CaptchaType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeneratedChallengePayload {
    private CaptchaType captchaType;
    private String question;
    private String imagesJson;
    private String correctAnswersJson;
    private CaptchaGenerateResponse clientPayload;
}
