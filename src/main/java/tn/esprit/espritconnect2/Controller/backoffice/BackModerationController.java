package tn.esprit.espritconnect2.Controller.backoffice;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.espritconnect2.Config.ApiOfficePaths;
import tn.esprit.espritconnect2.DTO.ModerationReportDTO;
import tn.esprit.espritconnect2.DTO.ModerationReviewRequestDTO;
import tn.esprit.espritconnect2.Entitie.ModerationStatus;
import tn.esprit.espritconnect2.Service.IModerationService;
import tn.esprit.espritconnect2.security.SecurityUtils;

import java.util.List;
import java.util.UUID;

/**
 * Backoffice — review and resolve content reports.
 */
@RestController
@RequestMapping(ApiOfficePaths.BACK_MODERATION)
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class BackModerationController {

    private final IModerationService moderationService;

    @GetMapping("/reports")
    public ResponseEntity<List<ModerationReportDTO>> getAllReports() {
        return ResponseEntity.ok(moderationService.getAllReports());
    }

    @GetMapping("/reports/status/{status}")
    public ResponseEntity<List<ModerationReportDTO>> getByStatus(@PathVariable ModerationStatus status) {
        return ResponseEntity.ok(moderationService.getReportsByStatus(status));
    }

    @GetMapping("/reports/{id}")
    public ResponseEntity<ModerationReportDTO> getReport(@PathVariable Long id) {
        return ResponseEntity.ok(moderationService.getReport(id));
    }

    @PatchMapping("/reports/{id}/review")
    public ResponseEntity<ModerationReportDTO> reviewReport(
            @PathVariable Long id,
            @RequestBody ModerationReviewRequestDTO review) {
        return ResponseEntity.ok(moderationService.reviewReport(id, review, SecurityUtils.getCurrentUserId()));
    }
}
