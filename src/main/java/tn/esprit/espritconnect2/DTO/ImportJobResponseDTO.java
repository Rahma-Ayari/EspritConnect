package tn.esprit.espritconnect2.DTO;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class ImportJobResponseDTO {
    private String title;
    private String description;
    private String responsibilities;
    private List<String> skills;
    private String requirements;
    private String benefits;
    private String experienceLevel;
    private String location;
    private String contractType;
    private Map<String, Object> extractedData;
}
