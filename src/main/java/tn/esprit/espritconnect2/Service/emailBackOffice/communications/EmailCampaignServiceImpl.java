package tn.esprit.espritconnect2.Service.emailBackOffice.communications;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.espritconnect2.DTO.emailBackOffice.communications.EmailCampaignRequestDTO;
import tn.esprit.espritconnect2.DTO.emailBackOffice.communications.EmailCampaignResponseDTO;
import tn.esprit.espritconnect2.Entitie.Etudiant;
import tn.esprit.espritconnect2.Entitie.User;
import tn.esprit.espritconnect2.Entitie.emailBackOffice.EmailCampaign;
import tn.esprit.espritconnect2.Entitie.emailBackOffice.EmailHistory;
import tn.esprit.espritconnect2.Entitie.emailBackOffice.MailingList;
import tn.esprit.espritconnect2.Entitie.emailBackOffice.MailingListMember;
import tn.esprit.espritconnect2.Entitie.emailBackOffice.enums.EmailCampaignStatus;
import tn.esprit.espritconnect2.Entitie.emailBackOffice.enums.EmailDeliveryStatus;
import tn.esprit.espritconnect2.Entitie.emailBackOffice.enums.EmailHistoryType;
import tn.esprit.espritconnect2.Entitie.emailBackOffice.enums.RecipientScope;
import tn.esprit.espritconnect2.Repository.EtudiantRepository;
import tn.esprit.espritconnect2.Repository.UserRepository;
import tn.esprit.espritconnect2.Repository.emailBackOffice.EmailCampaignRepository;
import tn.esprit.espritconnect2.Repository.emailBackOffice.EmailHistoryRepository;
import tn.esprit.espritconnect2.Repository.emailBackOffice.MailingListMemberRepository;
import tn.esprit.espritconnect2.Repository.emailBackOffice.MailingListRepository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmailCampaignServiceImpl implements IEmailCampaignService {

    private final EmailCampaignRepository campaignRepo;
    private final EmailHistoryRepository historyRepo;
    private final UserRepository userRepository;
    private final EtudiantRepository etudiantRepository;
    private final MailingListRepository mailingListRepository;
    private final MailingListMemberRepository memberRepository;
    private final EmailDispatchService dispatch;

    @Override
    @Transactional
    public EmailCampaignResponseDTO create(EmailCampaignRequestDTO dto) {
        validateMailingList(dto);
        EmailCampaign c = mapNew(dto);
        return toDto(campaignRepo.save(c));
    }

    @Override
    @Transactional
    public EmailCampaignResponseDTO update(Long id, EmailCampaignRequestDTO dto) {
        EmailCampaign c = campaignRepo.findById(id).orElseThrow(() -> new RuntimeException("Campagne introuvable"));
        if (c.getStatus() == EmailCampaignStatus.SENT) {
            throw new RuntimeException("Impossible de modifier une campagne déjà envoyée");
        }
        validateMailingList(dto);
        c.setSubject(dto.getSubject());
        c.setHtmlBody(dto.getHtmlBody());
        c.setFromEmail(dto.getFromEmail());
        c.setRecipientScope(dto.getRecipientScope());
        c.setMailingList(resolveList(dto));
        return toDto(campaignRepo.save(c));
    }

    @Override
    @Transactional(readOnly = true)
    public EmailCampaignResponseDTO get(Long id) {
        return toDto(campaignRepo.findById(id).orElseThrow(() -> new RuntimeException("Campagne introuvable")));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmailCampaignResponseDTO> list() {
        return campaignRepo.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * Envoie la campagne :
     * 1) calcule la liste des emails destinataires
     * 2) envoie un par un
     * 3) écrit l'historique (succès / échec)
     * 4) met la campagne en SENT
     */
    @Override
    @Transactional
    public void send(Long id) {
        EmailCampaign c = campaignRepo.findById(id).orElseThrow(() -> new RuntimeException("Campagne introuvable"));
        if (c.getStatus() == EmailCampaignStatus.SENT) {
            throw new RuntimeException("Campagne déjà envoyée");
        }

        c.setStatus(EmailCampaignStatus.SENDING);
        campaignRepo.save(c);

        Set<String> recipients = resolveRecipients(c);

        for (String email : recipients) {
            try {
                String html = buildBrandedCampaignHtml(c.getSubject(), c.getHtmlBody());
                dispatch.sendHtml(email, c.getFromEmail(), c.getSubject(), html);
                historyRepo.save(EmailHistory.builder()
                        .type(EmailHistoryType.MESSAGE_USERS_CAMPAIGN)
                        .campaign(c)
                        .toEmail(email)
                        .subject(c.getSubject())
                        .deliveryStatus(EmailDeliveryStatus.SUCCESS)
                        .errorMessage(null)
                        .sentAt(LocalDateTime.now())
                        .build());
            } catch (Exception ex) {
                historyRepo.save(EmailHistory.builder()
                        .type(EmailHistoryType.MESSAGE_USERS_CAMPAIGN)
                        .campaign(c)
                        .toEmail(email)
                        .subject(c.getSubject())
                        .deliveryStatus(EmailDeliveryStatus.FAILED)
                        .errorMessage(ex.getMessage())
                        .sentAt(LocalDateTime.now())
                        .build());
            }
        }

        c.setStatus(EmailCampaignStatus.SENT);
        c.setSentAt(LocalDateTime.now());
        campaignRepo.save(c);
    }

    @Override
    @Transactional
    public void sendNow(EmailCampaignRequestDTO dto) {
        validateMailingList(dto);
        Set<String> recipients = resolveRecipients(dto);

        for (String email : recipients) {
            try {
                String html = buildBrandedCampaignHtml(dto.getSubject(), dto.getHtmlBody());
                dispatch.sendHtml(email, dto.getFromEmail(), dto.getSubject(), html);
                historyRepo.save(EmailHistory.builder()
                        .type(EmailHistoryType.MESSAGE_USERS_CAMPAIGN)
                        .campaign(null)
                        .toEmail(email)
                        .subject(dto.getSubject())
                        .deliveryStatus(EmailDeliveryStatus.SUCCESS)
                        .errorMessage(null)
                        .sentAt(LocalDateTime.now())
                        .build());
            } catch (Exception ex) {
                historyRepo.save(EmailHistory.builder()
                        .type(EmailHistoryType.MESSAGE_USERS_CAMPAIGN)
                        .campaign(null)
                        .toEmail(email)
                        .subject(dto.getSubject())
                        .deliveryStatus(EmailDeliveryStatus.FAILED)
                        .errorMessage(ex.getMessage())
                        .sentAt(LocalDateTime.now())
                        .build());
            }
        }
    }

    private void validateMailingList(EmailCampaignRequestDTO dto) {
        if (dto.getRecipientScope() == RecipientScope.MAILING_LIST) {
            if (dto.getMailingListId() == null) {
                throw new RuntimeException("mailingListId est obligatoire pour MAILING_LIST");
            }
            mailingListRepository.findById(dto.getMailingListId())
                    .orElseThrow(() -> new RuntimeException("Mailing list introuvable"));
        }
    }

    private EmailCampaign mapNew(EmailCampaignRequestDTO dto) {
        EmailCampaign c = new EmailCampaign();
        c.setSubject(dto.getSubject());
        c.setHtmlBody(dto.getHtmlBody());
        c.setFromEmail(dto.getFromEmail());
        c.setRecipientScope(dto.getRecipientScope());
        c.setMailingList(resolveList(dto));
        c.setStatus(EmailCampaignStatus.DRAFT);
        return c;
    }

    private MailingList resolveList(EmailCampaignRequestDTO dto) {
        if (dto.getRecipientScope() != RecipientScope.MAILING_LIST) return null;
        return mailingListRepository.findById(dto.getMailingListId()).orElse(null);
    }

    private Set<String> resolveRecipients(EmailCampaignRequestDTO dto) {
        Set<String> emails = new LinkedHashSet<>();

        switch (dto.getRecipientScope()) {
            case ALL_ENABLED_USERS -> {
                for (User u : userRepository.findAll()) {
                    if (u.isEnabled() && u.getEmail() != null && !u.getEmail().isBlank()) {
                        emails.add(u.getEmail().trim().toLowerCase());
                    }
                }
            }
            case ALL_STUDENTS -> {
                for (Etudiant e : etudiantRepository.findAll()) {
                    if (e.getEmail() != null && !e.getEmail().isBlank()) {
                        emails.add(e.getEmail().trim().toLowerCase());
                    }
                }
            }
            case MAILING_LIST -> {
                if (dto.getMailingListId() == null) {
                    throw new RuntimeException("mailingListId est obligatoire pour MAILING_LIST");
                }

                List<MailingListMember> members = memberRepository.findByMailingList_Id(dto.getMailingListId());
                Set<String> listEmails = members.stream()
                        .map(MailingListMember::getEmail)
                        .filter(e -> e != null && !e.isBlank())
                        .map(e -> e.trim().toLowerCase())
                        .collect(Collectors.toCollection(LinkedHashSet::new));

                if (dto.getRecipientEmails() != null && !dto.getRecipientEmails().isEmpty()) {
                    Set<String> selected = dto.getRecipientEmails().stream()
                            .filter(e -> e != null && !e.isBlank())
                            .map(e -> e.trim().toLowerCase())
                            .collect(Collectors.toCollection(HashSet::new));

                    listEmails.removeIf(e -> !selected.contains(e));
                    if (listEmails.isEmpty()) {
                        throw new RuntimeException("Aucun destinataire valide sélectionné dans cette mailing list");
                    }
                }

                emails.addAll(listEmails);
            }
        }

        if (emails.isEmpty()) {
            throw new RuntimeException("Aucun destinataire trouvé pour cette campagne");
        }
        return emails;
    }

    /**
     * Calcule les destinataires sans doublons (LinkedHashSet).
     */
    private Set<String> resolveRecipients(EmailCampaign c) {
        Set<String> emails = new LinkedHashSet<>();

        switch (c.getRecipientScope()) {
            case ALL_ENABLED_USERS -> {
                for (User u : userRepository.findAll()) {
                    if (u.isEnabled()) {
                        emails.add(u.getEmail());
                    }
                }
            }
            case ALL_STUDENTS -> {
                for (Etudiant e : etudiantRepository.findAll()) {
                    if (e.getEmail() != null && !e.getEmail().isBlank()) {
                        emails.add(e.getEmail());
                    }
                }
            }
            case MAILING_LIST -> {
                if (c.getMailingList() == null) {
                    throw new RuntimeException("Campagne invalide : mailing list manquante");
                }
                List<MailingListMember> members = memberRepository.findByMailingList_Id(c.getMailingList().getId());
                for (MailingListMember m : members) {
                    emails.add(m.getEmail());
                }
            }
        }
        return emails;
    }

    /**
     * Wraps the admin HTML inside the same branded container/footer used by Activity Digest.
     */
    private String buildBrandedCampaignHtml(String subject, String adminHtml) {
        String safeSubject = (subject == null || subject.isBlank()) ? "Message Esprit Connect" : subject;
        String body = (adminHtml == null) ? "" : adminHtml;

        String bannerHtml =
                "<div style='background:linear-gradient(135deg, #dc2626 0%, #b91c1c 100%); padding:40px 24px; text-align:center; color:#ffffff;'>"
                        + "<div style='font-size:32px; font-weight:800; letter-spacing:1px; margin:0; font-family:Arial, sans-serif;'>ESPRIT<span style='color:#ffd2d2;'>Connect</span></div>"
                        + "<div style='font-size:14px; opacity:0.85; margin-top:6px; font-family:Arial, sans-serif;'>Se former autrement</div>"
                        + "</div>";

        // Important: don't use String.format / formatted because the body can contain '%' from CSS.
        String template = """
            <!DOCTYPE html>
            <html lang="fr">
            <head>
              <meta charset="UTF-8"/>
              <meta name="viewport" content="width=device-width, initial-scale=1"/>
              <title>Esprit Connect - Message</title>
            </head>
            <body style="margin:0; padding:0; background-color:#f3f4f6; -webkit-font-smoothing:antialiased;">
              <div style="width:100%; max-width:600px; margin:20px auto; background-color:#ffffff; border-radius:12px; overflow:hidden; box-shadow:0 4px 20px rgba(0,0,0,0.05); border:1px solid #e5e7eb; font-family:-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;">
                
                <!-- En-tête / Bannière -->
                %s
                
                <!-- Corps de l'email -->
                <div style="padding:32px 32px 10px;">
                  <h2 style="margin:0; color:#111827; font-size:20px; font-weight:700;">%s</h2>
                </div>
                
                <!-- Contenu admin -->
                <div style="padding:0 32px 32px; color:#374151; font-size:14px; line-height:1.6;">
                  %s
                </div>
                
                <!-- Pied de page -->
                <div style="background:linear-gradient(135deg, #dc2626 0%, #b91c1c 100%); color:#ffe4e6; padding:24px 24px; font-size:12px; text-align:center; font-family:Arial, sans-serif; border-top:1px solid #fecaca;">
                  <div style="font-weight:600; color:#ffffff; margin-bottom:6px;">ESPRIT Connect</div>
                  <div style="margin-bottom:12px; opacity:0.8;">Vous recevez cet email car vous êtes inscrit sur la plateforme ESPRIT Connect.</div>
                  <div style="border-top:1px solid rgba(255,255,255,0.25); padding-top:12px; opacity:0.9;">
                    © 2026 ESPRIT — Honoris United Universities. Tous droits réservés.
                  </div>
                </div>
                
              </div>
            </body>
            </html>
            """;

        return template
                .replace("%s", "%%s")
                .replaceFirst("%%s", java.util.regex.Matcher.quoteReplacement(bannerHtml))
                .replaceFirst("%%s", java.util.regex.Matcher.quoteReplacement(escapeHtml(safeSubject)))
                .replaceFirst("%%s", java.util.regex.Matcher.quoteReplacement(body));
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private EmailCampaignResponseDTO toDto(EmailCampaign c) {
        return EmailCampaignResponseDTO.builder()
                .id(c.getId())
                .subject(c.getSubject())
                .htmlBody(c.getHtmlBody())
                .fromEmail(c.getFromEmail())
                .status(c.getStatus())
                .recipientScope(c.getRecipientScope())
                .mailingListId(c.getMailingList() != null ? c.getMailingList().getId() : null)
                .createdAt(c.getCreatedAt() != null ? c.getCreatedAt().toString() : null)
                .updatedAt(c.getUpdatedAt() != null ? c.getUpdatedAt().toString() : null)
                .sentAt(c.getSentAt() != null ? c.getSentAt().toString() : null)
                .build();
    }
}