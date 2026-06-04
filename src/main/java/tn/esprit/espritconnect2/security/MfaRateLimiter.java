package tn.esprit.espritconnect2.security;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MfaRateLimiter {

    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCK_TIME_MINUTES = 15;

    private static class AttemptTracker {
        int attempts = 0;
        LocalDateTime lastAttemptTime = LocalDateTime.now();
    }

    private final ConcurrentHashMap<String, AttemptTracker> cache = new ConcurrentHashMap<>();

    public boolean isLocked(String email) {
        if (email == null) return false;
        AttemptTracker tracker = cache.get(email);
        if (tracker == null) return false;

        if (tracker.attempts >= MAX_ATTEMPTS) {
            LocalDateTime lockExpiry = tracker.lastAttemptTime.plusMinutes(LOCK_TIME_MINUTES);
            if (LocalDateTime.now().isBefore(lockExpiry)) {
                return true;
            } else {
                // Lock has expired, clean up
                cache.remove(email);
            }
        }
        return false;
    }

    public void recordFailure(String email) {
        if (email == null) return;
        cache.compute(email, (key, tracker) -> {
            if (tracker == null) {
                tracker = new AttemptTracker();
            }
            tracker.attempts++;
            tracker.lastAttemptTime = LocalDateTime.now();
            return tracker;
        });
    }

    public void recordSuccess(String email) {
        if (email == null) return;
        cache.remove(email);
    }

    public int getRemainingAttempts(String email) {
        if (email == null) return MAX_ATTEMPTS;
        AttemptTracker tracker = cache.get(email);
        if (tracker == null) return MAX_ATTEMPTS;
        return Math.max(0, MAX_ATTEMPTS - tracker.attempts);
    }
}
