package tn.esprit.espritconnect2.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MatchingComputeRequestDTO {
    @NotNull
    private Long etudiantId;

    @NotNull
    private Long offreId;
}
