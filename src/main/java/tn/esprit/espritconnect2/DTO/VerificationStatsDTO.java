package tn.esprit.espritconnect2.DTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationStatsDTO {
    private long total;
    private long pending;
    private long verified;
    private long rejected;
    private long notSubmitted;
}
