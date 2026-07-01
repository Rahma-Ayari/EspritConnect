package tn.esprit.espritconnect2.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tn.esprit.espritconnect2.Entitie.User;
import tn.esprit.espritconnect2.Repository.UserRepository;
import tn.esprit.espritconnect2.Service.ActivityLogService;
import tn.esprit.espritconnect2.annotation.TrackActivity;

import java.lang.reflect.Method;
import java.util.Optional;

@Aspect
@Component
@RequiredArgsConstructor
public class ActivityLoggingAspect {

    private final ActivityLogService activityLogService;
    private final UserRepository userRepository;

    @AfterReturning("@annotation(tn.esprit.espritconnect2.annotation.TrackActivity)")
    public void logActivity(JoinPoint joinPoint) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            TrackActivity trackActivity = method.getAnnotation(TrackActivity.class);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
                return; // Do not log if user is not authenticated
            }

            Optional<User> userOpt = userRepository.findByEmail(authentication.getName());
            if (userOpt.isEmpty()) {
                return;
            }
            User user = userOpt.get();

            String ip = "Unknown";
            String browser = "Unknown";
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                ip = request.getHeader("X-Forwarded-For");
                if (ip == null || ip.isEmpty()) {
                    ip = request.getRemoteAddr();
                } else {
                    ip = ip.split(",")[0].trim();
                }
                browser = request.getHeader("User-Agent");
            }

            activityLogService.logActivity(
                    user.getId(),
                    user.getEmail(),
                    trackActivity.action(),
                    trackActivity.entity(),
                    "N/A", // We can't always know the exact ID from generic AOP easily without SpEL, default to N/A
                    trackActivity.description().isEmpty() ? "User performed " + trackActivity.action() : trackActivity.description(),
                    ip,
                    browser
            );

        } catch (Exception e) {
            System.err.println("Failed to log activity via AOP: " + e.getMessage());
        }
    }
}
