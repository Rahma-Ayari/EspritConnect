package tn.esprit.espritconnect2.DTO;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class AiImportExtractResponseDTO extends AiResponseMetaDTO {
    private String title;
    private String contractType;
    private String experienceLevel;
    private String location;
    private List<String> skills;
    private String responsibilities;
    private String requirements;
    private String benefits;
    private String description;
}
