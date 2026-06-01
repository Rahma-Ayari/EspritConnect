package tn.esprit.espritconnect2.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import tn.esprit.espritconnect2.Entitie.Role;
import tn.esprit.espritconnect2.Entitie.User;

import java.util.Optional;
import java.util.UUID;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Optional<User> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return Optional.empty();
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof User user) {
            return Optional.of(user);
        }
        return Optional.empty();
    }

    public static UUID getCurrentUserId() {
        return getCurrentUser()
                .map(User::getId)
                .orElseThrow(() -> new AccessDeniedException("Authentication required"));
    }

    public static UUID getCurrentUserIdOr(UUID fallback) {
        return getCurrentUser()
                .map(User::getId)
                .orElseGet(() -> {
                    if (fallback != null) {
                        return fallback;
                    }
                    throw new AccessDeniedException("Authentication required");
                });
    }

    public static boolean isAdmin() {
        return getCurrentUser().map(u -> u.getRole() == Role.ADMIN).orElse(false);
    }
}
