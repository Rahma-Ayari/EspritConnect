package tn.esprit.espritconnect2.ai;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JobsAiCache {

    private record CacheEntry(String payload, String provider, Instant expiresAt) {
        boolean isValid() {
            return Instant.now().isBefore(expiresAt);
        }
    }

    private final Map<String, CacheEntry> store = new ConcurrentHashMap<>();

    public Optional<CachedResult> get(String key) {
        CacheEntry entry = store.get(key);
        if (entry == null || !entry.isValid()) {
            if (entry != null) {
                store.remove(key);
            }
            return Optional.empty();
        }
        return Optional.of(new CachedResult(entry.payload(), entry.provider()));
    }

    public void put(String key, String payload, String provider, long ttlMinutes) {
        store.put(key, new CacheEntry(payload, provider, Instant.now().plusSeconds(ttlMinutes * 60)));
    }

    public void invalidate(String key) {
        store.remove(key);
    }

    public record CachedResult(String payload, String provider) {}
}
