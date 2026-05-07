package tn.esprit.espritconnect2.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tn.esprit.espritconnect2.Entitie.Status;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CandidatureStatusUpdateDTO {
    @NotNull
    private Status statut;
}
