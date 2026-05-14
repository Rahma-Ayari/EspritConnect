package tn.esprit.espritconnect2.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.espritconnect2.DTO.GeneralSettingsDTO;
import tn.esprit.espritconnect2.Entitie.GeneralPlatformSettings;
import tn.esprit.espritconnect2.Repository.GeneralPlatformSettingsRepository;

@Service
@RequiredArgsConstructor
public class GeneralSettingsService {

    private final GeneralPlatformSettingsRepository repository;

    @Transactional(readOnly = true)
    public GeneralSettingsDTO getSettings() {
        return toDto(repository.findById(GeneralPlatformSettings.SINGLETON_ID)
                .orElseGet(this::createDefaults));
    }

    @Transactional
    public GeneralSettingsDTO updateSettings(GeneralSettingsDTO dto) {
        GeneralPlatformSettings entity = repository.findById(GeneralPlatformSettings.SINGLETON_ID)
                .orElseGet(this::createDefaults);
        applyDto(entity, dto);
        return toDto(repository.save(entity));
    }

    private GeneralPlatformSettings createDefaults() {
        GeneralPlatformSettings s = GeneralPlatformSettings.builder()
                .id(GeneralPlatformSettings.SINGLETON_ID)
                .build();
        return repository.save(s);
    }

    private void applyDto(GeneralPlatformSettings e, GeneralSettingsDTO d) {
        e.setAlumniDepartmentEmail(d.getAlumniDepartmentEmail());
        e.setAlumniDepartmentPhone(d.getAlumniDepartmentPhone());
        e.setDonationOptInPageEnabled(d.isDonationOptInPageEnabled());
        e.setInvitationsEventsStudent(d.isInvitationsEventsStudent());
        e.setInvitationsEventsAlumni(d.isInvitationsEventsAlumni());
        e.setInvitationsEventsCompany(d.isInvitationsEventsCompany());
        e.setInvitationsEventsTeacherStaff(d.isInvitationsEventsTeacherStaff());
        e.setUniversityUpdatesStudent(d.isUniversityUpdatesStudent());
        e.setUniversityUpdatesAlumni(d.isUniversityUpdatesAlumni());
        e.setUniversityUpdatesCompany(d.isUniversityUpdatesCompany());
        e.setUniversityUpdatesTeacherStaff(d.isUniversityUpdatesTeacherStaff());
        e.setFundraisingEmailsStudent(d.isFundraisingEmailsStudent());
        e.setFundraisingEmailsAlumni(d.isFundraisingEmailsAlumni());
        e.setFundraisingEmailsCompany(d.isFundraisingEmailsCompany());
        e.setFundraisingEmailsTeacherStaff(d.isFundraisingEmailsTeacherStaff());
    }

    private GeneralSettingsDTO toDto(GeneralPlatformSettings e) {
        return GeneralSettingsDTO.builder()
                .alumniDepartmentEmail(e.getAlumniDepartmentEmail())
                .alumniDepartmentPhone(e.getAlumniDepartmentPhone())
                .donationOptInPageEnabled(e.isDonationOptInPageEnabled())
                .invitationsEventsStudent(e.isInvitationsEventsStudent())
                .invitationsEventsAlumni(e.isInvitationsEventsAlumni())
                .invitationsEventsCompany(e.isInvitationsEventsCompany())
                .invitationsEventsTeacherStaff(e.isInvitationsEventsTeacherStaff())
                .universityUpdatesStudent(e.isUniversityUpdatesStudent())
                .universityUpdatesAlumni(e.isUniversityUpdatesAlumni())
                .universityUpdatesCompany(e.isUniversityUpdatesCompany())
                .universityUpdatesTeacherStaff(e.isUniversityUpdatesTeacherStaff())
                .fundraisingEmailsStudent(e.isFundraisingEmailsStudent())
                .fundraisingEmailsAlumni(e.isFundraisingEmailsAlumni())
                .fundraisingEmailsCompany(e.isFundraisingEmailsCompany())
                .fundraisingEmailsTeacherStaff(e.isFundraisingEmailsTeacherStaff())
                .build();
    }
}
