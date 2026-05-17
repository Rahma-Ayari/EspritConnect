package tn.esprit.espritconnect2.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.espritconnect2.DTO.BadgeDTO;
import tn.esprit.espritconnect2.DTO.BadgeReqDTO;
import tn.esprit.espritconnect2.DTO.UserBadgeDTO;
import tn.esprit.espritconnect2.Entitie.*;
import tn.esprit.espritconnect2.Exception.BusinessRuleException;
import tn.esprit.espritconnect2.Exception.NotFoundException;
import tn.esprit.espritconnect2.Repository.BadgeRepository;
import tn.esprit.espritconnect2.Repository.UserBadgeRepository;
import tn.esprit.espritconnect2.Repository.UserRepository;
import tn.esprit.espritconnect2.Repository.BadgeRequestRepository;
import tn.esprit.espritconnect2.DTO.BadgeRequestDTO;
import tn.esprit.espritconnect2.DTO.BadgeRequestReqDTO;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BadgeServiceImpl implements IBadgeService {

    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final UserRepository userRepository;
    private final BadgeRequestRepository badgeRequestRepository;

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
        Badge badge = badgeRepository.findById(badgeId).orElseThrow(() -> new NotFoundException("Badge not found"));
        String email = "test.student." + userId.toString().substring(0, 8) + "@esprit.tn";
        User user = userRepository.findById(userId)
                .or(() -> userRepository.findByEmail(email))
                .orElseGet(() -> {
                    log.info("User {} not found by ID or Email, creating new mock user", userId);
                    User newUser = User.builder()
                            .id(userId)
                            .nom("Test Student")
                            .email(email)
                            .password("password")
                            .role(Role.ETUDIANT)
                            .enabled(true)
                            .build();
                    return userRepository.save(newUser);
                });

        if (userBadgeRepository.findByBadgeIdAndUser_Id(badgeId, userId).isPresent()) {
            throw new BusinessRuleException("User already has this badge");
        }

        UserBadge userBadge = UserBadge.builder()
                .badge(badge)
                .user(user)
                .build();
        userBadge = userBadgeRepository.save(userBadge);
        log.info("Successfully assigned badge {} to user {}", badge.getName(), userId);

        return mapUserBadgeToDTO(userBadge);
    }

    @Override
    @Transactional
    public void removeBadgeFromUser(Long badgeId, UUID userId) {
        UserBadge userBadge = userBadgeRepository.findByBadgeIdAndUser_Id(badgeId, userId)
                .orElseThrow(() -> new NotFoundException("User badge association not found"));
        userBadgeRepository.delete(userBadge);
    }

    @Override
    @Transactional
    public List<UserBadgeDTO> getUserBadges(UUID userId) {
        log.info("Fetching badges for user ID: {}", userId);
        
        // Find the "real" ID for this user (could be different if DB was binary)
        String email = "test.student." + userId.toString().substring(0, 8) + "@esprit.tn";
        UUID actualId = userRepository.findById(userId)
                .map(User::getId)
                .orElseGet(() -> userRepository.findByEmail(email)
                        .map(User::getId)
                        .orElse(userId));

        if (!actualId.equals(userId)) {
            log.info("ID mismatch detected. Mapping frontend ID {} to DB ID {}", userId, actualId);
        }

        // Using native query with the actual ID found in DB
        List<UserBadgeDTO> badges = userBadgeRepository.findByUserIdNative(actualId.toString()).stream()
                .map(this::mapUserBadgeToDTO)
                .collect(Collectors.toList());
        
        log.info("Found {} earned badges for user {}", badges.size(), actualId);
        return badges;
    }

    @Override
    @Transactional
    public BadgeRequestDTO requestBadge(BadgeRequestReqDTO req) {
        UUID studentId = req.getUserId();
        String email = "test.student." + studentId.toString().substring(0, 8) + "@esprit.tn";
        User student = userRepository.findById(studentId)
                .or(() -> userRepository.findByEmail(email))
                .orElseGet(() -> {
                    log.info("Student {} not found by ID or Email, creating new mock student", studentId);
                    User newUser = User.builder()
                            .id(studentId)
                            .nom("Test Student")
                            .email(email)
                            .password("password")
                            .role(Role.ETUDIANT)
                            .enabled(true)
                            .build();
                    return userRepository.save(newUser);
                });

        // 1. Check if user already has this badge
        if (userBadgeRepository.findByBadgeIdAndUser_Id(req.getBadgeId(), studentId).isPresent()) {
            throw new BusinessRuleException("You already earned this badge!");
        }

        // 2. Check if user already has a pending or approved request for this badge
        List<RequestStatus> activeStatuses = List.of(RequestStatus.PENDING, RequestStatus.APPROVED);
        if (!badgeRequestRepository.findByUserIdAndBadgeIdAndStatusIn(studentId, req.getBadgeId(), activeStatuses).isEmpty()) {
            throw new BusinessRuleException("You already have an active request for this badge!");
        }

        Badge badge = badgeRepository.findById(req.getBadgeId()).orElseThrow(() -> new NotFoundException("Badge not found"));

        BadgeRequest request = BadgeRequest.builder()
                .badge(badge)
                .user(student)
                .motivation(req.getMotivation())
                .status(RequestStatus.PENDING)
                .build();
        request = badgeRequestRepository.save(request);
        return mapBadgeRequestToDTO(request);
    }

    @Override
    public List<BadgeRequestDTO> getAllRequests() {
        return badgeRequestRepository.findAll().stream()
                .map(this::mapBadgeRequestToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void handleRequest(Long requestId, boolean approved) {
        BadgeRequest request = badgeRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request not found"));
        
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new BusinessRuleException("Request already handled");
        }

        if (approved) {
            log.info("Approving request {} for badge {} and user {}", requestId, request.getBadge().getName(), request.getUser().getId());
            request.setStatus(RequestStatus.APPROVED);
            assignBadgeToUser(request.getBadge().getId(), request.getUser().getId());
        } else {
            log.info("Rejecting request {}", requestId);
            request.setStatus(RequestStatus.REJECTED);
        }
        badgeRequestRepository.save(request);
        log.info("Request {} handled successfully with status {}", requestId, request.getStatus());
    }
    
    private BadgeRequestDTO mapBadgeRequestToDTO(BadgeRequest req) {
        return BadgeRequestDTO.builder()
                .id(req.getId())
                .userId(req.getUser().getId())
                .userName(req.getUser().getNom())
                .badgeId(req.getBadge().getId())
                .badgeName(req.getBadge().getName())
                .motivation(req.getMotivation())
                .status(req.getStatus())
                .requestedAt(req.getRequestedAt())
                .build();
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
