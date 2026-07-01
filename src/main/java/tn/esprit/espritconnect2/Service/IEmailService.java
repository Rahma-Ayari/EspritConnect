package tn.esprit.espritconnect2.Service;

import tn.esprit.espritconnect2.Entitie.User;

public interface IEmailService {

    void sendNewRegistrationNotification(User user);

    void sendApprovalNotification(User user);

    void sendDeclineNotification(User user);

    void sendWelcomeEmailWithTemporaryPassword(User user, String temporaryPassword);

    void sendEmailVerification(User user, String verificationToken);

    void sendPasswordResetEmail(User user, String resetUrl);

    void sendSuspiciousLoginWarningEmail(User user, String ipAddress, String userAgentInfo);

    void sendAccountLockoutEmail(User user, String ipAddress, String userAgentInfo);

    void sendApplicationConfirmation(String studentEmail, String studentName, String jobTitle, String companyName);

    void sendApplicationProceeding(String studentEmail, String studentName, String jobTitle, String companyName);

    void sendApplicationAccepted(String studentEmail, String studentName, String jobTitle, String companyName);

    void sendApplicationRejected(String studentEmail, String studentName, String jobTitle, String companyName);
}
