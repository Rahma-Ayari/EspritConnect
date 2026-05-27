package tn.esprit.espritconnect2.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.espritconnect2.Entitie.Role;
import tn.esprit.espritconnect2.Entitie.User;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    // Pending users (enabled = false)
    List<User> findByEnabledFalse();
    Page<User> findByEnabledFalse(Pageable pageable);
    long countByEnabledFalse();

    // Approved users (enabled = true)
    List<User> findByEnabledTrue();
    long countByEnabledTrue();

    // By role
    List<User> findByRole(Role role);
    List<User> findByRoleAndEnabledFalse(Role role);
    long countByRole(Role role);
    long countByRoleAndEnabledFalse(Role role);

    // Search pending users by name or email
    @Query("SELECT u FROM User u WHERE u.enabled = false AND " +
           "(LOWER(u.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<User> searchPendingUsers(@Param("search") String search);

    // Search pending users by name, email, or role
    @Query("SELECT u FROM User u WHERE u.enabled = false AND " +
           "(LOWER(u.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:role IS NULL OR u.role = :role)")
    List<User> searchPendingUsersByRole(@Param("search") String search, @Param("role") Role role);

    // Bulk operations
    @Query("SELECT u FROM User u WHERE u.id IN :ids")
    List<User> findByIdIn(@Param("ids") List<UUID> ids);

    // Admin dashboard: pending students/alumni (not enabled, not refused)
    long countByEnabledFalseAndInscriptionRefuseeFalseAndRoleIn(Collection<Role> roles);

    List<User> findByEnabledFalseAndInscriptionRefuseeFalseAndRoleInOrderByCreatedAtDesc(Collection<Role> roles);

    @Query("SELECT COUNT(u) FROM User u WHERE u.enabled = true AND u.inscriptionRefusee = false " +
           "AND u.role = :role AND u.createdAt >= :start AND u.createdAt < :end")
    long countApprovedByRoleAndCreatedAtBetween(
            @Param("role") Role role,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
