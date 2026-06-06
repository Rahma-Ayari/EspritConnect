package tn.esprit.espritconnect2.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tn.esprit.espritconnect2.Entitie.RegionalDateFormat;
import tn.esprit.espritconnect2.Entitie.RegionalTimeFormat;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegionalSettingsDTO {

    @Size(max = 512)
    private String institutionAddress;

    private boolean locationDetectionEnabled;

    @NotBlank
    @Size(max = 64)
    private String primaryTimezone;

    @NotNull
    @Builder.Default
    private List<@Size(max = 64) String> additionalTimezones = new ArrayList<>();

    @NotNull
    private RegionalDateFormat dateFormat;

    @NotNull
    private RegionalTimeFormat timeFormat;

    @NotBlank
    @Size(max = 32)
    private String defaultLanguage;

    @NotNull
    @Builder.Default
    private List<@Size(max = 32) String> additionalLanguages = new ArrayList<>();
}
