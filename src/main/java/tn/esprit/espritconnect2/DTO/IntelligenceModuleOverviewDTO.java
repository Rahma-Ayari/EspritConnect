package tn.esprit.espritconnect2.DTO;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class IntelligenceModuleOverviewDTO {
    private String moduleName;
    private List<SubmoduleStatusDTO> submodules;
}
