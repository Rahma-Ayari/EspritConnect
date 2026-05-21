package tn.esprit.espritconnect2.Repository.emailBackOffice;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.espritconnect2.Entitie.emailBackOffice.EmailCampaign;

public interface EmailCampaignRepository extends JpaRepository<EmailCampaign, Long> {
}