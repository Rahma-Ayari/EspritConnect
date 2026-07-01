package tn.esprit.espritconnect2.DTO;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AiJobGenerateWrapperDTO extends AiResponseMetaDTO {
    private AIJobGenerateResponseDTO data;
}
