package tn.esprit.espritconnect2.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardResponseDTO {
    private Summary summary;
    private long pendingApprovalsCount;
    private List<PendingApprovalItem> pendingApprovals;
    private ActivityOverview activityOverview;
    private QuickStart quickStart;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Summary {
        private long totalUsers;
        private long students;
        private long alumni;
        private long companies;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PendingApprovalItem {
        /** COMPANY | ETUDIANT | ALUMNI */
        private String profileType;
        private Long companyId;
        /** UUID du compte Spring Security (users) pour approuver / refuser un étudiant ou alumni */
        private String userId;
        /** Nom affiché (étudiant, alumni ou entreprise) */
        private String companyName;
        /** Sous-titre : filière, domaine, secteur, etc. */
        private String sector;
        private String statusLabel;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ActivityOverview {
        private long newRegistrations;
        private long registrationDeltaPercent;
        private long activeJobPostings;
        private long jobPostingDeltaPercent;
        private long sentEmailCampaigns;
        private long emailCampaignDeltaPercent;
        private long avgOpenRatePercent;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuickStart {
        private int completedSteps;
        private int totalSteps;
    }
}
