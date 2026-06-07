package tn.esprit.espritconnect2.Repository.emailBackOffice;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.espritconnect2.Entitie.emailBackOffice.MailingList;

public interface MailingListRepository extends JpaRepository<MailingList, Long> {
    boolean existsByNameIgnoreCase(String name);
}