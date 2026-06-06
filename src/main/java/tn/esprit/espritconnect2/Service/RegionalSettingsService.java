package tn.esprit.espritconnect2.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.espritconnect2.DTO.RegionalSettingsDTO;
import tn.esprit.espritconnect2.Entitie.RegionalPlatformSettings;
import tn.esprit.espritconnect2.Repository.RegionalPlatformSettingsRepository;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegionalSettingsService {

    private static final Set<String> SUPPORTED_LANGUAGE_CODES = Set.of(
            "en_GB", "en_US", "fr_FR", "ar_TN", "de_DE", "es_ES", "it_IT"
    );

    private final RegionalPlatformSettingsRepository repository;

    @Transactional(readOnly = true)
    public RegionalSettingsDTO getSettings() {
        return toDto(repository.findById(RegionalPlatformSettings.SINGLETON_ID)
                .orElseGet(this::createDefaults));
    }

    @Transactional
    public RegionalSettingsDTO updateSettings(RegionalSettingsDTO dto) {
        validateTimezones(dto.getPrimaryTimezone(), dto.getAdditionalTimezones());
        validateLanguages(dto.getDefaultLanguage(), dto.getAdditionalLanguages());

        RegionalPlatformSettings entity = repository.findById(RegionalPlatformSettings.SINGLETON_ID)
                .orElseGet(this::createDefaults);
        applyDto(entity, dto);
        return toDto(repository.save(entity));
    }

    private RegionalPlatformSettings createDefaults() {
        RegionalPlatformSettings s = RegionalPlatformSettings.builder()
                .id(RegionalPlatformSettings.SINGLETON_ID)
                .build();
        s.setAdditionalLanguages(new ArrayList<>(List.of("fr_FR")));
        return repository.save(s);
    }

    private void validateTimezones(String primary, List<String> additional) {
        if (primary == null || primary.isBlank()) {
            throw new IllegalArgumentException("primaryTimezone is required");
        }
        requireValidZone(primary.trim());
        if (additional == null) {
            return;
        }
        for (String z : additional) {
            if (z == null || z.isBlank()) {
                continue;
            }
            requireValidZone(z.trim());
        }
    }

    private void requireValidZone(String zoneId) {
        try {
            ZoneId.of(zoneId);
        } catch (DateTimeException e) {
            throw new IllegalArgumentException("Invalid timezone: " + zoneId);
        }
    }

    private void validateLanguages(String defaultLang, List<String> additional) {
        String def = canonicalLang(defaultLang);
        List<String> add = additional == null ? List.of() : additional;
        for (String code : add) {
            if (code == null || code.isBlank()) {
                continue;
            }
            String c = canonicalLang(code);
            if (c.equals(def)) {
                throw new IllegalArgumentException("defaultLanguage must not appear in additionalLanguages");
            }
        }
    }

    private String canonicalLang(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("language code is required");
        }
        String s = raw.trim().replace('-', '_');
        for (String c : SUPPORTED_LANGUAGE_CODES) {
            if (c.equalsIgnoreCase(s)) {
                return c;
            }
        }
        throw new IllegalArgumentException("Unsupported language code: " + raw);
    }

    private void applyDto(RegionalPlatformSettings e, RegionalSettingsDTO d) {
        e.setInstitutionAddress(d.getInstitutionAddress() == null ? "" : d.getInstitutionAddress());
        e.setLocationDetectionEnabled(d.isLocationDetectionEnabled());
        e.setPrimaryTimezone(d.getPrimaryTimezone().trim());

        List<String> extras = d.getAdditionalTimezones() == null ? List.of() : d.getAdditionalTimezones();
        LinkedHashSet<String> zones = extras.stream()
                .filter(z -> z != null && !z.isBlank())
                .map(String::trim)
                .filter(z -> !z.equals(e.getPrimaryTimezone()))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        e.getAdditionalTimezones().clear();
        e.getAdditionalTimezones().addAll(zones);

        e.setDateFormat(d.getDateFormat());
        e.setTimeFormat(d.getTimeFormat());
        String defLang = canonicalLang(d.getDefaultLanguage());
        e.setDefaultLanguage(defLang);

        List<String> langs = d.getAdditionalLanguages() == null ? List.of() : d.getAdditionalLanguages();
        LinkedHashSet<String> langSet = langs.stream()
                .filter(x -> x != null && !x.isBlank())
                .map(this::canonicalLang)
                .filter(x -> !x.equals(defLang))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        e.getAdditionalLanguages().clear();
        e.getAdditionalLanguages().addAll(langSet);
    }

    private RegionalSettingsDTO toDto(RegionalPlatformSettings e) {
        return RegionalSettingsDTO.builder()
                .institutionAddress(e.getInstitutionAddress())
                .locationDetectionEnabled(e.isLocationDetectionEnabled())
                .primaryTimezone(e.getPrimaryTimezone())
                .additionalTimezones(new ArrayList<>(e.getAdditionalTimezones()))
                .dateFormat(e.getDateFormat())
                .timeFormat(e.getTimeFormat())
                .defaultLanguage(e.getDefaultLanguage())
                .additionalLanguages(new ArrayList<>(e.getAdditionalLanguages()))
                .build();
    }
}
