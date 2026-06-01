package tn.esprit.espritconnect2.DTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubmoduleStatusDTO {
    private String name;
    /** FRONTOFFICE or BACKOFFICE */
    private String office;
    private String status;
    private String apiBasePath;
    private String description;
}
