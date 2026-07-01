package tn.esprit.espritconnect2.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CandidatureApplyMeDTO {
    @NotNull
    private Long offreId;

    @Size(max = 2000)
    private String lettreMotivation;

    // Selected existing CV (Fichier) to attach. Optional.
    private Long fichierId;

    // Easy-apply standard questions
    private Integer yearsExperience;
    private Boolean willingToRelocate;
    private String availabilityDate; // ISO date (yyyy-MM-dd)
}
