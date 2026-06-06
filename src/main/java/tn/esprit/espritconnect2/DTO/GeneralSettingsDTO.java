package tn.esprit.espritconnect2.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneralSettingsDTO {
    private String alumniDepartmentEmail;
    private String alumniDepartmentPhone;
    private boolean donationOptInPageEnabled;
    private boolean invitationsEventsStudent;
    private boolean invitationsEventsAlumni;
    private boolean invitationsEventsCompany;
    private boolean invitationsEventsTeacherStaff;
    private boolean universityUpdatesStudent;
    private boolean universityUpdatesAlumni;
    private boolean universityUpdatesCompany;
    private boolean universityUpdatesTeacherStaff;
    private boolean fundraisingEmailsStudent;
    private boolean fundraisingEmailsAlumni;
    private boolean fundraisingEmailsCompany;
    private boolean fundraisingEmailsTeacherStaff;
}
