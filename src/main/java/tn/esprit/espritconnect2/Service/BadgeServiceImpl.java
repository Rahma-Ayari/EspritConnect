package tn.esprit.espritconnect2.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.espritconnect2.DTO.BadgeDTO;
import tn.esprit.espritconnect2.DTO.BadgeReqDTO;
import tn.esprit.espritconnect2.DTO.UserBadgeDTO;
import tn.esprit.espritconnect2.Entitie.Badge;
import tn.esprit.espritconnect2.Entitie.User;
import tn.esprit.espritconnect2.Entitie.UserBadge;
import tn.esprit.espritconnect2.Exception.BusinessRuleException;
import tn.esprit.espritconnect2.Exception.NotFoundException;
import tn.esprit.espritconnect2.Repository.BadgeRepository;
import tn.esprit.espritconnect2.Repository.UserBadgeRepository;
import tn.esprit.espritconnect2.Repository.UserRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BadgeServiceImpl implements IBadgeService {

    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BadgeDTO createBadge(BadgeReqDTO req) {
        Badge badge = Badge.builder()
                .name(req.getName())
                .criteria(req.getCriteria())
                .icon(req.getIcon())
                .badgeType(req.getBadgeType())
                .enabled(req.isEnabled())
                .build();
        badge = badgeRepository.save(badge);
        return mapToDTO(badge);
    }

    @Override
    @Transactional
    public BadgeDTO updateBadge(Long id, BadgeReqDTO req) {
        Badge badge = badgeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Badge not found"));
        
        badge.setName(req.getName());
        badge.setCriteria(req.getCriteria());
        badge.setIcon(req.getIcon());
        badge.setBadgeType(req.getBadgeType());
        badge.setEnabled(req.isEnabled());
        
        return mapToDTO(badgeRepository.save(badge));
    }

    @Override
    @Transactional
    public void deleteBadge(Long id) {
        if (!badgeRepository.existsById(id)) {
            throw new NotFoundException("Badge not found");
        }
        badgeRepository.deleteById(id);
    }

    @Override
    public List<BadgeDTO> getAllBadges() {
        return badgeRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BadgeDTO getBadgeById(Long id) {
        return badgeRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> new NotFoundException("Badge not found"));
    }

    @Override
    @Transactional
    public BadgeDTO toggleBadgeStatus(Long id) {
        Badge badge = badgeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Badge not found"));
        badge.setEnabled(!badge.isEnabled());
        return mapToDTO(badgeRepository.save(badge));
    }

    @Override
    @Transactional
    public UserBadgeDTO assignBadgeToUser(Long badgeId, UUID userId) {
        Badge badge = badgeRepository.findById(badgeId)
                .orElseThrow(() -> new NotFoundException("Badge not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (userBadgeRepository.findByBadgeIdAndUserId(badgeId, userId).isPresent()) {
            throw new BusinessRuleException("User already has this badge");
        }

        UserBadge userBadge = UserBadge.builder()
                .badge(badge)
                .user(user)
                .build();
        userBadge = userBadgeRepository.save(userBadge);

        return mapUserBadgeToDTO(userBadge);
    }

    @Override
    @Transactional
    public void removeBadgeFromUser(Long badgeId, UUID userId) {
        UserBadge userBadge = userBadgeRepository.findByBadgeIdAndUserId(badgeId, userId)
                .orElseThrow(() -> new NotFoundException("User badge association not found"));
        userBadgeRepository.delete(userBadge);
    }

    @Override
    public List<UserBadgeDTO> getUserBadges(UUID userId) {
        return userBadgeRepository.findByUserId(userId).stream()
                .map(this::mapUserBadgeToDTO)
                .collect(Collectors.toList());
    }

    private BadgeDTO mapToDTO(Badge badge) {
        long count = userBadgeRepository.countByBadgeId(badge.getId());
        return BadgeDTO.builder()
                .id(badge.getId())
                .name(badge.getName())
                .criteria(badge.getCriteria())
                .icon(badge.getIcon())
                .badgeType(badge.getBadgeType())
                .enabled(badge.isEnabled())
                .createdAt(badge.getCreatedAt())
                .userCount(count)
                .build();
    }

    private UserBadgeDTO mapUserBadgeToDTO(UserBadge userBadge) {
        return UserBadgeDTO.builder()
                .id(userBadge.getId())
                .userId(userBadge.getUser().getId())
                .userName(userBadge.getUser().getNom())
                .badgeId(userBadge.getBadge().getId())
                .badgeName(userBadge.getBadge().getName())
                .earnedAt(userBadge.getEarnedAt())
                .build();
    }
}
