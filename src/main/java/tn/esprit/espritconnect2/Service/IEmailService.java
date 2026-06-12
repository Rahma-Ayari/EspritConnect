package tn.esprit.espritconnect2.Service;

import tn.esprit.espritconnect2.Entitie.User;

public interface IEmailService {

    void sendNewRegistrationNotification(User user);

    void sendApprovalNotification(User user);

    void sendDeclineNotification(User user);

    void sendWelcomeEmailWithTemporaryPassword(User user, String temporaryPassword);

    void sendEmailVerification(User user, String verificationToken);

    void sendPasswordResetEmail(User user, String resetUrl);
}
