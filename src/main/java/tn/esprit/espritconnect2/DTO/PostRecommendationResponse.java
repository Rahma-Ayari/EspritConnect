package tn.esprit.espritconnect2.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostRecommendationResponse {
    private boolean aiPowered;
    private int basedOnFavoritesCount;
    private LocalDateTime generatedAt;
    @Builder.Default
    private List<RecommendedPostItem> recommendations = new ArrayList<>();
    private String emptyState;
}
