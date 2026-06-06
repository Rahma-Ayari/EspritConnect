package tn.esprit.espritconnect2.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegionalSettingsDTO {
    private String institutionAddress;
    private boolean locationDetectionEnabled;
    private String primaryTimezone;
    @Builder.Default
    private List<String> additionalTimezones = new ArrayList<>();
    private String dateFormat;
    private String timeFormat;
    private String defaultLanguage;
    @Builder.Default
    private List<String> additionalLanguages = new ArrayList<>();
}
