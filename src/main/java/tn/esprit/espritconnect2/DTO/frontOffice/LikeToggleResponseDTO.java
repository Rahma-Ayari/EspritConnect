package tn.esprit.espritconnect2.DTO.frontOffice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LikeToggleResponseDTO {
    private int likesCount;
    private boolean likedByCurrentUser;
}
