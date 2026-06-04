package tn.esprit.espritconnect2.DTO;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkAddUsersResponse {
    private int successCount;
    private int failedCount;
    private List<String> errors;
    private List<UserApprovalDTO> users;
}
