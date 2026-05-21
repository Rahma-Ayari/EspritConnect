package tn.esprit.espritconnect2.Service.emailBackOffice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tn.esprit.espritconnect2.Entitie.emailBackOffice.DigestConfig;
import tn.esprit.espritconnect2.Repository.emailBackOffice.DigestConfigRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityDigestScheduler {

    private final DigestConfigRepository configRepo;
    private final ActivityDigestSendService sendService;

    // Toutes les heures
    @Scheduled(cron = "0 0 * * * *")
    public void tick() {
        DigestConfig cfg = configRepo.findById(1L).orElse(null);
        if (cfg == null || !Boolean.TRUE.equals(cfg.getActif())) return;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime last = cfg.getLastSentAt();

        boolean due;
        if ("DAILY".equals(cfg.getFrequence())) {
            due = last == null || last.isBefore(now.minusDays(1));
        } else {
            due = last == null || last.isBefore(now.minusDays(7));
        }

        if (!due) return;

        log.info("Digest dû ({}) → envoi automatique", cfg.getFrequence());
        try {
            sendService.sendNowToAllEnabledUsers();
        } catch (Exception e) {
            log.error("Échec scheduler digest: {}", e.getMessage());
        }
    }
}