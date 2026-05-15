package tn.esprit.espritconnect2.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.espritconnect2.DTO.UserApprovalDTO;
import tn.esprit.espritconnect2.DTO.UserApprovalStatsDTO;
import tn.esprit.espritconnect2.Entitie.Alumni;
import tn.esprit.espritconnect2.Entitie.Etudiant;
import tn.esprit.espritconnect2.Entitie.Role;
import tn.esprit.espritconnect2.Entitie.Status;
import tn.esprit.espritconnect2.Entitie.User;
import tn.esprit.espritconnect2.Exception.NotFoundException;
import tn.esprit.espritconnect2.Repository.AlumniRepository;
import tn.esprit.espritconnect2.Repository.EtudiantRepository;
import tn.esprit.espritconnect2.Repository.UserRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserApprovalServiceImpl implements IUserApprovalService {

    private final UserRepository userRepository;
    private final EtudiantRepository etudiantRepository;
    private final AlumniRepository alumniRepository;
    private final IEmailService emailService;
    private final ApprovalSettingsService approvalSettingsService;

    private static final String[] AVATAR_COLORS = {
        "#E53935", "#D81B60", "#8E24AA", "#5E35B1", "#3949AB",
        "#1E88E5", "#039BE5", "#00ACC1", "#00897B", "#43A047",
        "#7CB342", "#C0CA33", "#FDD835", "#FFB300", "#FB8C00",
        "#F4511E", "#6D4C41", "#757575", "#546E7A"
    };

    @Override
    public List<UserApprovalDTO> getPendingUsers() {
        return processPendingUsers(userRepository.findByEnabledFalse());
    }

    @Override
    public List<UserApprovalDTO> getPendingUsersByRole(Role role) {
        return processPendingUsers(userRepository.findByRoleAndEnabledFalse(role));
    }

    @Override
    public List<UserApprovalDTO> searchPendingUsers(String search, Role role) {
        List<User> users;
        if (search == null || search.trim().isEmpty()) {
            if (role == null) {
                users = userRepository.findByEnabledFalse();
            } else {
                users = userRepository.findByRoleAndEnabledFalse(role);
            }
        } else {
            if (role == null) {
                users = userRepository.searchPendingUsers(search.trim());
            } else {
                users = userRepository.searchPendingUsersByRole(search.trim(), role);
            }
        }
        return processPendingUsers(users);
    }

    /**
     * Vérifie et applique l'auto-approbation pour une liste d'utilisateurs en attente.
     * Utile pour synchroniser les changements faits directement en base de données.
     */
    private List<UserApprovalDTO> processPendingUsers(List<User> users) {
        if (users.isEmpty()) return Collections.emptyList();

        List<User> toAutoApprove = users.stream()
                .filter(u -> !u.isEnabled() && approvalSettingsService.shouldAutoApprove(u.getEmail()))
                .collect(Collectors.toList());

        if (!toAutoApprove.isEmpty()) {
            toAutoApprove.forEach(u -> {
                u.setEnabled(true);
                u.setStatus(Status.ACCEPTEE);
                emailService.sendApprovalNotification(u);
            });
            userRepository.saveAll(toAutoApprove);
            users.removeAll(toAutoApprove);
        }

        return users.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserApprovalDTO approveUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
        
        user.setEnabled(true);
        user.setStatus(Status.ACCEPTEE);
        User savedUser = userRepository.save(user);
        
        emailService.sendApprovalNotification(savedUser);
        
        return mapToDTO(savedUser);
    }

    @Override
    public void declineUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
        
        emailService.sendDeclineNotification(user);
        
        deleteRelatedEntities(user);
        userRepository.delete(user);
    }

    @Override
    public void deleteUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
        
        deleteRelatedEntities(user);
        userRepository.delete(user);
    }

    @Override
    public List<UserApprovalDTO> bulkApprove(List<UUID> userIds) {
        List<User> users = userRepository.findByIdIn(userIds);
        
        users.forEach(user -> {
            user.setEnabled(true);
            user.setStatus(Status.ACCEPTEE);
        });
        List<User> savedUsers = userRepository.saveAll(users);
        
        savedUsers.forEach(emailService::sendApprovalNotification);
        
        return savedUsers.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void bulkDecline(List<UUID> userIds) {
        List<User> users = userRepository.findByIdIn(userIds);
        
        users.forEach(emailService::sendDeclineNotification);
        users.forEach(this::deleteRelatedEntities);
        userRepository.deleteAll(users);
    }

    @Override
    public UserApprovalStatsDTO getApprovalStats() {
        return UserApprovalStatsDTO.builder()
                .pendingCount(userRepository.countByEnabledFalse())
                .approvedCount(userRepository.countByEnabledTrue())
                .totalStudents(userRepository.countByRole(Role.ETUDIANT))
                .totalAlumni(userRepository.countByRole(Role.ALUMNI))
                .build();
    }

    @Override
    public List<UserApprovalDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .filter(u -> u.getRole() != Role.ADMIN)
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserApprovalDTO> getApprovedUsers() {
        return userRepository.findByEnabledTrue()
                .stream()
                .filter(u -> u.getRole() != Role.ADMIN)
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private UserApprovalDTO mapToDTO(User user) {
        String affiliation = getAffiliation(user);
        String initials = getInitials(user.getNom());
        String color = getAvatarColor(user.getEmail());
        
        return UserApprovalDTO.builder()
                .id(user.getId())
                .nom(user.getNom())
                .email(user.getEmail())
                .role(user.getRole())
                .affiliation(affiliation)
                .registrationDate(user.getCreatedAt())
                .status(user.getStatus() != null ? user.getStatus().name() : "EN_ATTENTE")
                .avatarInitials(initials)
                .avatarColor(color)
                .build();
    }

    private String getAffiliation(User user) {
        if (user.getRole() == Role.ETUDIANT) {
            Optional<Etudiant> etudiant = etudiantRepository.findByEmail(user.getEmail());
            if (etudiant.isPresent()) {
                String filiere = etudiant.get().getFiliere();
                return "Student" + (filiere != null ? " - " + filiere : "");
            }
            return "Student";
        } else if (user.getRole() == Role.ALUMNI) {
            Optional<Alumni> alumni = alumniRepository.findAll().stream()
                    .filter(a -> a.getEmail().equals(user.getEmail()))
                    .findFirst();
            if (alumni.isPresent()) {
                String domaine = alumni.get().getDomaine();
                return "Alumni" + (domaine != null ? " - " + domaine : "");
            }
            return "Alumni";
        } else if (user.getRole() == Role.ENTREPRISE) {
            return "Enterprise";
        }
        return user.getRole().name();
    }

    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "?";
        }
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].charAt(0) + "" + parts[parts.length - 1].charAt(0)).toUpperCase();
        }
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }

    private String getAvatarColor(String email) {
        if (email == null) {
            return AVATAR_COLORS[0];
        }
        int hash = Math.abs(email.hashCode());
        return AVATAR_COLORS[hash % AVATAR_COLORS.length];
    }

    private void deleteRelatedEntities(User user) {
        if (user.getRole() == Role.ETUDIANT) {
            etudiantRepository.findByEmail(user.getEmail())
                    .ifPresent(etudiantRepository::delete);
        } else if (user.getRole() == Role.ALUMNI) {
            alumniRepository.findAll().stream()
                    .filter(a -> a.getEmail().equals(user.getEmail()))
                    .findFirst()
                    .ifPresent(alumniRepository::delete);
        }
    }
}
