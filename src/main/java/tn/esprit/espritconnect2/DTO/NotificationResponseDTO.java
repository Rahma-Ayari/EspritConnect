package tn.esprit.espritconnect2.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class NotificationResponseDTO {
    private Long idNotification;
    private String type;
    private String contenu;
    private Date dateEnvoi;
    private Boolean lue;
    private String destinataire;
}
