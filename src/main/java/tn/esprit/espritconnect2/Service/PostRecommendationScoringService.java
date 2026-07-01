package tn.esprit.espritconnect2.Service;

import lombok.Builder;
import lombok.Getter;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tn.esprit.espritconnect2.DTO.RecommendationSource;
import tn.esprit.espritconnect2.DTO.RecommendedPostItem;
import tn.esprit.espritconnect2.Entitie.ForumPost;
import tn.esprit.espritconnect2.Entitie.PostType;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostRecommendationScoringService {

    private static final int MIN_SCORE = 35;

    public UserTasteProfile buildProfile(List<ForumPost> favorites) {
        Map<String, Integer> tagWeights = new HashMap<>();
        Map<Long, Integer> categoryWeights = new HashMap<>();
        Map<PostType, Integer> postTypeWeights = new EnumMap<>(PostType.class);
        Map<Long, Integer> discussionWeights = new HashMap<>();

        for (ForumPost favorite : favorites) {
            incrementTagWeights(tagWeights, favorite.getTags());
            if (favorite.getCategory() != null && favorite.getCategory().getId() != null) {
                categoryWeights.merge(favorite.getCategory().getId(), 1, Integer::sum);
            }
            if (favorite.getPostType() != null) {
                postTypeWeights.merge(favorite.getPostType(), 1, Integer::sum);
            }
            if (favorite.getForumDiscussion() != null && favorite.getForumDiscussion().getId() != null) {
                discussionWeights.merge(favorite.getForumDiscussion().getId(), 1, Integer::sum);
            }
        }

        return UserTasteProfile.builder()
                .tagWeights(tagWeights)
                .categoryWeights(categoryWeights)
                .postTypeWeights(postTypeWeights)
                .discussionWeights(discussionWeights)
                .recentFavoriteTitles(favorites.stream()
                        .limit(5)
                        .map(ForumPost::getTitle)
                        .filter(StringUtils::hasText)
                        .collect(Collectors.toList()))
                .build();
    }

    public List<RecommendedPostItem> rank(List<ForumPost> candidates, UserTasteProfile profile, int limit) {
        return candidates.stream()
                .map(post -> scorePost(post, profile))
                .filter(item -> item.getScore() >= MIN_SCORE)
                .sorted(Comparator.comparingInt(RecommendedPostItem::getScore).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public RecommendedPostItem scorePost(ForumPost post, UserTasteProfile profile) {
        List<String> reasons = new ArrayList<>();
        double tagScore = computeTagOverlap(post.getTags(), profile.getTagWeights(), reasons);
        double categoryScore = computeCategoryMatch(post, profile.getCategoryWeights(), reasons);
        double typeScore = computePostTypeMatch(post, profile.getPostTypeWeights(), reasons);
        double discussionScore = computeDiscussionMatch(post, profile.getDiscussionWeights(), reasons);
        double recencyScore = computeRecencyBoost(post.getPublishedAt() != null ? post.getPublishedAt() : post.getCreatedAt());
        double popularityScore = computePopularityBoost(post.getLikesCount(), post.getViewsCount());

        int totalScore = (int) Math.round(
                tagScore * 0.40
                        + categoryScore * 0.25
                        + typeScore * 0.15
                        + discussionScore * 0.10
                        + recencyScore * 0.05
                        + popularityScore * 0.05
        );

        if (reasons.isEmpty()) {
            reasons.add("Related to your reading interests");
        }

        return RecommendedPostItem.builder()
                .post(post)
                .score(Math.min(100, totalScore))
                .matchLabel(matchLabel(totalScore))
                .reasons(reasons)
                .source(RecommendationSource.FAVORITES_PROFILE)
                .build();
    }

    public RecommendedPostItem fromSkillMatch(ForumPost post, int score) {
        return RecommendedPostItem.builder()
                .post(post)
                .score(Math.min(100, score))
                .matchLabel(matchLabel(score))
                .reasons(List.of("Matches your profile skills and interests"))
                .source(RecommendationSource.SKILLS_MATCH)
                .build();
    }

    public RecommendedPostItem fromTrending(ForumPost post, int score) {
        return RecommendedPostItem.builder()
                .post(post)
                .score(Math.min(100, score))
                .matchLabel("Trending")
                .reasons(List.of("Popular with the ESPRIT community"))
                .source(RecommendationSource.TRENDING)
                .build();
    }

    public String buildTemplateReason(RecommendedPostItem item) {
        if (item.getReasons() != null && !item.getReasons().isEmpty()) {
            return "Because " + item.getReasons().get(0).toLowerCase(Locale.ROOT) + ".";
        }
        return "Because it aligns with topics you follow in the forum.";
    }

    private double computeTagOverlap(List<String> tags, Map<String, Integer> tagWeights, List<String> reasons) {
        if (tags == null || tags.isEmpty() || tagWeights.isEmpty()) {
            return 0;
        }
        Set<String> normalizedPostTags = normalizeTags(tags);
        int matchedWeight = 0;
        int totalWeight = tagWeights.values().stream().mapToInt(Integer::intValue).sum();
        List<String> matchedTags = new ArrayList<>();

        for (String tag : normalizedPostTags) {
            Integer weight = tagWeights.get(tag);
            if (weight != null && weight > 0) {
                matchedWeight += weight;
                matchedTags.add(tag);
            }
        }

        if (!matchedTags.isEmpty()) {
            reasons.add(matchedTags.size() + " shared tag" + (matchedTags.size() > 1 ? "s" : "")
                    + ": " + String.join(", ", matchedTags.stream().limit(3).toList()));
        }

        if (totalWeight == 0) {
            return 0;
        }
        return Math.min(100, (matchedWeight * 100.0) / totalWeight);
    }

    private double computeCategoryMatch(ForumPost post, Map<Long, Integer> categoryWeights, List<String> reasons) {
        if (post.getCategory() == null || post.getCategory().getId() == null || categoryWeights.isEmpty()) {
            return 0;
        }
        Integer weight = categoryWeights.get(post.getCategory().getId());
        if (weight == null || weight == 0) {
            return 0;
        }
        int maxWeight = categoryWeights.values().stream().mapToInt(Integer::intValue).max().orElse(1);
        if (post.getCategory().getName() != null) {
            reasons.add("Same category: " + post.getCategory().getName());
        }
        return Math.min(100, (weight * 100.0) / maxWeight);
    }

    private double computePostTypeMatch(ForumPost post, Map<PostType, Integer> postTypeWeights, List<String> reasons) {
        if (post.getPostType() == null || postTypeWeights.isEmpty()) {
            return 0;
        }
        Integer weight = postTypeWeights.get(post.getPostType());
        if (weight == null || weight == 0) {
            return 0;
        }
        int maxWeight = postTypeWeights.values().stream().mapToInt(Integer::intValue).max().orElse(1);
        reasons.add("Similar post type: " + formatPostType(post.getPostType()));
        return Math.min(100, (weight * 100.0) / maxWeight);
    }

    private double computeDiscussionMatch(ForumPost post, Map<Long, Integer> discussionWeights, List<String> reasons) {
        if (post.getForumDiscussion() == null || post.getForumDiscussion().getId() == null || discussionWeights.isEmpty()) {
            return 0;
        }
        Integer weight = discussionWeights.get(post.getForumDiscussion().getId());
        if (weight == null || weight == 0) {
            return 0;
        }
        int maxWeight = discussionWeights.values().stream().mapToInt(Integer::intValue).max().orElse(1);
        if (post.getForumDiscussion().getName() != null) {
            reasons.add("From a discussion you follow: " + post.getForumDiscussion().getName());
        }
        return Math.min(100, (weight * 100.0) / maxWeight);
    }

    private double computeRecencyBoost(LocalDateTime publishedAt) {
        if (publishedAt == null) {
            return 40;
        }
        long days = ChronoUnit.DAYS.between(publishedAt, LocalDateTime.now());
        if (days <= 7) return 100;
        if (days <= 30) return 75;
        if (days <= 90) return 50;
        return 25;
    }

    private double computePopularityBoost(int likes, int views) {
        double likeScore = Math.min(100, likes * 8.0);
        double viewScore = Math.min(100, views * 0.5);
        return Math.min(100, (likeScore + viewScore) / 2.0);
    }

    private String matchLabel(int score) {
        if (score >= 80) return "Highly relevant";
        if (score >= 60) return "Good match";
        if (score >= 45) return "Worth reading";
        return "Suggested";
    }

    private String formatPostType(PostType type) {
        return type.name().charAt(0) + type.name().substring(1).toLowerCase(Locale.ROOT).replace('_', ' ');
    }

    private Set<String> normalizeTags(List<String> tags) {
        return tags.stream()
                .filter(StringUtils::hasText)
                .map(tag -> tag.toLowerCase(Locale.ROOT).replace("#", "").trim())
                .filter(tag -> !tag.isEmpty())
                .collect(Collectors.toSet());
    }

    private void incrementTagWeights(Map<String, Integer> tagWeights, List<String> tags) {
        if (tags == null) {
            return;
        }
        for (String tag : normalizeTags(tags)) {
            tagWeights.merge(tag, 1, Integer::sum);
        }
    }

    @Getter
    @Builder
    public static class UserTasteProfile {
        private final Map<String, Integer> tagWeights;
        private final Map<Long, Integer> categoryWeights;
        private final Map<PostType, Integer> postTypeWeights;
        private final Map<Long, Integer> discussionWeights;
        private final List<String> recentFavoriteTitles;
    }
}
