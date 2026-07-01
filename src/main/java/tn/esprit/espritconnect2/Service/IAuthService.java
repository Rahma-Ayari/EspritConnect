package tn.esprit.espritconnect2.Service;

import org.springframework.web.multipart.MultipartFile;
import tn.esprit.espritconnect2.DTO.AuthResponse;
import tn.esprit.espritconnect2.DTO.EnterpriseRegisterRequest;
import tn.esprit.espritconnect2.DTO.LoginRequest;
import tn.esprit.espritconnect2.DTO.RegisterRequest;
import tn.esprit.espritconnect2.DTO.RegisterResponse;

public interface IAuthService {
    AuthResponse login(LoginRequest req);
    AuthResponse verify2faLogin(tn.esprit.espritconnect2.DTO.TwoFactorVerificationRequest verifyReq, String ipAddress, String userAgent);
    RegisterResponse register(RegisterRequest req);
    RegisterResponse registerEnterprise(EnterpriseRegisterRequest req, MultipartFile document);
    void requestPasswordReset(String email);
    void resetPassword(String token, String newPassword);
}
