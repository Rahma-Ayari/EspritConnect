package tn.esprit.espritconnect2.Service.emailBackOffice.communications;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.espritconnect2.DTO.emailBackOffice.communications.BirthdayEmailSettingsRequestDTO;
import tn.esprit.espritconnect2.DTO.emailBackOffice.communications.BirthdayEmailSettingsResponseDTO;
import tn.esprit.espritconnect2.DTO.emailBackOffice.communications.SendTestBirthdayEmailRequestDTO;
import tn.esprit.espritconnect2.Entitie.emailBackOffice.BirthdayEmailSettings;
import tn.esprit.espritconnect2.Entitie.emailBackOffice.EmailHistory;
import tn.esprit.espritconnect2.Entitie.emailBackOffice.enums.EmailDeliveryStatus;
import tn.esprit.espritconnect2.Entitie.emailBackOffice.enums.EmailHistoryType;
import tn.esprit.espritconnect2.Repository.emailBackOffice.BirthdayEmailSettingsRepository;
import tn.esprit.espritconnect2.Repository.emailBackOffice.EmailHistoryRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BirthdayEmailSettingsServiceImpl implements IBirthdayEmailSettingsService {

    private static final Long SETTINGS_ID = 1L;

    private final BirthdayEmailSettingsRepository repo;
    private final EmailDispatchService dispatch;
    private final EmailHistoryRepository historyRepo;

    private BirthdayEmailSettings ensure() {
        return repo.findById(SETTINGS_ID).orElseGet(() -> repo.save(BirthdayEmailSettings.builder()
                .id(SETTINGS_ID)
                .active(false)
                .subject("Joyeux anniversaire depuis EspritConnect")
                .templateHtml("<p>Bonjour <b>{{nom}}</b>, joyeux anniversaire !</p>")
                .sendHour(8)
                .build()));
    }

    @Override
    @Transactional(readOnly = true)
    public BirthdayEmailSettingsResponseDTO get() {
        return toDto(ensure());
    }

    @Override
    @Transactional
    public BirthdayEmailSettingsResponseDTO update(BirthdayEmailSettingsRequestDTO dto) {
        BirthdayEmailSettings s = ensure();
        s.setActive(Boolean.TRUE.equals(dto.getActive()));
        s.setSubject(dto.getSubject());
        s.setTemplateHtml(dto.getTemplateHtml());
        s.setSendHour(dto.getSendHour() != null ? dto.getSendHour() : s.getSendHour());
        return toDto(repo.save(s));
    }

    /**
     * Test manuel : utile en démo pour ton suivi, sans scheduler.
     * Remplace {{nom}} dans le template.
     */
    @Override
    @Transactional
    public void sendTest(SendTestBirthdayEmailRequestDTO dto) {
        BirthdayEmailSettings s = ensure();
        String html = s.getTemplateHtml().replace("{{nom}}", dto.getNomDemo());

        try {
            // fromEmail : on réutilise spring.mail.username côté SMTP (souvent obligatoire avec Gmail)
            // Ici on prend un expéditeur simple : tu peux le rendre configurable plus tard.
            dispatch.sendHtml(dto.getToEmail(), "noreply@esprit.tn", s.getSubject(), html);

            historyRepo.save(EmailHistory.builder()
                    .type(EmailHistoryType.BIRTHDAY_TEST)
                    .campaign(null)
                    .toEmail(dto.getToEmail())
                    .subject(s.getSubject())
                    .deliveryStatus(EmailDeliveryStatus.SUCCESS)
                    .errorMessage(null)
                    .sentAt(LocalDateTime.now())
                    .build());
        } catch (Exception ex) {
            historyRepo.save(EmailHistory.builder()
                    .type(EmailHistoryType.BIRTHDAY_TEST)
                    .campaign(null)
                    .toEmail(dto.getToEmail())
                    .subject(s.getSubject())
                    .deliveryStatus(EmailDeliveryStatus.FAILED)
                    .errorMessage(ex.getMessage())
                    .sentAt(LocalDateTime.now())
                    .build());
            throw new RuntimeException("Échec envoi test anniversaire: " + ex.getMessage());
        }
    }

    private BirthdayEmailSettingsResponseDTO toDto(BirthdayEmailSettings s) {
        return BirthdayEmailSettingsResponseDTO.builder()
                .id(s.getId())
                .active(s.getActive())
                .subject(s.getSubject())
                .templateHtml(s.getTemplateHtml())
                .sendHour(s.getSendHour())
                .lastRunAt(s.getLastRunAt() != null ? s.getLastRunAt().toString() : null)
                .build();
    }
}