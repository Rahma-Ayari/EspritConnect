package tn.esprit.espritconnect2.frontoffice.dto;

import lombok.Data;
import java.util.List;

@Data
public class AIDescriptionRequest {
    private String titre;
    private String typeOffre;
    private String domaine;
    private List<String> competences;
    private String localisation;
    private String prompt;
}
