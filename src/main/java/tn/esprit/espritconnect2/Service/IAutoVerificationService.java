package tn.esprit.espritconnect2.Service;

import tn.esprit.espritconnect2.DTO.AutoVerificationResultDTO;
import tn.esprit.espritconnect2.Entitie.User;

public interface IAutoVerificationService {
    AutoVerificationResultDTO performAutoVerification(User user);
    boolean isValidBusinessRegistrationNumber(String rcNumber);
    boolean isValidTaxNumber(String taxNumber);
    int calculateConfidenceScore(User user);
}
