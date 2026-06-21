package tn.esprit.espritconnect2.DTO;

import lombok.Data;

@Data
public class AiResponseMetaDTO {
    private String provider;
    private boolean cached;
    private String aiDisclaimer;
}
