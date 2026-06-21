package tn.esprit.espritconnect2.DTO.captcha;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprit.espritconnect2.Entitie.CaptchaType;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaptchaGenerateResponse {
    private Long captchaId;
    private CaptchaType captchaType;
    private String question;
    private List<CaptchaImageOptionDto> images;
    private List<CaptchaQuestionOptionDto> options;
    private CaptchaPuzzleDataDto puzzleData;
}
