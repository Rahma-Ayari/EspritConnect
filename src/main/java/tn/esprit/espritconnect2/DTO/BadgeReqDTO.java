package tn.esprit.espritconnect2.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprit.espritconnect2.Entitie.BadgeType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BadgeReqDTO {
    
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Criteria is required")
    private String criteria;

    @NotBlank(message = "Icon is required")
    private String icon;

    @NotNull(message = "Badge type is required")
    private BadgeType badgeType;

    @Builder.Default
    private boolean enabled = true;
}
