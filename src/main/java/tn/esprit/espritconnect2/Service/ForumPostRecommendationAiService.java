package tn.esprit.espritconnect2.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tn.esprit.espritconnect2.DTO.RecommendedPostItem;
import tn.esprit.espritconnect2.Entitie.ForumPost;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ForumPostRecommendationAiService {

    private static final int MAX_AI_ITEMS = 5;
    private static final int MAX_REASON_LENGTH = 140;

    private final ChatbotAiService chatbotAiService;
    private final PostRecommendationScoringService scoringService;

    public boolean isConfigured() {
        return chatbotAiService.isConfigured();
    }

    public void enrichWithReasons(
            List<RecommendedPostItem> items,
            List<ForumPost> favorites,
            PostRecommendationScoringService.UserTasteProfile profile,
            String lang) {

        if (items == null || items.isEmpty()) {
            return;
        }

        List<RecommendedPostItem> topItems = items.stream().limit(MAX_AI_ITEMS).toList();
        for (RecommendedPostItem item : topItems) {
            item.setAiReason(generateReason(item, favorites, profile, lang));
        }

        for (int i = MAX_AI_ITEMS; i < items.size(); i++) {
            RecommendedPostItem item = items.get(i);
            if (!StringUtils.hasText(item.getAiReason())) {
                item.setAiReason(scoringService.buildTemplateReason(item));
            }
        }
    }

    private String generateReason(
            RecommendedPostItem item,
            List<ForumPost> favorites,
            PostRecommendationScoringService.UserTasteProfile profile,
            String lang) {

        String templateReason = scoringService.buildTemplateReason(item);
        if (!chatbotAiService.isConfigured() || item.getPost() == null) {
            return templateReason;
        }

        try {
            String favoriteTitles = favorites.stream()
                    .limit(3)
                    .map(ForumPost::getTitle)
                    .filter(StringUtils::hasText)
                    .collect(Collectors.joining(" | "));

            String topTags = profile.getTagWeights().entrySet().stream()
                    .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                    .limit(5)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.joining(", "));

            String systemPrompt = """
                    You are a recommendation assistant for ESPRIT Connect forum.
                    Write ONE short sentence (max 120 characters) explaining why a post might interest the user.
                    Tone: professional, friendly, concise.
                    Language: %s.
                    Output ONLY the sentence. No quotes, no bullet points.
                    """.formatted(english(lang) ? "English" : "French");

            String userPrompt = """
                    User favorite posts: %s
                    User top tags: %s
                    Candidate post title: %s
                    Candidate tags: %s
                    Structural reasons: %s
                    """.formatted(
                    StringUtils.hasText(favoriteTitles) ? favoriteTitles : "none",
                    StringUtils.hasText(topTags) ? topTags : "none",
                    item.getPost().getTitle(),
                    item.getPost().getTags() != null ? String.join(", ", item.getPost().getTags()) : "none",
                    item.getReasons() != null ? String.join("; ", item.getReasons()) : "none"
            );

            String aiContent = chatbotAiService.generateContent(systemPrompt, userPrompt);
            if (StringUtils.hasText(aiContent)) {
                return truncate(aiContent.trim());
            }
        } catch (Exception ex) {
            log.warn("AI recommendation reason failed for post #{}: {}", item.getPost().getId(), ex.getMessage());
        }

        return templateReason;
    }

    private boolean english(String lang) {
        return lang != null && lang.toLowerCase(Locale.ROOT).startsWith("en");
    }

    private String truncate(String value) {
        if (value.length() <= MAX_REASON_LENGTH) {
            return value;
        }
        return value.substring(0, MAX_REASON_LENGTH - 3).trim() + "...";
    }
}
