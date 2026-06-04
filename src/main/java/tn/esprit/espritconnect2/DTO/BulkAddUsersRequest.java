package tn.esprit.espritconnect2.DTO;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkAddUsersRequest {
    private List<NewUserRequest> users;
}
