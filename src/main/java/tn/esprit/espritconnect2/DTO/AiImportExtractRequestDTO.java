package tn.esprit.espritconnect2.DTO;

import lombok.Data;

import java.util.List;

@Data
public class AiImportExtractRequestDTO {
    private String rawContent;
    private String source;
    private boolean forceRefresh;
}
