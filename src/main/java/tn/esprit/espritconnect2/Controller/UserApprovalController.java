package tn.esprit.espritconnect2.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.esprit.espritconnect2.DTO.ApprovalSettingsDTO;
import tn.esprit.espritconnect2.DTO.BulkApprovalRequest;
import tn.esprit.espritconnect2.DTO.UserApprovalDTO;
import tn.esprit.espritconnect2.DTO.UserApprovalStatsDTO;
import tn.esprit.espritconnect2.Entitie.Role;
import tn.esprit.espritconnect2.Service.ApprovalSettingsService;
import tn.esprit.espritconnect2.Service.IUserApprovalService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
@PreAuthorize("hasRole('ADMIN')")
public class UserApprovalController {

    private final IUserApprovalService userApprovalService;
    private final ApprovalSettingsService approvalSettingsService;

    @GetMapping("/pending")
    public ResponseEntity<List<UserApprovalDTO>> getPendingUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role) {
        
        Role roleEnum = null;
        if (role != null && !role.isEmpty()) {
            try {
                roleEnum = Role.valueOf(role.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid role, ignore filter
            }
        }
        
        List<UserApprovalDTO> users = userApprovalService.searchPendingUsers(search, roleEnum);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/all")
    public ResponseEntity<List<UserApprovalDTO>> getAllUsers() {
        return ResponseEntity.ok(userApprovalService.getAllUsers());
    }

    @GetMapping("/approved")
    public ResponseEntity<List<UserApprovalDTO>> getApprovedUsers() {
        return ResponseEntity.ok(userApprovalService.getApprovedUsers());
    }

    @GetMapping("/stats")
    public ResponseEntity<UserApprovalStatsDTO> getStats() {
        return ResponseEntity.ok(userApprovalService.getApprovalStats());
    }

    @PostMapping("/{userId}/approve")
    public ResponseEntity<UserApprovalDTO> approveUser(@PathVariable UUID userId) {
        UserApprovalDTO approved = userApprovalService.approveUser(userId);
        return ResponseEntity.ok(approved);
    }

    @PostMapping("/{userId}/decline")
    public ResponseEntity<Map<String, String>> declineUser(@PathVariable UUID userId) {
        userApprovalService.declineUser(userId);
        return ResponseEntity.ok(Map.of("message", "User declined successfully"));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable UUID userId) {
        userApprovalService.deleteUser(userId);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }

    @PostMapping("/bulk-approve")
    public ResponseEntity<List<UserApprovalDTO>> bulkApprove(@RequestBody BulkApprovalRequest request) {
        List<UserApprovalDTO> approved = userApprovalService.bulkApprove(request.getUserIds());
        return ResponseEntity.ok(approved);
    }

    @PostMapping("/bulk-decline")
    public ResponseEntity<Map<String, String>> bulkDecline(@RequestBody BulkApprovalRequest request) {
        userApprovalService.bulkDecline(request.getUserIds());
        return ResponseEntity.ok(Map.of("message", "Users declined successfully"));
    }

    @GetMapping("/settings")
    public ResponseEntity<ApprovalSettingsDTO> getSettings() {
        return ResponseEntity.ok(approvalSettingsService.getSettings());
    }

    @PutMapping("/settings")
    public ResponseEntity<ApprovalSettingsDTO> updateSettings(@RequestBody ApprovalSettingsDTO settings) {
        ApprovalSettingsDTO updated = approvalSettingsService.updateSettings(settings);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/settings/reset")
    public ResponseEntity<ApprovalSettingsDTO> resetSettings() {
        approvalSettingsService.resetToDefaults();
        return ResponseEntity.ok(approvalSettingsService.getSettings());
    }
}
