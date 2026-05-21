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
                dispatch.sendHtml(email, c.getFromEmail(), c.getSubject(), c.getHtmlBody());
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