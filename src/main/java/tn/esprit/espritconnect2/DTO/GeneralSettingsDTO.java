package tn.esprit.espritconnect2.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeneralSettingsDTO {

    @NotBlank
    @Email
    private String alumniDepartmentEmail;

    @NotBlank
    @Size(max = 64)
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
