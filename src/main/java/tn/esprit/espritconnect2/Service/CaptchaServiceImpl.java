package tn.esprit.espritconnect2.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.espritconnect2.DTO.captcha.CaptchaGenerateResponse;
import tn.esprit.espritconnect2.DTO.captcha.CaptchaVerifyRequest;
import tn.esprit.espritconnect2.DTO.captcha.CaptchaVerifyResponse;
import tn.esprit.espritconnect2.Entitie.CaptchaAttemptLog;
import tn.esprit.espritconnect2.Entitie.CaptchaChallenge;
import tn.esprit.espritconnect2.Repository.CaptchaAttemptLogRepository;
import tn.esprit.espritconnect2.Repository.CaptchaChallengeRepository;
import tn.esprit.espritconnect2.captcha.CaptchaChallengeFactory;
import tn.esprit.espritconnect2.captcha.strategy.CaptchaChallengeStrategy;
import tn.esprit.espritconnect2.captcha.strategy.GeneratedChallengePayload;
import tn.esprit.espritconnect2.exception.CaptchaVerificationException;
import tn.esprit.espritconnect2.exception.NotFoundException;
import tn.esprit.espritconnect2.security.CaptchaRateLimiter;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CaptchaServiceImpl implements ICaptchaService {

    private final CaptchaChallengeRepository challengeRepository;
    private final CaptchaAttemptLogRepository attemptLogRepository;
    private final CaptchaChallengeFactory challengeFactory;
    private final CaptchaRateLimiter rateLimiter;
    private final ObjectMapper objectMapper;

    @Value("${app.captcha.enabled:true}")
    private boolean captchaEnabled;

    @Value("${app.captcha.expiration-minutes:5}")
    private int expirationMinutes;

    @Value("${app.captcha.max-attempts:3}")
    private int maxAttempts;

    @Value("${app.captcha.puzzle-tolerance-px:12}")
    private int puzzleTolerancePx;

    @Value("${app.captcha.token-validity-minutes:10}")
    private int tokenValidityMinutes;

    @Value("${app.captcha.rate-limit-per-ip:20}")
    private int rateLimitPerIp;

    @Value("${app.captcha.rate-limit-window-minutes:10}")
    private int rateLimitWindowMinutes;

    @Override
    @Transactional
    public CaptchaGenerateResponse generate(String clientIp) {
        if (!captchaEnabled) {
            throw new CaptchaVerificationException("Le CAPTCHA est désactivé.", "CAPTCHA_DISABLED");
        }
        assertNotRateLimited(clientIp, "generate");

        GeneratedChallengePayload payload = challengeFactory.generateRandom();
        LocalDateTime now = LocalDateTime.now();

        CaptchaChallenge challenge = CaptchaChallenge.builder()
                .captchaType(payload.getCaptchaType())
                .question(payload.getQuestion())
                .images(payload.getImagesJson())
                .correctAnswers(payload.getCorrectAnswersJson())
                .expirationDate(now.plusMinutes(expirationMinutes))
                .solved(false)
                .attempts(0)
                .createdAt(now)
                .consumed(false)
                .clientIp(clientIp)
                .build();

        challenge = challengeRepository.save(challenge);
        rateLimiter.recordRequest(clientIp);

        CaptchaGenerateResponse response = payload.getClientPayload();
        response.setCaptchaId(challenge.getId());
        return response;
    }

    @Override
    @Transactional
    public CaptchaVerifyResponse verify(CaptchaVerifyRequest request, String clientIp) {
        if (!captchaEnabled) {
            return CaptchaVerifyResponse.builder()
                    .success(true)
                    .message("Captcha bypass (disabled)")
                    .build();
        }

        assertNotRateLimited(clientIp, "verify");

        CaptchaChallenge challenge = challengeRepository.findById(request.getCaptchaId())
                .orElseThrow(() -> new NotFoundException("Captcha introuvable."));

        if (challenge.isConsumed()) {
            logFailedAttempt(challenge, clientIp, "Captcha déjà consommé");
            throw new CaptchaVerificationException("Ce captcha a déjà été utilisé.", "CAPTCHA_CONSUMED");
        }
        if (challenge.isSolved()) {
            throw new CaptchaVerificationException("Ce captcha a déjà été validé. Rechargez-en un nouveau.", "CAPTCHA_ALREADY_SOLVED");
        }
        if (challenge.isExpired()) {
            logFailedAttempt(challenge, clientIp, "Captcha expiré");
            throw new CaptchaVerificationException("Captcha expiré. Veuillez en générer un nouveau.", "CAPTCHA_EXPIRED");
        }
        if (challenge.getAttempts() >= maxAttempts) {
            logFailedAttempt(challenge, clientIp, "Nombre maximal de tentatives atteint");
            throw new CaptchaVerificationException("Nombre maximal de tentatives atteint.", "CAPTCHA_MAX_ATTEMPTS");
        }

        challenge.setAttempts(challenge.getAttempts() + 1);
        CaptchaChallengeStrategy strategy = challengeFactory.getStrategy(challenge.getCaptchaType());
        boolean valid = strategy.verify(challenge.getCorrectAnswers(), request.getAnswers(), puzzleTolerancePx);

        if (!valid) {
            challengeRepository.save(challenge);
            logFailedAttempt(challenge, clientIp, "Réponse incorrecte");
            int remaining = Math.max(0, maxAttempts - challenge.getAttempts());
            throw new CaptchaVerificationException(
                    "Réponse incorrecte. Tentatives restantes : " + remaining,
                    "CAPTCHA_INVALID"
            );
        }

        String token = UUID.randomUUID().toString().replace("-", "");
        challenge.setSolved(true);
        challenge.setVerifiedAt(LocalDateTime.now());
        challenge.setVerifiedToken(token);
        challengeRepository.save(challenge);
        rateLimiter.recordRequest(clientIp);

        log.info("Captcha {} validé pour IP {}", challenge.getId(), clientIp);

        return CaptchaVerifyResponse.builder()
                .success(true)
                .message("Captcha valid")
                .captchaId(challenge.getId())
                .captchaToken(token)
                .build();
    }

    @Override
    @Transactional
    public void validateForAuth(Long captchaId, String captchaToken, String clientIp) {
        if (!captchaEnabled) {
            return;
        }
        if (captchaId == null || captchaToken == null || captchaToken.isBlank()) {
            throw new CaptchaVerificationException("Validation CAPTCHA requise.", "CAPTCHA_REQUIRED");
        }

        CaptchaChallenge challenge = challengeRepository.findById(captchaId)
                .orElseThrow(() -> new CaptchaVerificationException("Captcha invalide.", "CAPTCHA_INVALID"));

        if (!challenge.isSolved() || challenge.getVerifiedToken() == null) {
            throw new CaptchaVerificationException("Le captcha n'a pas été validé.", "CAPTCHA_NOT_VERIFIED");
        }
        if (challenge.isConsumed()) {
            throw new CaptchaVerificationException("Ce captcha a déjà été utilisé.", "CAPTCHA_CONSUMED");
        }
        if (challenge.isExpired()) {
            throw new CaptchaVerificationException("Captcha expiré.", "CAPTCHA_EXPIRED");
        }
        if (challenge.getVerifiedAt() == null ||
                challenge.getVerifiedAt().plusMinutes(tokenValidityMinutes).isBefore(LocalDateTime.now())) {
            throw new CaptchaVerificationException("Jeton captcha expiré. Validez à nouveau le captcha.", "CAPTCHA_TOKEN_EXPIRED");
        }
        if (!captchaToken.equals(challenge.getVerifiedToken())) {
            logFailedAttempt(challenge, clientIp, "Jeton captcha invalide");
            throw new CaptchaVerificationException("Jeton captcha invalide.", "CAPTCHA_INVALID");
        }

        challenge.setConsumed(true);
        challengeRepository.save(challenge);
    }

    private void assertNotRateLimited(String clientIp, String action) {
        if (rateLimiter.isRateLimited(clientIp, rateLimitPerIp, rateLimitWindowMinutes)) {
            log.warn("Rate limit CAPTCHA {} pour IP {}", action, clientIp);
            throw new CaptchaVerificationException("Trop de requêtes. Réessayez plus tard.", "CAPTCHA_RATE_LIMIT");
        }
    }

    private void logFailedAttempt(CaptchaChallenge challenge, String clientIp, String reason) {
        attemptLogRepository.save(CaptchaAttemptLog.builder()
                .captchaId(challenge.getId())
                .clientIp(clientIp)
                .success(false)
                .failureReason(reason)
                .captchaType(challenge.getCaptchaType().name())
                .createdAt(LocalDateTime.now())
                .build());
        log.warn("Échec CAPTCHA id={} type={} ip={} raison={}",
                challenge.getId(), challenge.getCaptchaType(), clientIp, reason);
    }

    public static String resolveClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isBlank()) {
            return xfHeader.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
