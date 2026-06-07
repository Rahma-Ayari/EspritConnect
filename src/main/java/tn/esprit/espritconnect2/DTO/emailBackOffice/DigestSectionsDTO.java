package tn.esprit.espritconnect2.DTO.emailBackOffice;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DigestSectionsDTO {
    private boolean businessDirectoryPosts;
    private boolean recentlyJoinedMembers;
    private boolean latestEvents;
    private boolean latestFeedPosts;
    private boolean latestJobPosts;
    private boolean includePlatformContact;
}