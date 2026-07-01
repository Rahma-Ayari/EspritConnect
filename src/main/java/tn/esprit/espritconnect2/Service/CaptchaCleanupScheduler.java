package tn.esprit.espritconnect2.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.espritconnect2.Repository.CaptchaChallengeRepository;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class CaptchaCleanupScheduler {

    private final CaptchaChallengeRepository challengeRepository;

    @Value("${app.captcha.consumed-retention-hours:24}")
    private int consumedRetentionHours;

    @Scheduled(cron = "${app.captcha.cleanup-cron:0 */15 * * * *}")
    @Transactional
    public void cleanupExpiredCaptchas() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime consumedCutoff = now.minusHours(consumedRetentionHours);
        int deleted = challengeRepository.deleteExpiredOrOldConsumed(now, consumedCutoff);
        if (deleted > 0) {
            log.info("Nettoyage CAPTCHA : {} enregistrements supprimés", deleted);
        }
    }
}
