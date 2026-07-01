package tn.esprit.espritconnect2.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprit.espritconnect2.Entitie.ForumPost;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendedPostItem {
    private ForumPost post;
    private int score;
    private String matchLabel;
    @Builder.Default
    private List<String> reasons = new ArrayList<>();
    private String aiReason;
    private RecommendationSource source;
}
