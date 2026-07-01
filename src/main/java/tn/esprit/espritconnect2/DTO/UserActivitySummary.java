package tn.esprit.espritconnect2.DTO;

import lombok.Data;
import java.util.List;

@Data
public class UserActivitySummary {
    private String username;
    private String lastLogin;
    private String lastProfileUpdate;
    private String riskLevel;
    private List<String> timeline;
}
