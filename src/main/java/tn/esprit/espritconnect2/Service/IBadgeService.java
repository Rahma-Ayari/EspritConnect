package tn.esprit.espritconnect2.Service;

import tn.esprit.espritconnect2.DTO.BadgeDTO;
import tn.esprit.espritconnect2.DTO.BadgeReqDTO;
import tn.esprit.espritconnect2.DTO.UserBadgeDTO;
import tn.esprit.espritconnect2.DTO.BadgeRequestDTO;
import tn.esprit.espritconnect2.DTO.BadgeRequestReqDTO;

import java.util.List;
import java.util.UUID;

public interface IBadgeService {
    BadgeDTO createBadge(BadgeReqDTO req);
    BadgeDTO updateBadge(Long id, BadgeReqDTO req);
    void deleteBadge(Long id);
    List<BadgeDTO> getAllBadges();
    BadgeDTO getBadgeById(Long id);
    BadgeDTO toggleBadgeStatus(Long id);
    UserBadgeDTO assignBadgeToUser(Long badgeId, UUID userId);
    void removeBadgeFromUser(Long badgeId, UUID userId);
    List<UserBadgeDTO> getUserBadges(UUID userId);
    
    BadgeRequestDTO requestBadge(BadgeRequestReqDTO req);
    List<BadgeRequestDTO> getAllRequests();
    void handleRequest(Long requestId, boolean approved);
}
