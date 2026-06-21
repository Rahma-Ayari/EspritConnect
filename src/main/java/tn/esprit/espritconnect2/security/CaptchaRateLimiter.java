package tn.esprit.espritconnect2.security;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CaptchaRateLimiter {

    private static class Tracker {
        int count = 0;
        LocalDateTime windowStart = LocalDateTime.now();
    }

    private final ConcurrentHashMap<String, Tracker> cache = new ConcurrentHashMap<>();

    public boolean isRateLimited(String clientIp, int maxRequestsPerWindow, int windowMinutes) {
        if (clientIp == null || clientIp.isBlank()) {
            return false;
        }
        Tracker tracker = cache.computeIfAbsent(clientIp, k -> new Tracker());
        synchronized (tracker) {
            LocalDateTime now = LocalDateTime.now();
            if (tracker.windowStart.plusMinutes(windowMinutes).isBefore(now)) {
                tracker.count = 0;
                tracker.windowStart = now;
            }
            return tracker.count >= maxRequestsPerWindow;
        }
    }

    public void recordRequest(String clientIp) {
        if (clientIp == null || clientIp.isBlank()) {
            return;
        }
        Tracker tracker = cache.computeIfAbsent(clientIp, k -> new Tracker());
        synchronized (tracker) {
            tracker.count++;
        }
    }
}
