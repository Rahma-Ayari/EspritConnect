package tn.esprit.espritconnect2.DTO.frontOffice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprit.espritconnect2.Entitie.ForumCategory;
import tn.esprit.espritconnect2.Entitie.ForumPost;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForumHomeDTO {
    private long totalPosts;
    private long totalReplies;
    private long totalCategories;
    private List<ForumCategory> categories;
    private Map<Long, Long> postCountsPerCategory;
    private List<ForumPost> pinnedPosts;
    private List<ForumPost> recentPosts;
}
