package tn.esprit.espritconnect2.Service;

import tn.esprit.espritconnect2.Entitie.User;

public interface IEmailService {

    void sendNewRegistrationNotification(User user);

    void sendApprovalNotification(User user);

    void sendDeclineNotification(User user);
}
