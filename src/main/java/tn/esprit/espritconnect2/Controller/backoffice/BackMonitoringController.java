package tn.esprit.espritconnect2.Controller.backoffice;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.espritconnect2.Config.ApiOfficePaths;
import tn.esprit.espritconnect2.DTO.PlatformMonitoringDTO;
import tn.esprit.espritconnect2.Service.IPlatformMonitoringService;

@RestController
@RequestMapping(ApiOfficePaths.BACK_MONITORING)
@RequiredArgsConstructor
public class BackMonitoringController {

    private final IPlatformMonitoringService platformMonitoringService;

    @GetMapping("/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PlatformMonitoringDTO> getStatus() {
        return ResponseEntity.ok(platformMonitoringService.getStatus());
    }

    @GetMapping("/health")
    public ResponseEntity<PlatformMonitoringDTO> health() {
        PlatformMonitoringDTO status = platformMonitoringService.getStatus();
        if ("DOWN".equals(status.getStatus())) {
            return ResponseEntity.status(503).body(status);
        }
        return ResponseEntity.ok(status);
    }
}
