package tn.esprit.espritconnect2.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import tn.esprit.espritconnect2.DTO.ActivityLogDto;
import tn.esprit.espritconnect2.DTO.UserActivityInfoDTO;
import tn.esprit.espritconnect2.Entitie.ActivityLog;
import tn.esprit.espritconnect2.Entitie.User;
import tn.esprit.espritconnect2.Repository.ActivityLogRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ActivityLogService {

    @Autowired
    private ActivityLogRepository activityLogRepository;

    @Autowired
    private tn.esprit.espritconnect2.Repository.UserRepository userRepository;

    @Autowired
    private LoginHistoryService loginHistoryService;

    public ActivityLog logActivity(UUID userId, String username, String action, String entity, String entityId, String description, String ipAddress, String browser) {
        
        // Exclude routine Admin activity (like their own logins or profile updates) from being logged, 
        // but KEEP important administrative actions like DELETE_USER so they appear in the dashboard.
        if (userId != null) {
            java.util.Optional<tn.esprit.espritconnect2.Entitie.User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent() && userOpt.get().getRole() == tn.esprit.espritconnect2.Entitie.Role.ADMIN) {
                if ("LOGIN".equals(action) || "UPDATE_USER".equals(action)) {
                    return null;
                }
            }
        }

        ActivityLog log = ActivityLog.builder()
                .userId(userId)
                .username(username)
                .action(action)
                .entity(entity)
                .entityId(entityId)
                .description(description)
                .ipAddress(ipAddress)
                .browser(browser)
                .createdAt(LocalDateTime.now())
                .build();
        return activityLogRepository.save(log);
    }

    public Page<ActivityLogDto> getAllActivities(Pageable pageable) {
        return activityLogRepository.findAll(pageable).map(this::mapToDto);
    }

    public Page<ActivityLogDto> getUserActivities(UUID userId, Pageable pageable) {
        return activityLogRepository.findByUserId(userId, pageable).map(this::mapToDto);
    }

    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalLogins", activityLogRepository.countByAction("LOGIN"));
        stats.put("totalUpdates", activityLogRepository.countByAction("UPDATE_USER"));
        stats.put("totalDeletes", activityLogRepository.countByAction("DELETE_USER"));
        stats.put("totalActivities", activityLogRepository.count());

        // Last Login
        ActivityLog lastLogin = activityLogRepository.findFirstByActionOrderByCreatedAtDesc("LOGIN");
        if (lastLogin != null) {
            Map<String, Object> lastLoginMap = new HashMap<>();
            lastLoginMap.put("username", lastLogin.getUsername());
            lastLoginMap.put("time", lastLogin.getCreatedAt());
            lastLoginMap.put("ipAddress", lastLogin.getIpAddress());
            stats.put("lastLogin", lastLoginMap);
        }

        // Last Update
        ActivityLog lastUpdate = activityLogRepository.findFirstByActionInOrderByCreatedAtDesc(
                List.of("UPDATE_USER", "UPDATE_PROJECT", "CHANGE_PASSWORD"));
        if (lastUpdate != null) {
            Map<String, Object> lastUpdateMap = new HashMap<>();
            lastUpdateMap.put("username", lastUpdate.getUsername());
            lastUpdateMap.put("action", lastUpdate.getAction());
            lastUpdateMap.put("time", lastUpdate.getCreatedAt());
            lastUpdateMap.put("description", lastUpdate.getDescription());
            stats.put("lastUpdate", lastUpdateMap);
        }

        return stats;
    }

    public ActivityLogDto getLastLogin(UUID userId) {
        ActivityLog log = activityLogRepository.findFirstByUserIdAndActionOrderByCreatedAtDesc(userId, "LOGIN");
        return log != null ? mapToDto(log) : null;
    }

    public ActivityLogDto getLastProfileUpdate(UUID userId) {
        ActivityLog log = activityLogRepository.findFirstByUserIdAndActionOrderByCreatedAtDesc(userId, "UPDATE_USER");
        return log != null ? mapToDto(log) : null;
    }

    public UserActivityInfoDTO getUserActivityInfo(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        UserActivityInfoDTO.UserActivityInfoDTOBuilder builder = UserActivityInfoDTO.builder();

        loginHistoryService.getLastSuccessfulLogin(user).ifPresentOrElse(
                login -> builder
                        .lastLoginAt(login.getLoginTime())
                        .lastLoginIp(login.getIpAddress())
                        .lastLoginBrowser(login.getBrowser()),
                () -> {
                    ActivityLogDto lastLogin = getLastLogin(user.getId());
                    if (lastLogin != null) {
                        builder
                                .lastLoginAt(lastLogin.getCreatedAt())
                                .lastLoginIp(lastLogin.getIpAddress())
                                .lastLoginBrowser(lastLogin.getBrowser());
                    }
                });

        ActivityLogDto lastUpdate = getLastProfileUpdate(user.getId());
        if (lastUpdate != null) {
            builder
                    .lastProfileUpdateAt(lastUpdate.getCreatedAt())
                    .lastProfileUpdateDescription(lastUpdate.getDescription());
        }

        return builder.build();
    }

    public List<ActivityLogDto> getRecentActivities() {
        return activityLogRepository.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"))
                .stream().limit(50).map(this::mapToDto).collect(Collectors.toList());
    }

    private ActivityLogDto mapToDto(ActivityLog entity) {
        ActivityLogDto dto = new ActivityLogDto();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUserId());
        dto.setUsername(entity.getUsername());
        dto.setAction(entity.getAction());
        dto.setEntity(entity.getEntity());
        dto.setEntityId(entity.getEntityId());
        dto.setDescription(entity.getDescription());
        dto.setIpAddress(entity.getIpAddress());
        dto.setBrowser(entity.getBrowser());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}
