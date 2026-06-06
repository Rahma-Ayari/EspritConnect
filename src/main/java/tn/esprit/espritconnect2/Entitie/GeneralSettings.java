package tn.esprit.espritconnect2.Entitie;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "general_settings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneralSettings {

    @Id
    private Long id = 1L;

    @Column(nullable = false)
    @Builder.Default
    private String alumniDepartmentEmail = "alumni@esprit.tn";

    @Column(nullable = false)
    @Builder.Default
    private String alumniDepartmentPhone = "+216 70 000 000";

    @Builder.Default
    private boolean donationOptInPageEnabled = true;

    @Builder.Default
    private boolean invitationsEventsStudent = true;
    @Builder.Default
    private boolean invitationsEventsAlumni = true;
    @Builder.Default
    private boolean invitationsEventsCompany = true;
    @Builder.Default
    private boolean invitationsEventsTeacherStaff = true;

    @Builder.Default
    private boolean universityUpdatesStudent = true;
    @Builder.Default
    private boolean universityUpdatesAlumni = true;
    @Builder.Default
    private boolean universityUpdatesCompany = false;
    @Builder.Default
    private boolean universityUpdatesTeacherStaff = true;

    @Builder.Default
    private boolean fundraisingEmailsStudent = false;
    @Builder.Default
    private boolean fundraisingEmailsAlumni = true;
    @Builder.Default
    private boolean fundraisingEmailsCompany = false;
    @Builder.Default
    private boolean fundraisingEmailsTeacherStaff = false;
}
