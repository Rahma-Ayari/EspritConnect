package tn.esprit.espritconnect2.DTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntrepriseJobDashboardDTO {
    private EntrepriseVerificationDTO verification;
    private long totalOffers;
    private long activeOffers;
    private long totalApplications;
    private long pendingApplications;
}
