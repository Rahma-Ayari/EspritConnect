package tn.esprit.espritconnect2.DTO;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BulkApprovalRequest {
    private List<UUID> userIds;
}
