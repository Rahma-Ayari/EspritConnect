package tn.esprit.espritconnect2.DTO.captcha;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaptchaVerifyResponse {
    private boolean success;
    private String message;
    private Long captchaId;
    private String captchaToken;
}
