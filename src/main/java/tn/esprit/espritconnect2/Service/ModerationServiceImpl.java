package tn.esprit.espritconnect2.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.espritconnect2.DTO.ModerationReportDTO;
import tn.esprit.espritconnect2.DTO.ModerationReportRequestDTO;
import tn.esprit.espritconnect2.DTO.ModerationReviewRequestDTO;
import tn.esprit.espritconnect2.Entitie.*;
import tn.esprit.espritconnect2.exception.NotFoundException;
import tn.esprit.espritconnect2.Repository.ModerationReportRepository;
import tn.esprit.espritconnect2.Repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ModerationServiceImpl implements IModerationService {

    private final ModerationReportRepository moderationReportRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ModerationReportDTO submitReport(ModerationReportRequestDTO request, UUID reporterId) {
        String fallbackEmail = "test.reporter." + reporterId.toString().substring(0, 8) + "@esprit.tn";
        User reporter = userRepository.findById(reporterId)
                .or(() -> userRepository.findByEmail(fallbackEmail))
                .orElseGet(() -> {
                    log.info("Reporter not found by ID {}, creating fallback user.", reporterId);
                    User newUser = User.builder()
                            .id(reporterId)
                            .nom("Student User")
                            .email(fallbackEmail)
                            .password("password")
                            .role(Role.ETUDIANT)
                            .enabled(true)
                            .build();
                    try {
                        return userRepository.save(newUser);
                    } catch (Exception e) {
                        log.error("Failed to create fallback reporter user: {}", e.getMessage());
                        throw e;
                    }
                });

        ModerationReport report = ModerationReport.builder()
                .contentType(request.getContentType())
                .contentRefId(request.getContentRefId())
                .reason(request.getReason())
                .status(ModerationStatus.PENDING)
                .actionTaken(ModerationAction.NONE)
                .reporter(reporter)
                .build();

        return mapToDTO(moderationReportRepository.save(report));
    }

    @Override
    public List<ModerationReportDTO> getMyReports(UUID reporterId) {
        return moderationReportRepository.findByReporterIdOrderByCreatedAtDesc(reporterId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ModerationReportDTO> getAllReports() {
        return moderationReportRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ModerationReportDTO> getReportsByStatus(ModerationStatus status) {
        return moderationReportRepository.findByStatusOrderByCreatedAtDesc(status).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ModerationReportDTO getReport(Long id) {
        return mapToDTO(moderationReportRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Moderation report not found")));
    }

    @Override
    public ModerationReportDTO getMyReport(Long id, UUID reporterId) {
        ModerationReport report = moderationReportRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Moderation report not found"));
        if (report.getReporter() == null || !report.getReporter().getId().equals(reporterId)) {
            throw new tn.esprit.espritconnect2.exception.BusinessRuleException("You can only view your own reports.");
        }
        return mapToDTO(report);
    }

    @Override
    @Transactional
    public ModerationReportDTO reviewReport(Long id, ModerationReviewRequestDTO review, UUID moderatorId) {
        ModerationReport report = moderationReportRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Moderation report not found"));
        User moderator = userRepository.findById(moderatorId)
                .orElseThrow(() -> new NotFoundException("Moderator not found"));

        if (review.getStatus() != null) {
            report.setStatus(review.getStatus());
        }
        if (review.getActionTaken() != null) {
            report.setActionTaken(review.getActionTaken());
        }
        if (review.getModeratorNotes() != null) {
            report.setModeratorNotes(review.getModeratorNotes());
        }
        if (review.getLinkedTicketId() != null) {
            report.setLinkedTicketId(review.getLinkedTicketId());
        }
        report.setModerator(moderator);

        if (report.getStatus() == ModerationStatus.RESOLVED || report.getStatus() == ModerationStatus.DISMISSED) {
            report.setResolvedAt(LocalDateTime.now());
        }

        return mapToDTO(moderationReportRepository.save(report));
    }

    private ModerationReportDTO mapToDTO(ModerationReport report) {
        return ModerationReportDTO.builder()
                .id(report.getId())
                .contentType(report.getContentType())
                .contentRefId(report.getContentRefId())
                .reason(report.getReason())
                .status(report.getStatus())
                .actionTaken(report.getActionTaken())
                .reporterId(report.getReporter() != null ? report.getReporter().getId() : null)
                .reporterName(report.getReporter() != null ? report.getReporter().getNom() : null)
                .moderatorId(report.getModerator() != null ? report.getModerator().getId() : null)
                .moderatorName(report.getModerator() != null ? report.getModerator().getNom() : null)
                .moderatorNotes(report.getModeratorNotes())
                .linkedTicketId(report.getLinkedTicketId())
                .createdAt(report.getCreatedAt())
                .resolvedAt(report.getResolvedAt())
                .build();
    }
}
