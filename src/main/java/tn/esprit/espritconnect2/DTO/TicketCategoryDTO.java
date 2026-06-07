package tn.esprit.espritconnect2.DTO;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketCategoryDTO {
    private Long id;
    private String name;
    private String description;
    private Integer slaHours;
}
