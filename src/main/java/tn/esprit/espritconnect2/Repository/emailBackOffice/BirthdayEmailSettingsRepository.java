package tn.esprit.espritconnect2.Repository.emailBackOffice;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.espritconnect2.Entitie.emailBackOffice.BirthdayEmailSettings;

public interface BirthdayEmailSettingsRepository extends JpaRepository<BirthdayEmailSettings, Long> {
}