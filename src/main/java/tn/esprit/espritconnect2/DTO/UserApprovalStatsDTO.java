package tn.esprit.espritconnect2.DTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserApprovalStatsDTO {
    private long pendingCount;
    private long approvedCount;
    private long totalStudents;
    private long totalAlumni;
}
