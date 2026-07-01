package tn.esprit.espritconnect2.Service;

import org.springframework.stereotype.Service;
import tn.esprit.espritconnect2.DTO.PostRecommendationResponse;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ForumPostRecommendationCacheService {

    private static final Duration TTL = Duration.ofMinutes(30);

    private final Map<String, CachedEntry> cache = new ConcurrentHashMap<>();
    private final Map<String, Set<Long>> dismissedByUser = new ConcurrentHashMap<>();

    public Optional<PostRecommendationResponse> getIfValid(String email, String favoritesHash) {
        CachedEntry entry = cache.get(normalize(email));
        if (entry == null) {
            return Optional.empty();
        }
        if (entry.expiresAt().isBefore(LocalDateTime.now())) {
            cache.remove(normalize(email));
            return Optional.empty();
        }
        if (!entry.favoritesHash().equals(favoritesHash)) {
            return Optional.empty();
        }
        return Optional.of(entry.response());
    }

    public void put(String email, String favoritesHash, PostRecommendationResponse response) {
        cache.put(normalize(email), new CachedEntry(favoritesHash, response, LocalDateTime.now().plus(TTL)));
    }

    public void evict(String email) {
        cache.remove(normalize(email));
    }

    public void dismiss(String email, Long postId) {
        if (postId == null) {
            return;
        }
        dismissedByUser.computeIfAbsent(normalize(email), key -> ConcurrentHashMap.newKeySet()).add(postId);
        evict(email);
    }

    public Set<Long> getDismissedPostIds(String email) {
        return dismissedByUser.getOrDefault(normalize(email), Set.of());
    }

    public String buildFavoritesHash(java.util.List<Long> favoriteIds) {
        if (favoriteIds == null || favoriteIds.isEmpty()) {
            return "empty";
        }
        return favoriteIds.stream()
                .sorted()
                .map(String::valueOf)
                .reduce((a, b) -> a + "," + b)
                .orElse("empty");
    }

    private String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private record CachedEntry(String favoritesHash, PostRecommendationResponse response, LocalDateTime expiresAt) {}
}
