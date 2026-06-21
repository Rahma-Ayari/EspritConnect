package tn.esprit.espritconnect2.Service;

import tn.esprit.espritconnect2.DTO.captcha.CaptchaGenerateResponse;
import tn.esprit.espritconnect2.DTO.captcha.CaptchaVerifyRequest;
import tn.esprit.espritconnect2.DTO.captcha.CaptchaVerifyResponse;

public interface ICaptchaService {

    CaptchaGenerateResponse generate(String clientIp);

    CaptchaVerifyResponse verify(CaptchaVerifyRequest request, String clientIp);

    void validateForAuth(Long captchaId, String captchaToken, String clientIp);
}
