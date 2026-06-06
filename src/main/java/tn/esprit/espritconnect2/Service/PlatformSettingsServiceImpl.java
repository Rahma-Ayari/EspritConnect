package tn.esprit.espritconnect2.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.espritconnect2.DTO.GeneralSettingsDTO;
import tn.esprit.espritconnect2.DTO.RegionalSettingsDTO;
import tn.esprit.espritconnect2.DTO.RegistrationSettingsDTO;
import tn.esprit.espritconnect2.Entitie.GeneralSettings;
import tn.esprit.espritconnect2.Entitie.RegionalSettings;
import tn.esprit.espritconnect2.Entitie.RegistrationSettings;
import tn.esprit.espritconnect2.Repository.GeneralSettingsRepository;
import tn.esprit.espritconnect2.Repository.RegionalSettingsRepository;
import tn.esprit.espritconnect2.Repository.RegistrationSettingsRepository;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class PlatformSettingsServiceImpl implements IPlatformSettingsService {

    private static final long SETTINGS_ID = 1L;

    private final GeneralSettingsRepository generalSettingsRepository;
    private final RegionalSettingsRepository regionalSettingsRepository;
    private final RegistrationSettingsRepository registrationSettingsRepository;

    @Override
    @Transactional
    public void seedDefaultsIfMissing() {
        if (generalSettingsRepository.findById(SETTINGS_ID).isEmpty()) {
            generalSettingsRepository.save(GeneralSettings.builder().id(SETTINGS_ID).build());
        }
        if (regionalSettingsRepository.findById(SETTINGS_ID).isEmpty()) {
            regionalSettingsRepository.save(RegionalSettings.builder().id(SETTINGS_ID).build());
        }
        if (registrationSettingsRepository.findById(SETTINGS_ID).isEmpty()) {
            registrationSettingsRepository.save(RegistrationSettings.builder().id(SETTINGS_ID).build());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public GeneralSettingsDTO getGeneralSettings() {
        return mapGeneral(requireGeneral());
    }

    @Override
    @Transactional
    public GeneralSettingsDTO updateGeneralSettings(GeneralSettingsDTO dto) {
        GeneralSettings entity = requireGeneral();
        entity.setAlumniDepartmentEmail(dto.getAlumniDepartmentEmail());
        entity.setAlumniDepartmentPhone(dto.getAlumniDepartmentPhone());
        entity.setDonationOptInPageEnabled(dto.isDonationOptInPageEnabled());
        entity.setInvitationsEventsStudent(dto.isInvitationsEventsStudent());
        entity.setInvitationsEventsAlumni(dto.isInvitationsEventsAlumni());
        entity.setInvitationsEventsCompany(dto.isInvitationsEventsCompany());
        entity.setInvitationsEventsTeacherStaff(dto.isInvitationsEventsTeacherStaff());
        entity.setUniversityUpdatesStudent(dto.isUniversityUpdatesStudent());
        entity.setUniversityUpdatesAlumni(dto.isUniversityUpdatesAlumni());
        entity.setUniversityUpdatesCompany(dto.isUniversityUpdatesCompany());
        entity.setUniversityUpdatesTeacherStaff(dto.isUniversityUpdatesTeacherStaff());
        entity.setFundraisingEmailsStudent(dto.isFundraisingEmailsStudent());
        entity.setFundraisingEmailsAlumni(dto.isFundraisingEmailsAlumni());
        entity.setFundraisingEmailsCompany(dto.isFundraisingEmailsCompany());
        entity.setFundraisingEmailsTeacherStaff(dto.isFundraisingEmailsTeacherStaff());
        return mapGeneral(generalSettingsRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public RegionalSettingsDTO getRegionalSettings() {
        return mapRegional(requireRegional());
    }

    @Override
    @Transactional
    public RegionalSettingsDTO updateRegionalSettings(RegionalSettingsDTO dto) {
        RegionalSettings entity = requireRegional();
        entity.setInstitutionAddress(dto.getInstitutionAddress());
        entity.setLocationDetectionEnabled(dto.isLocationDetectionEnabled());
        entity.setPrimaryTimezone(dto.getPrimaryTimezone());
        entity.setAdditionalTimezones(dto.getAdditionalTimezones() != null
                ? new ArrayList<>(dto.getAdditionalTimezones()) : new ArrayList<>());
        entity.setDateFormat(dto.getDateFormat());
        entity.setTimeFormat(dto.getTimeFormat());
        entity.setDefaultLanguage(dto.getDefaultLanguage());
        entity.setAdditionalLanguages(dto.getAdditionalLanguages() != null
                ? new ArrayList<>(dto.getAdditionalLanguages()) : new ArrayList<>());
        return mapRegional(regionalSettingsRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public RegistrationSettingsDTO getRegistrationSettings() {
        return mapRegistration(requireRegistration());
    }

    @Override
    @Transactional
    public RegistrationSettingsDTO updateRegistrationSettings(RegistrationSettingsDTO dto) {
        RegistrationSettings entity = requireRegistration();
        entity.setLinkedInEnabled(dto.isLinkedInEnabled());
        entity.setFacebookEnabled(dto.isFacebookEnabled());
        entity.setGoogleEnabled(dto.isGoogleEnabled());
        entity.setSsoEnabled(dto.isSsoEnabled());
        entity.setEmailEnabled(dto.isEmailEnabled());
        entity.setAppleEnabled(dto.isAppleEnabled());
        entity.setTermsAndPrivacyHtml(dto.getTermsAndPrivacyHtml());
        return mapRegistration(registrationSettingsRepository.save(entity));
    }

    private GeneralSettings requireGeneral() {
        return generalSettingsRepository.findById(SETTINGS_ID)
                .orElseGet(() -> generalSettingsRepository.save(GeneralSettings.builder().id(SETTINGS_ID).build()));
    }

    private RegionalSettings requireRegional() {
        return regionalSettingsRepository.findById(SETTINGS_ID)
                .orElseGet(() -> regionalSettingsRepository.save(RegionalSettings.builder().id(SETTINGS_ID).build()));
    }

    private RegistrationSettings requireRegistration() {
        return registrationSettingsRepository.findById(SETTINGS_ID)
                .orElseGet(() -> registrationSettingsRepository.save(RegistrationSettings.builder().id(SETTINGS_ID).build()));
    }

    private GeneralSettingsDTO mapGeneral(GeneralSettings s) {
        return GeneralSettingsDTO.builder()
                .alumniDepartmentEmail(s.getAlumniDepartmentEmail())
                .alumniDepartmentPhone(s.getAlumniDepartmentPhone())
                .donationOptInPageEnabled(s.isDonationOptInPageEnabled())
                .invitationsEventsStudent(s.isInvitationsEventsStudent())
                .invitationsEventsAlumni(s.isInvitationsEventsAlumni())
                .invitationsEventsCompany(s.isInvitationsEventsCompany())
                .invitationsEventsTeacherStaff(s.isInvitationsEventsTeacherStaff())
                .universityUpdatesStudent(s.isUniversityUpdatesStudent())
                .universityUpdatesAlumni(s.isUniversityUpdatesAlumni())
                .universityUpdatesCompany(s.isUniversityUpdatesCompany())
                .universityUpdatesTeacherStaff(s.isUniversityUpdatesTeacherStaff())
                .fundraisingEmailsStudent(s.isFundraisingEmailsStudent())
                .fundraisingEmailsAlumni(s.isFundraisingEmailsAlumni())
                .fundraisingEmailsCompany(s.isFundraisingEmailsCompany())
                .fundraisingEmailsTeacherStaff(s.isFundraisingEmailsTeacherStaff())
                .build();
    }

    private RegionalSettingsDTO mapRegional(RegionalSettings s) {
        return RegionalSettingsDTO.builder()
                .institutionAddress(s.getInstitutionAddress())
                .locationDetectionEnabled(s.isLocationDetectionEnabled())
                .primaryTimezone(s.getPrimaryTimezone())
                .additionalTimezones(new ArrayList<>(s.getAdditionalTimezones()))
                .dateFormat(s.getDateFormat())
                .timeFormat(s.getTimeFormat())
                .defaultLanguage(s.getDefaultLanguage())
                .additionalLanguages(new ArrayList<>(s.getAdditionalLanguages()))
                .build();
    }

    private RegistrationSettingsDTO mapRegistration(RegistrationSettings s) {
        return RegistrationSettingsDTO.builder()
                .linkedInEnabled(s.isLinkedInEnabled())
                .facebookEnabled(s.isFacebookEnabled())
                .googleEnabled(s.isGoogleEnabled())
                .ssoEnabled(s.isSsoEnabled())
                .emailEnabled(s.isEmailEnabled())
                .appleEnabled(s.isAppleEnabled())
                .termsAndPrivacyHtml(s.getTermsAndPrivacyHtml())
                .build();
    }
}
