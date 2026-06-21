package tn.esprit.espritconnect2.DTO.captcha;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaptchaQuestionOptionDto {
    private String id;
    private String text;
    private String imageUrl;
}
