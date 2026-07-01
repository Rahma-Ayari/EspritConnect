package tn.esprit.espritconnect2.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CandidatureRequestDTO {
    @NotNull
    private Long etudiantId;

    @NotNull
    private Long offreId;

    @Size(max = 2000)
    private String lettreMotivation;
}
