package tn.esprit.espritconnect2.Entitie;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "regional_settings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegionalSettings {

    @Id
    private Long id = 1L;

    @Column(nullable = false, columnDefinition = "TEXT")
    @Builder.Default
    private String institutionAddress = "Pôle Technologique, Chotrana, 1082 Tunis, Tunisia";

    @Builder.Default
    private boolean locationDetectionEnabled = true;

    @Column(nullable = false)
    @Builder.Default
    private String primaryTimezone = "Africa/Tunis";

    @ElementCollection
    @CollectionTable(name = "regional_additional_timezones", joinColumns = @JoinColumn(name = "settings_id"))
    @Column(name = "timezone")
    @Builder.Default
    private List<String> additionalTimezones = new ArrayList<>(List.of("Europe/Paris", "UTC"));

    @Column(nullable = false)
    @Builder.Default
    private String dateFormat = "DD_MM_YYYY";

    @Column(nullable = false)
    @Builder.Default
    private String timeFormat = "HOUR_24";

    @Column(nullable = false)
    @Builder.Default
    private String defaultLanguage = "fr";

    @ElementCollection
    @CollectionTable(name = "regional_additional_languages", joinColumns = @JoinColumn(name = "settings_id"))
    @Column(name = "language_code")
    @Builder.Default
    private List<String> additionalLanguages = new ArrayList<>(List.of("en", "ar"));
}
