package tn.esprit.espritconnect2.Service;

import tn.esprit.espritconnect2.DTO.ModerationReportDTO;
import tn.esprit.espritconnect2.DTO.ModerationReportRequestDTO;
import tn.esprit.espritconnect2.DTO.ModerationReviewRequestDTO;
import tn.esprit.espritconnect2.Entitie.ModerationStatus;

import java.util.List;
import java.util.UUID;

public interface IModerationService {
    ModerationReportDTO submitReport(ModerationReportRequestDTO request, UUID reporterId);
    List<ModerationReportDTO> getMyReports(UUID reporterId);
    List<ModerationReportDTO> getAllReports();
    List<ModerationReportDTO> getReportsByStatus(ModerationStatus status);
    ModerationReportDTO getReport(Long id);
    ModerationReportDTO getMyReport(Long id, UUID reporterId);
    ModerationReportDTO reviewReport(Long id, ModerationReviewRequestDTO review, UUID moderatorId);
}
