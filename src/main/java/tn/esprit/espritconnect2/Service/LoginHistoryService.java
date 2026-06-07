package tn.esprit.espritconnect2.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.espritconnect2.Entitie.LoginHistory;
import tn.esprit.espritconnect2.Entitie.User;
import tn.esprit.espritconnect2.Repository.LoginHistoryRepository;
import tn.esprit.espritconnect2.security.UserAgentParser;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginHistoryService {

    private final LoginHistoryRepository loginHistoryRepository;
    private final UserAgentParser userAgentParser;
    private final IpGeolocationService ipGeolocationService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordLoginAttempt(User user, String ipAddress, String userAgent, String status) {
        try {
            UserAgentParser.UserAgentDetails uaDetails = userAgentParser.parse(userAgent);

            LoginHistory history = LoginHistory.builder()
                    .user(user)
                    .loginTime(LocalDateTime.now())
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .os(uaDetails.os)
                    .browser(uaDetails.browser)
                    .device(uaDetails.device)
                    .status(status)
                    .location("Recherche...")
                    .build();

            final LoginHistory savedHistory = loginHistoryRepository.save(history);

            ipGeolocationService.getGeoLocation(ipAddress).thenAccept(location -> {
                savedHistory.setLocation(location);
                loginHistoryRepository.save(savedHistory);
            });
        } catch (Exception e) {
            log.warn("Could not record login history for {}: {}", user.getEmail(), e.getMessage());
        }
    }

    public List<LoginHistory> getLoginHistory(User user) {
        return loginHistoryRepository.findTop10ByUserOrderByLoginTimeDesc(user);
    }
}
