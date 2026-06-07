package tn.esprit.espritconnect2.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.espritconnect2.DTO.RegistrationSettingsDTO;
import tn.esprit.espritconnect2.Entitie.RegistrationPlatformSettings;
import tn.esprit.espritconnect2.Repository.RegistrationPlatformSettingsRepository;

@Service
@RequiredArgsConstructor
public class RegistrationSettingsService {

    private final RegistrationPlatformSettingsRepository repository;

    @Transactional(readOnly = true)
    public RegistrationSettingsDTO getSettings() {
        return toDto(repository.findById(RegistrationPlatformSettings.SINGLETON_ID)
                .orElseGet(this::createDefaults));
    }

    @Transactional
    public RegistrationSettingsDTO updateSettings(RegistrationSettingsDTO dto) {
        if (!anyMethodEnabled(dto)) {
            throw new IllegalArgumentException(
                    "At least one registration method must remain enabled (e.g. Email).");
        }
        String safeHtml = RegistrationHtmlSanitizer.sanitize(dto.getTermsAndPrivacyHtml());

        RegistrationPlatformSettings entity = repository.findById(RegistrationPlatformSettings.SINGLETON_ID)
                .orElseGet(this::createDefaults);
        applyDto(entity, dto, safeHtml);
        return toDto(repository.save(entity));
    }

    private boolean anyMethodEnabled(RegistrationSettingsDTO d) {
        return d.isLinkedInEnabled()
                || d.isFacebookEnabled()
                || d.isGoogleEnabled()
                || d.isSsoEnabled()
                || d.isEmailEnabled()
                || d.isAppleEnabled();
    }

    private RegistrationPlatformSettings createDefaults() {
        RegistrationPlatformSettings s = RegistrationPlatformSettings.builder()
                .id(RegistrationPlatformSettings.SINGLETON_ID)
                .build();
        return repository.save(s);
    }

    private void applyDto(RegistrationPlatformSettings e, RegistrationSettingsDTO d, String safeHtml) {
        e.setLinkedInEnabled(d.isLinkedInEnabled());
        e.setFacebookEnabled(d.isFacebookEnabled());
        e.setGoogleEnabled(d.isGoogleEnabled());
        e.setSsoEnabled(d.isSsoEnabled());
        e.setEmailEnabled(d.isEmailEnabled());
        e.setAppleEnabled(d.isAppleEnabled());
        e.setTermsAndPrivacyHtml(safeHtml);
    }

    private RegistrationSettingsDTO toDto(RegistrationPlatformSettings e) {
        return RegistrationSettingsDTO.builder()
                .linkedInEnabled(e.isLinkedInEnabled())
                .facebookEnabled(e.isFacebookEnabled())
                .googleEnabled(e.isGoogleEnabled())
                .ssoEnabled(e.isSsoEnabled())
                .emailEnabled(e.isEmailEnabled())
                .appleEnabled(e.isAppleEnabled())
                .termsAndPrivacyHtml(e.getTermsAndPrivacyHtml() == null ? "" : e.getTermsAndPrivacyHtml())
                .build();
    }
}
