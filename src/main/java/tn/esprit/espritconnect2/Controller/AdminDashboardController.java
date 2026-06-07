package tn.esprit.espritconnect2.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.espritconnect2.DTO.AdminDashboardResponseDTO;
import tn.esprit.espritconnect2.Service.AdminDashboardService;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping
    public ResponseEntity<AdminDashboardResponseDTO> getDashboardData() {
        return ResponseEntity.ok(adminDashboardService.getDashboardData());
    }

    @GetMapping("/approvals")
    public ResponseEntity<java.util.List<AdminDashboardResponseDTO.PendingApprovalItem>> getPendingApprovals(
            @RequestParam(defaultValue = "3") int limit
    ) {
        return ResponseEntity.ok(adminDashboardService.getPendingApprovals(limit));
    }

    @PostMapping("/approvals/{companyId}/approve")
    public ResponseEntity<Void> approveCompany(@PathVariable Long companyId) {
        adminDashboardService.approveCompany(companyId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/approvals/{companyId}/decline")
    public ResponseEntity<Void> declineCompany(@PathVariable Long companyId) {
        adminDashboardService.declineCompany(companyId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/approvals/user/{userId}/approve")
    public ResponseEntity<Void> approveUser(@PathVariable UUID userId) {
        adminDashboardService.approveUser(userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/approvals/user/{userId}/decline")
    public ResponseEntity<Void> declineUser(@PathVariable UUID userId) {
        adminDashboardService.declineUser(userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
