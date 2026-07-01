package tn.esprit.espritconnect2.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.espritconnect2.DTO.ActivityLogDto;
import tn.esprit.espritconnect2.DTO.AIAnalysisResponse;
import tn.esprit.espritconnect2.DTO.UserActivityInfoDTO;
import tn.esprit.espritconnect2.Service.ActivityLogService;
import tn.esprit.espritconnect2.Service.GeminiService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/activity")
@CrossOrigin(origins = "*") // Adjust if needed
public class ActivityLogController {

    @Autowired
    private ActivityLogService activityLogService;

    @Autowired
    private GeminiService geminiService;

    @GetMapping
    public ResponseEntity<Page<ActivityLogDto>> getAllActivities(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        return ResponseEntity.ok(activityLogService.getAllActivities(PageRequest.of(page, size, sort)));
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<Page<ActivityLogDto>> getUserActivities(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(activityLogService.getUserActivities(id, PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        return ResponseEntity.ok(activityLogService.getStatistics());
    }

    @GetMapping("/last-login/{id}")
    public ResponseEntity<ActivityLogDto> getLastLogin(@PathVariable UUID id) {
        ActivityLogDto lastLogin = activityLogService.getLastLogin(id);
        if (lastLogin == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(lastLogin);
    }

    @GetMapping("/user-info/email/{email}")
    public ResponseEntity<UserActivityInfoDTO> getUserActivityInfo(@PathVariable String email) {
        return ResponseEntity.ok(activityLogService.getUserActivityInfo(email));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<ActivityLogDto>> getRecentActivities() {
        return ResponseEntity.ok(activityLogService.getRecentActivities());
    }

    @PostMapping("/analyze")
    public ResponseEntity<AIAnalysisResponse> analyzeActivities() {
        List<ActivityLogDto> recentActivities = activityLogService.getRecentActivities();
        return ResponseEntity.ok(geminiService.analyzeActivities(recentActivities));
    }

    @PostMapping("/seed-demo")
    public ResponseEntity<Map<String, Object>> seedDemoData() {
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();
        UUID user3 = UUID.randomUUID();

        // Simulate realistic activity logs
        activityLogService.logActivity(user1, "rahma.ayari@esprit.tn", "LOGIN", "User", user1.toString(), "User logged in successfully", "192.168.1.10", "Chrome/126.0");
        activityLogService.logActivity(user1, "rahma.ayari@esprit.tn", "LOGIN", "User", user1.toString(), "User logged in successfully", "192.168.1.10", "Chrome/126.0");
        activityLogService.logActivity(user1, "rahma.ayari@esprit.tn", "UPDATE_USER", "User", user1.toString(), "Updated profile information", "192.168.1.10", "Chrome/126.0");
        activityLogService.logActivity(user1, "rahma.ayari@esprit.tn", "CHANGE_PASSWORD", "User", user1.toString(), "Password changed successfully", "192.168.1.10", "Chrome/126.0");

        activityLogService.logActivity(user2, "admin@esprit.tn", "LOGIN", "User", user2.toString(), "Admin logged in", "10.0.0.5", "Firefox/128.0");
        activityLogService.logActivity(user2, "admin@esprit.tn", "CREATE_USER", "User", UUID.randomUUID().toString(), "Created new student account", "10.0.0.5", "Firefox/128.0");
        activityLogService.logActivity(user2, "admin@esprit.tn", "DELETE_USER", "User", UUID.randomUUID().toString(), "Deleted inactive user account", "10.0.0.5", "Firefox/128.0");
        activityLogService.logActivity(user2, "admin@esprit.tn", "UPDATE_PROJECT", "Project", UUID.randomUUID().toString(), "Updated project settings", "10.0.0.5", "Firefox/128.0");
        activityLogService.logActivity(user2, "admin@esprit.tn", "DELETE_USER", "User", UUID.randomUUID().toString(), "Deleted spam account", "10.0.0.5", "Firefox/128.0");
        activityLogService.logActivity(user2, "admin@esprit.tn", "CREATE_PROJECT", "Project", UUID.randomUUID().toString(), "Created new PFA project", "10.0.0.5", "Firefox/128.0");

        activityLogService.logActivity(user3, "suspicious.user@external.com", "LOGIN", "User", user3.toString(), "Login from unusual IP", "203.0.113.42", "curl/7.88");
        activityLogService.logActivity(user3, "suspicious.user@external.com", "LOGIN", "User", user3.toString(), "Login from unusual IP", "198.51.100.77", "Python-urllib/3.11");
        activityLogService.logActivity(user3, "suspicious.user@external.com", "LOGIN", "User", user3.toString(), "Multiple rapid login attempts", "203.0.113.42", "curl/7.88");
        activityLogService.logActivity(user3, "suspicious.user@external.com", "DELETE_USER", "User", UUID.randomUUID().toString(), "Attempted to delete admin account", "203.0.113.42", "curl/7.88");
        activityLogService.logActivity(user3, "suspicious.user@external.com", "CHANGE_PASSWORD", "User", user1.toString(), "Changed another user's password", "203.0.113.42", "curl/7.88");

        return ResponseEntity.ok(Map.of("message", "Demo data seeded successfully", "count", 15));
    }
}
