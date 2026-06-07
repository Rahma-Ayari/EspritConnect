package tn.esprit.espritconnect2.Service.emailBackOffice.communications;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BirthdayEmailScheduler {

    private final IBirthdayEmailSettingsService birthdayEmailSettingsService;

    /** Checks every hour whether birthday emails are due and sends them. */
    @Scheduled(cron = "0 0 * * * *")
    public void tick() {
        try {
            birthdayEmailSettingsService.runScheduledIfDue();
        } catch (Exception ex) {
            log.error("Échec scheduler anniversaire: {}", ex.getMessage(), ex);
        }
    }
}
