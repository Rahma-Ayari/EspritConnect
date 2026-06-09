package tn.esprit.espritconnect2.Service.emailBackOffice.communications;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.espritconnect2.DTO.emailBackOffice.communications.BirthdayEmailSettingsRequestDTO;
import tn.esprit.espritconnect2.DTO.emailBackOffice.communications.BirthdayEmailSettingsResponseDTO;
import tn.esprit.espritconnect2.DTO.emailBackOffice.communications.SendTestBirthdayEmailRequestDTO;
import tn.esprit.espritconnect2.Entitie.Profil;
import tn.esprit.espritconnect2.Entitie.User;
import tn.esprit.espritconnect2.Entitie.emailBackOffice.BirthdayEmailSettings;
import tn.esprit.espritconnect2.Entitie.emailBackOffice.EmailHistory;
import tn.esprit.espritconnect2.Entitie.emailBackOffice.enums.EmailDeliveryStatus;
import tn.esprit.espritconnect2.Entitie.emailBackOffice.enums.EmailHistoryType;
import tn.esprit.espritconnect2.Repository.ProfilRepository;
import tn.esprit.espritconnect2.Repository.UserRepository;
import tn.esprit.espritconnect2.Repository.emailBackOffice.BirthdayEmailSettingsRepository;
import tn.esprit.espritconnect2.Repository.emailBackOffice.EmailHistoryRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BirthdayEmailSettingsServiceImpl implements IBirthdayEmailSettingsService {

    private static final Long SETTINGS_ID = 1L;

    private final BirthdayEmailSettingsRepository repo;
    private final EmailDispatchService dispatch;
    private final EmailHistoryRepository historyRepo;
    private final ProfilRepository profilRepository;
    private final UserRepository userRepository;

    private static final List<DateTimeFormatter> BIRTHDAY_FORMATS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("d/M/yyyy"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("M/d/yyyy")
    );

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

    @Override
    @Transactional
    public void runScheduledIfDue() {
        BirthdayEmailSettings settings = ensure();
        if (!Boolean.TRUE.equals(settings.getActive())) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        int sendHour = settings.getSendHour() != null ? settings.getSendHour() : 8;
        if (now.getHour() != sendHour) {
            return;
        }

        LocalDate today = now.toLocalDate();
        if (settings.getLastRunAt() != null && settings.getLastRunAt().toLocalDate().equals(today)) {
            return;
        }

        int sent = 0;
        for (Profil profil : profilRepository.findAll()) {
            if (!isBirthdayToday(profil.getDateNaissance(), today)) {
                continue;
            }

            User user = resolveUser(profil);
            if (user == null || user.getEmail() == null || user.getEmail().isBlank()) {
                continue;
            }

            String displayName = profil.getPrenom() != null && !profil.getPrenom().isBlank()
                    ? profil.getPrenom()
                    : user.getNom();
            String html = settings.getTemplateHtml().replace("{{nom}}", displayName != null ? displayName : "Utilisateur");

            try {
                dispatch.sendHtml(user.getEmail(), null, settings.getSubject(), html);
                historyRepo.save(EmailHistory.builder()
                        .type(EmailHistoryType.BIRTHDAY_AUTO)
                        .campaign(null)
                        .toEmail(user.getEmail())
                        .subject(settings.getSubject())
                        .deliveryStatus(EmailDeliveryStatus.SUCCESS)
                        .errorMessage(null)
                        .sentAt(now)
                        .build());
                sent++;
            } catch (Exception ex) {
                log.error("Échec envoi anniversaire à {}: {}", user.getEmail(), ex.getMessage());
                historyRepo.save(EmailHistory.builder()
                        .type(EmailHistoryType.BIRTHDAY_AUTO)
                        .campaign(null)
                        .toEmail(user.getEmail())
                        .subject(settings.getSubject())
                        .deliveryStatus(EmailDeliveryStatus.FAILED)
                        .errorMessage(ex.getMessage())
                        .sentAt(now)
                        .build());
            }
        }

        settings.setLastRunAt(now);
        repo.save(settings);
        log.info("Scheduler anniversaire exécuté: {} email(s) envoyé(s)", sent);
    }

    private User resolveUser(Profil profil) {
        if (profil.getUserId() == null || profil.getUserId().isBlank()) {
            return null;
        }
        try {
            return userRepository.findById(UUID.fromString(profil.getUserId())).orElse(null);
        } catch (IllegalArgumentException ex) {
            return userRepository.findByEmail(profil.getUserId()).orElse(null);
        }
    }

    private boolean isBirthdayToday(String rawDate, LocalDate today) {
        if (rawDate == null || rawDate.isBlank()) {
            return false;
        }
        String value = rawDate.trim();
        for (DateTimeFormatter formatter : BIRTHDAY_FORMATS) {
            try {
                LocalDate parsed = LocalDate.parse(value, formatter);
                return parsed.getMonth() == today.getMonth() && parsed.getDayOfMonth() == today.getDayOfMonth();
            } catch (DateTimeParseException ignored) {
                // try next format
            }
        }
        return false;
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
