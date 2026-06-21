package tn.esprit.espritconnect2.Controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.espritconnect2.DTO.captcha.CaptchaGenerateResponse;
import tn.esprit.espritconnect2.DTO.captcha.CaptchaVerifyRequest;
import tn.esprit.espritconnect2.DTO.captcha.CaptchaVerifyResponse;
import tn.esprit.espritconnect2.Service.CaptchaServiceImpl;
import tn.esprit.espritconnect2.Service.ICaptchaService;

@RestController
@RequestMapping("/api/captcha")
@RequiredArgsConstructor
public class CaptchaController {

    private final ICaptchaService captchaService;

    @GetMapping("/generate")
    public ResponseEntity<CaptchaGenerateResponse> generate(HttpServletRequest request) {
        String clientIp = CaptchaServiceImpl.resolveClientIp(request);
        return ResponseEntity.ok(captchaService.generate(clientIp));
    }

    @PostMapping("/verify")
    public ResponseEntity<CaptchaVerifyResponse> verify(
            @Valid @RequestBody CaptchaVerifyRequest body,
            HttpServletRequest request) {
        String clientIp = CaptchaServiceImpl.resolveClientIp(request);
        return ResponseEntity.ok(captchaService.verify(body, clientIp));
    }
}
