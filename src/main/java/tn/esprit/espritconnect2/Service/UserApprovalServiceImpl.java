package tn.esprit.espritconnect2.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.espritconnect2.DTO.BulkAddUsersResponse;
import tn.esprit.espritconnect2.DTO.NewUserRequest;
import tn.esprit.espritconnect2.DTO.UserApprovalDTO;
import tn.esprit.espritconnect2.DTO.UserApprovalStatsDTO;
import tn.esprit.espritconnect2.Entitie.Alumni;
import tn.esprit.espritconnect2.Entitie.Etudiant;
import tn.esprit.espritconnect2.Entitie.Role;
import tn.esprit.espritconnect2.Entitie.Status;
import tn.esprit.espritconnect2.Entitie.User;
import tn.esprit.espritconnect2.exception.NotFoundException;
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
    private final PasswordEncoder passwordEncoder;

    private static final String[] AVATAR_COLORS = {
        "#E53935", "#D81B60", "#8E24AA", "#5E35B1", "#3949AB",
        "#1E88E5", "#039BE5", "#00ACC1", "#00897B", "#43A047",
        "#7CB342", "#C0CA33", "#FDD835", "#FFB300", "#FB8C00",
        "#F4511E", "#6D4C41", "#757575", "#546E7A"
    };

    @Override
    public List<UserApprovalDTO> getPendingUsers() {
        // Filter by status EN_ATTENTE and exclude ADMIN users
        return processPendingUsers(userRepository.findByStatusAndRoleNot(Status.EN_ATTENTE, Role.ADMIN));
    }

    @Override
    public List<UserApprovalDTO> getPendingUsersByRole(Role role) {
        return processPendingUsers(userRepository.findByRoleAndStatus(role, Status.EN_ATTENTE));
    }

    @Override
    public List<UserApprovalDTO> searchPendingUsers(String search, Role role) {
        List<User> users;
        if (search == null || search.trim().isEmpty()) {
            if (role == null) {
                users = userRepository.findByStatusAndRoleNot(Status.EN_ATTENTE, Role.ADMIN);
            } else {
                users = userRepository.findByRoleAndStatus(role, Status.EN_ATTENTE);
            }
        } else {
            if (role == null) {
                users = userRepository.searchPendingUsersByStatus(search.trim(), Status.EN_ATTENTE);
            } else {
                users = userRepository.searchPendingUsersByRoleAndStatus(search.trim(), role, Status.EN_ATTENTE);
            }
        }
        return processPendingUsers(users);
    }

    /**
     * Résout la liste des utilisateurs réellement en attente d'approbation manuelle.
     * Les comptes éligibles à l'auto-approbation sont exclus (et optionnellement approuvés).
     */
    private List<User> resolvePendingUsers(List<User> users, boolean applyAutoApproval) {
        if (users.isEmpty()) {
            return Collections.emptyList();
        }

        boolean emailVerificationRequired = approvalSettingsService.getSettings().isRequireEmailVerification();
        List<User> toAutoApprove = users.stream()
                .filter(u -> approvalSettingsService.shouldAutoApprove(u.getEmail()))
                .filter(u -> !emailVerificationRequired || u.isEmailVerified())
                .collect(Collectors.toList());

        if (applyAutoApproval && !toAutoApprove.isEmpty()) {
            toAutoApprove.forEach(u -> {
                approvalSettingsService.applyAutoApproval(u);
                emailService.sendApprovalNotification(u);
            });
            userRepository.saveAll(toAutoApprove);
        }

        List<User> remaining = new ArrayList<>(users);
        remaining.removeAll(toAutoApprove);
        return remaining;
    }

    private List<UserApprovalDTO> processPendingUsers(List<User> users) {
        return resolvePendingUsers(users, true).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private long countPendingUsers() {
        return resolvePendingUsers(
                userRepository.findByStatusAndRoleNot(Status.EN_ATTENTE, Role.ADMIN),
                false
        ).size();
    }

    @Override
    public UserApprovalDTO approveUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
        
        user.setEnabled(true);
        user.setStatus(Status.ACCEPTEE);
        if (user.getRole() == Role.ENTREPRISE) {
            user.setVerificationStatus(tn.esprit.espritconnect2.Entitie.VerificationStatus.VERIFIED);
            if (user.getVerifiedAt() == null) {
                user.setVerifiedAt(java.time.LocalDateTime.now());
                user.setVerifiedBy("Admin");
            }
        }
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
            if (user.getRole() == Role.ENTREPRISE) {
                user.setVerificationStatus(tn.esprit.espritconnect2.Entitie.VerificationStatus.VERIFIED);
                if (user.getVerifiedAt() == null) {
                    user.setVerifiedAt(java.time.LocalDateTime.now());
                    user.setVerifiedBy("Admin Bulk");
                }
            }
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
                .pendingCount(countPendingUsers())
                .approvedCount(userRepository.countByStatus(Status.ACCEPTEE))
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

    @Override
    public UserApprovalDTO addUser(NewUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Un utilisateur avec cet email existe déjà: " + request.getEmail());
        }

        String tempPassword = generateTemporaryPassword();
        
        User user = User.builder()
                .nom(request.getNom())
                .email(request.getEmail())
                .password(passwordEncoder.encode(tempPassword))
                .role(request.getRoleEnum())
                .status(Status.EN_ATTENTE)
                .enabled(false)
                .emailVerified(true)
                .build();

        User savedUser = userRepository.save(user);

        createRoleSpecificEntity(savedUser, request.getAffiliation());

        emailService.sendWelcomeEmailWithTemporaryPassword(savedUser, tempPassword);

        return mapToDTO(savedUser);
    }

    @Override
    public BulkAddUsersResponse bulkAddUsers(List<NewUserRequest> users) {
        List<UserApprovalDTO> addedUsers = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        int failedCount = 0;

        for (int i = 0; i < users.size(); i++) {
            NewUserRequest request = users.get(i);
            try {
                if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                    errors.add("Ligne " + (i + 1) + ": Email manquant");
                    failedCount++;
                    continue;
                }
                if (request.getNom() == null || request.getNom().trim().isEmpty()) {
                    errors.add("Ligne " + (i + 1) + ": Nom manquant");
                    failedCount++;
                    continue;
                }
                if (userRepository.existsByEmail(request.getEmail())) {
                    errors.add("Ligne " + (i + 1) + ": Email déjà utilisé (" + request.getEmail() + ")");
                    failedCount++;
                    continue;
                }

                UserApprovalDTO added = addUser(request);
                addedUsers.add(added);
                successCount++;
            } catch (Exception e) {
                errors.add("Ligne " + (i + 1) + ": " + e.getMessage());
                failedCount++;
            }
        }

        return BulkAddUsersResponse.builder()
                .successCount(successCount)
                .failedCount(failedCount)
                .errors(errors)
                .users(addedUsers)
                .build();
    }

    private void createRoleSpecificEntity(User user, String affiliation) {
        if (user.getRole() == Role.ETUDIANT) {
            Etudiant etudiant = new Etudiant();
            etudiant.setNom(user.getNom());
            etudiant.setEmail(user.getEmail());
            etudiant.setPassword(user.getPassword());
            etudiant.setDateInscription(new java.util.Date());
            if (affiliation != null && affiliation.contains(" - ")) {
                String[] parts = affiliation.split(" - ");
                if (parts.length > 1) {
                    etudiant.setFiliere(parts[1]);
                }
            }
            etudiantRepository.save(etudiant);
        } else if (user.getRole() == Role.ALUMNI) {
            Alumni alumni = new Alumni();
            alumni.setNom(user.getNom());
            alumni.setEmail(user.getEmail());
            alumni.setPassword(user.getPassword());
            alumni.setDisponibleMentorat(false);
            if (affiliation != null && affiliation.contains(" - ")) {
                String[] parts = affiliation.split(" - ");
                if (parts.length > 1) {
                    alumni.setDomaine(parts[1]);
                }
            }
            alumniRepository.save(alumni);
        }
    }

    private String generateTemporaryPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        StringBuilder password = new StringBuilder();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < 12; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }
}
