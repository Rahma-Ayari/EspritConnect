package tn.esprit.espritconnect2.Entitie;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "regional_platform_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegionalPlatformSettings {

    public static final Long SINGLETON_ID = 1L;

    @Id
    @Column(name = "id")
    private Long id = SINGLETON_ID;

    @Column(name = "institution_address", nullable = false, length = 512)
    @Builder.Default
    private String institutionAddress = "ESPRIT, Cebalat Ben Ammar, Ariana, Tunisia";

    @Column(name = "location_detection_enabled", nullable = false)
    @Builder.Default
    private boolean locationDetectionEnabled = false;

    @Column(name = "primary_timezone", nullable = false, length = 64)
    @Builder.Default
    private String primaryTimezone = "Africa/Tunis";

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "regional_additional_timezones", joinColumns = @JoinColumn(name = "regional_id"))
    @Column(name = "zone_id", length = 64)
    @Builder.Default
    private List<String> additionalTimezones = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "date_format", nullable = false, length = 32)
    @Builder.Default
    private RegionalDateFormat dateFormat = RegionalDateFormat.DD_MM_YYYY;

    @Enumerated(EnumType.STRING)
    @Column(name = "time_format", nullable = false, length = 32)
    @Builder.Default
    private RegionalTimeFormat timeFormat = RegionalTimeFormat.HOUR_24;

    @Column(name = "default_language", nullable = false, length = 32)
    @Builder.Default
    private String defaultLanguage = "en_GB";

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "regional_additional_languages", joinColumns = @JoinColumn(name = "regional_id"))
    @Column(name = "lang_code", length = 32)
    @Builder.Default
    private List<String> additionalLanguages = new ArrayList<>();

    @PrePersist
    void ensureId() {
        if (id == null) {
            id = SINGLETON_ID;
        }
    }
}
