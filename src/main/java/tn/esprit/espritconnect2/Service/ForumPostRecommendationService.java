package tn.esprit.espritconnect2.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tn.esprit.espritconnect2.DTO.PostMatchResponse;
import tn.esprit.espritconnect2.DTO.PostRecommendationResponse;
import tn.esprit.espritconnect2.DTO.RecommendedPostItem;
import tn.esprit.espritconnect2.Entitie.ForumPost;
import tn.esprit.espritconnect2.Entitie.PostStatus;
import tn.esprit.espritconnect2.Repository.ForumPostFavoriteRepository;
import tn.esprit.espritconnect2.Repository.ForumPostRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ForumPostRecommendationService {

    private static final int CANDIDATE_POOL_SIZE = 200;

    private final ForumPostFavoriteRepository favoriteRepository;
    private final ForumPostRepository postRepository;
    private final PostRecommendationScoringService scoringService;
    private final ForumPostRecommendationAiService aiService;
    private final ForumPostRecommendationCacheService cacheService;
    private final PostMatchingService postMatchingService;

    @Transactional(readOnly = true)
    public PostRecommendationResponse recommend(String email, int limit, String lang, boolean forceRefresh) {
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("Email is required.");
        }

        String normalizedEmail = email.trim();
        int safeLimit = Math.max(1, Math.min(limit, 20));

        List<ForumPost> favorites = favoriteRepository.findPostsWithDetailsByUserEmail(normalizedEmail);
        List<Long> favoriteIds = favorites.stream()
                .map(ForumPost::getId)
                .filter(Objects::nonNull)
                .toList();
        String favoritesHash = cacheService.buildFavoritesHash(favoriteIds);

        if (!forceRefresh) {
            Optional<PostRecommendationResponse> cached = cacheService.getIfValid(normalizedEmail, favoritesHash);
            if (cached.isPresent()) {
                return trimResponse(cached.get(), safeLimit);
            }
        }

        PostRecommendationResponse response = favorites.isEmpty()
                ? buildColdStartResponse(normalizedEmail, safeLimit)
                : buildFavoritesBasedResponse(normalizedEmail, favorites, safeLimit, lang);

        cacheService.put(normalizedEmail, favoritesHash, response);
        return response;
    }

    public void refresh(String email) {
        cacheService.evict(email);
    }

    public void dismiss(String email, Long postId) {
        cacheService.dismiss(email, postId);
    }

    private PostRecommendationResponse buildFavoritesBasedResponse(
            String email,
            List<ForumPost> favorites,
            int limit,
            String lang) {

        PostRecommendationScoringService.UserTasteProfile profile = scoringService.buildProfile(favorites);
        Set<Long> excludedIds = buildExcludedIds(email, favorites);
        List<ForumPost> candidates = loadCandidates(email, excludedIds);

        List<RecommendedPostItem> ranked = scoringService.rank(candidates, profile, limit);
        aiService.enrichWithReasons(ranked, favorites, profile, lang);

        return PostRecommendationResponse.builder()
                .aiPowered(aiService.isConfigured())
                .basedOnFavoritesCount(favorites.size())
                .generatedAt(LocalDateTime.now())
                .recommendations(ranked)
                .emptyState(ranked.isEmpty() ? "NO_CANDIDATES" : null)
                .build();
    }

    private PostRecommendationResponse buildColdStartResponse(String email, int limit) {
        List<RecommendedPostItem> items = new ArrayList<>();

        List<PostMatchResponse> skillMatches = postMatchingService.getMatchesForUser(email);
        Set<Long> seenIds = new HashSet<>();

        for (PostMatchResponse match : skillMatches) {
            if (match.getPost() == null || match.getPost().getId() == null) {
                continue;
            }
            if (seenIds.add(match.getPost().getId())) {
                items.add(scoringService.fromSkillMatch(match.getPost(), match.getScore()));
            }
            if (items.size() >= limit) {
                break;
            }
        }

        if (items.size() < limit) {
            Set<Long> excludedIds = buildExcludedIds(email, List.of());
            excludedIds.addAll(seenIds);
            List<ForumPost> trending = loadCandidates(email, excludedIds).stream()
                    .sorted(Comparator
                            .comparingInt((ForumPost p) -> p.getLikesCount() + p.getViewsCount() / 10)
                            .reversed()
                            .thenComparing(ForumPost::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                    .limit(limit - items.size())
                    .toList();

            int baseScore = 55;
            for (ForumPost post : trending) {
                items.add(scoringService.fromTrending(post, baseScore));
                baseScore = Math.max(40, baseScore - 3);
            }
        }

        return PostRecommendationResponse.builder()
                .aiPowered(false)
                .basedOnFavoritesCount(0)
                .generatedAt(LocalDateTime.now())
                .recommendations(items.stream().limit(limit).collect(Collectors.toList()))
                .emptyState(items.isEmpty() ? "NO_FAVORITES" : "NO_FAVORITES")
                .build();
    }

    private List<ForumPost> loadCandidates(String email, Set<Long> excludedIds) {
        List<ForumPost> published = postRepository.findPublishedPostsExcludingAuthor(
                PostStatus.PUBLISHED,
                email,
                PageRequest.of(0, CANDIDATE_POOL_SIZE)
        );

        return published.stream()
                .filter(post -> post.getId() != null && !excludedIds.contains(post.getId()))
                .filter(post -> !cacheService.getDismissedPostIds(email).contains(post.getId()))
                .collect(Collectors.toList());
    }

    private Set<Long> buildExcludedIds(String email, List<ForumPost> favorites) {
        Set<Long> excluded = new HashSet<>(cacheService.getDismissedPostIds(email));
        favorites.stream()
                .map(ForumPost::getId)
                .filter(Objects::nonNull)
                .forEach(excluded::add);

        postRepository.findByAuthorEmailIgnoreCaseAndReportedFalseOrderByCreatedAtDesc(email).stream()
                .map(ForumPost::getId)
                .filter(Objects::nonNull)
                .forEach(excluded::add);

        return excluded;
    }

    private PostRecommendationResponse trimResponse(PostRecommendationResponse response, int limit) {
        if (response.getRecommendations() == null || response.getRecommendations().size() <= limit) {
            return response;
        }
        return PostRecommendationResponse.builder()
                .aiPowered(response.isAiPowered())
                .basedOnFavoritesCount(response.getBasedOnFavoritesCount())
                .generatedAt(response.getGeneratedAt())
                .recommendations(response.getRecommendations().stream().limit(limit).toList())
                .emptyState(response.getEmptyState())
                .build();
    }
}
