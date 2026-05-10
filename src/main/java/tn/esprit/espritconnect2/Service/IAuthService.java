package tn.esprit.espritconnect2.Service;

import tn.esprit.espritconnect2.DTO.AuthResponse;
import tn.esprit.espritconnect2.DTO.LoginRequest;
import tn.esprit.espritconnect2.DTO.RegisterRequest;

public interface IAuthService {
    AuthResponse login(LoginRequest req);
    AuthResponse register(RegisterRequest req);
}
