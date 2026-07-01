package tn.esprit.espritconnect2.DTO;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserActivityInfoDTO {
    private LocalDateTime lastLoginAt;
    private String lastLoginIp;
    private String lastLoginBrowser;
    private LocalDateTime lastProfileUpdateAt;
    private String lastProfileUpdateDescription;
}
