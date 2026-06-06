package tn.esprit.espritconnect2.Entitie;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Singleton row (id = 1) for admin "General Settings": alumni contact + communication opt-ins.
 */
@Entity
@Table(name = "general_platform_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeneralPlatformSettings {

    public static final Long SINGLETON_ID = 1L;

    @Id
    @Column(name = "id")
    private Long id = SINGLETON_ID;

    @Column(name = "alumni_department_email", nullable = false, length = 255)
    @Builder.Default
    private String alumniDepartmentEmail = "espritconnect@esprit.tn";

    @Column(name = "alumni_department_phone", nullable = false, length = 64)
    @Builder.Default
    private String alumniDepartmentPhone = "+216 XX XXX XXX";

    @Column(name = "donation_opt_in_page_enabled", nullable = false)
    @Builder.Default
    private boolean donationOptInPageEnabled = false;

    // Invitations to events
    @Column(name = "inv_events_student", nullable = false)
    @Builder.Default
    private boolean invitationsEventsStudent = true;
    @Column(name = "inv_events_alumni", nullable = false)
    @Builder.Default
    private boolean invitationsEventsAlumni = false;
    @Column(name = "inv_events_company", nullable = false)
    @Builder.Default
    private boolean invitationsEventsCompany = true;
    @Column(name = "inv_events_staff", nullable = false)
    @Builder.Default
    private boolean invitationsEventsTeacherStaff = true;

    // University updates
    @Column(name = "uni_updates_student", nullable = false)
    @Builder.Default
    private boolean universityUpdatesStudent = true;
    @Column(name = "uni_updates_alumni", nullable = false)
    @Builder.Default
    private boolean universityUpdatesAlumni = false;
    @Column(name = "uni_updates_company", nullable = false)
    @Builder.Default
    private boolean universityUpdatesCompany = true;
    @Column(name = "uni_updates_staff", nullable = false)
    @Builder.Default
    private boolean universityUpdatesTeacherStaff = false;

    // Fundraising emails
    @Column(name = "fundraising_student", nullable = false)
    @Builder.Default
    private boolean fundraisingEmailsStudent = false;
    @Column(name = "fundraising_alumni", nullable = false)
    @Builder.Default
    private boolean fundraisingEmailsAlumni = true;
    @Column(name = "fundraising_company", nullable = false)
    @Builder.Default
    private boolean fundraisingEmailsCompany = true;
    @Column(name = "fundraising_staff", nullable = false)
    @Builder.Default
    private boolean fundraisingEmailsTeacherStaff = false;

    @PrePersist
    void ensureId() {
        if (id == null) {
            id = SINGLETON_ID;
        }
    }
}
