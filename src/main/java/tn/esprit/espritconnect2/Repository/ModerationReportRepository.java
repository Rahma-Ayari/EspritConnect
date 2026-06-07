package tn.esprit.espritconnect2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.espritconnect2.Entitie.ModerationReport;
import tn.esprit.espritconnect2.Entitie.ModerationStatus;

import java.util.List;
import java.util.UUID;

public interface ModerationReportRepository extends JpaRepository<ModerationReport, Long> {
    List<ModerationReport> findByStatusOrderByCreatedAtDesc(ModerationStatus status);
    List<ModerationReport> findByReporterIdOrderByCreatedAtDesc(UUID reporterId);
    long countByStatus(ModerationStatus status);
}
