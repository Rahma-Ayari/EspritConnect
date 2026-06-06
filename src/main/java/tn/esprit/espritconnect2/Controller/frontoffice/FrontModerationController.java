package tn.esprit.espritconnect2.Controller.frontoffice;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.espritconnect2.Config.ApiOfficePaths;
import tn.esprit.espritconnect2.DTO.ModerationReportDTO;
import tn.esprit.espritconnect2.DTO.ModerationReportRequestDTO;
import tn.esprit.espritconnect2.Service.IModerationService;
import tn.esprit.espritconnect2.security.SecurityUtils;

import java.util.List;
import java.util.UUID;

/**
 * Frontoffice — report inappropriate content.
 */
@RestController
@RequestMapping({ApiOfficePaths.FRONT_MODERATION, "/api/moderation"})
@RequiredArgsConstructor
public class FrontModerationController {

    private final IModerationService moderationService;

    @PostMapping("/reports")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ModerationReportDTO> submitReport(
            @RequestBody ModerationReportRequestDTO request,
            @RequestParam(required = false) UUID reporterId) {
        return new ResponseEntity<>(
                moderationService.submitReport(request, SecurityUtils.getCurrentUserIdOr(reporterId)),
                HttpStatus.CREATED);
    }

    @GetMapping("/reports/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ModerationReportDTO>> getMyReports(
            @RequestParam(required = false) UUID reporterId) {
        return ResponseEntity.ok(
                moderationService.getMyReports(SecurityUtils.getCurrentUserIdOr(reporterId)));
    }

    @GetMapping("/reports/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ModerationReportDTO> getMyReport(
            @PathVariable Long id,
            @RequestParam(required = false) UUID reporterId) {
        return ResponseEntity.ok(
                moderationService.getMyReport(id, SecurityUtils.getCurrentUserIdOr(reporterId)));
    }
}
