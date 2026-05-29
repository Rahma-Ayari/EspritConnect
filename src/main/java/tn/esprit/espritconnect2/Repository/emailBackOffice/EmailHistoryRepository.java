package tn.esprit.espritconnect2.Repository.emailBackOffice;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.espritconnect2.Entitie.emailBackOffice.EmailHistory;
import tn.esprit.espritconnect2.Entitie.emailBackOffice.enums.EmailDeliveryStatus;
import tn.esprit.espritconnect2.Entitie.emailBackOffice.enums.EmailHistoryType;

public interface EmailHistoryRepository extends JpaRepository<EmailHistory, Long> {

    Page<EmailHistory> findByType(EmailHistoryType type, Pageable pageable);

    Page<EmailHistory> findByToEmailContainingIgnoreCase(String q, Pageable pageable);

    Page<EmailHistory> findByTypeAndToEmailContainingIgnoreCase(EmailHistoryType type, String q, Pageable pageable);

    long countByDeliveryStatus(EmailDeliveryStatus status);

    long countByTypeAndDeliveryStatus(EmailHistoryType type, EmailDeliveryStatus status);

    long countByToEmailContainingIgnoreCaseAndDeliveryStatus(String q, EmailDeliveryStatus status);

    long countByTypeAndToEmailContainingIgnoreCaseAndDeliveryStatus(EmailHistoryType type, String q, EmailDeliveryStatus status);
}