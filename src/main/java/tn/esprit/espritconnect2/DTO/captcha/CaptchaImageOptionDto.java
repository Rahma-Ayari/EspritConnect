package tn.esprit.espritconnect2.DTO.captcha;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaptchaImageOptionDto {
    private String id;
    private String url;
    private String label;
}
