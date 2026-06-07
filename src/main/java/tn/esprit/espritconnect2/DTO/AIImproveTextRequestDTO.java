package tn.esprit.espritconnect2.DTO;

import lombok.Data;

@Data
public class AIImproveTextRequestDTO {
    private String originalText;
    private String jobTitle;
    private String targetAudience;
    /** Output language code: en, fr, ar */
    private String outputLanguage;
}
