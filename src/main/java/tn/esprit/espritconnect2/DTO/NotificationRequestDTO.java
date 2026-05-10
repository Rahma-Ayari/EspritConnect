package tn.esprit.espritconnect2.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class NotificationRequestDTO {
    @NotBlank(message = "Le type est obligatoire")
    private String type;
    @NotBlank(message = "Le contenu est obligatoire")
    private String contenu;
    private String destinataire;
}
