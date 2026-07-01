package tn.esprit.espritconnect2.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprit.espritconnect2.Entitie.ForumPost;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostMatchResponse {
    private Long matchId;
    private ForumPost post;
    private int score;
    private boolean notified;
}
