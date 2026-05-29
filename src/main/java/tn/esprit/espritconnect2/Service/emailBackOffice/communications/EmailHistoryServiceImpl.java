package tn.esprit.espritconnect2.Service.emailBackOffice.communications;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.espritconnect2.DTO.emailBackOffice.communications.EmailHistoryResponseDTO;
import tn.esprit.espritconnect2.Entitie.emailBackOffice.EmailHistory;
import tn.esprit.espritconnect2.Entitie.emailBackOffice.enums.EmailDeliveryStatus;
import tn.esprit.espritconnect2.Entitie.emailBackOffice.enums.EmailHistoryType;
import tn.esprit.espritconnect2.Repository.emailBackOffice.EmailHistoryRepository;

@Service
@RequiredArgsConstructor
public class EmailHistoryServiceImpl implements IEmailHistoryService {

    private final EmailHistoryRepository repo;

    @Override
    @Transactional(readOnly = true)
    public Page<EmailHistoryResponseDTO> search(String q, EmailHistoryType type, Pageable pageable) {
        Page<EmailHistory> page;
        if (type != null && q != null && !q.isBlank()) {
            page = repo.findByTypeAndToEmailContainingIgnoreCase(type, q, pageable);
        } else if (type != null) {
            page = repo.findByType(type, pageable);
        } else if (q != null && !q.isBlank()) {
            page = repo.findByToEmailContainingIgnoreCase(q, pageable);
        } else {
            page = repo.findAll(pageable);
        }
        return page.map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByStatus(String q, EmailHistoryType type, EmailDeliveryStatus status) {
        if (type != null && q != null && !q.isBlank()) {
            return repo.countByTypeAndToEmailContainingIgnoreCaseAndDeliveryStatus(type, q, status);
        }
        if (type != null) {
            return repo.countByTypeAndDeliveryStatus(type, status);
        }
        if (q != null && !q.isBlank()) {
            return repo.countByToEmailContainingIgnoreCaseAndDeliveryStatus(q, status);
        }
        return repo.countByDeliveryStatus(status);
    }

    private EmailHistoryResponseDTO toDto(EmailHistory h) {
        return EmailHistoryResponseDTO.builder()
                .id(h.getId())
                .type(h.getType())
                .campaignId(h.getCampaign() != null ? h.getCampaign().getId() : null)
                .toEmail(h.getToEmail())
                .subject(h.getSubject())
                .deliveryStatus(h.getDeliveryStatus())
                .errorMessage(h.getErrorMessage())
                .sentAt(h.getSentAt() != null ? h.getSentAt().toString() : null)
                .build();
    }
}