package tn.esprit.espritconnect2.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.espritconnect2.Entitie.Role;
import tn.esprit.espritconnect2.Entitie.Status;
import tn.esprit.espritconnect2.Entitie.User;
import tn.esprit.espritconnect2.Entitie.VerificationStatus;

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

    // Pending users by status EN_ATTENTE (correct filtering)
    List<User> findByStatus(Status status);
    List<User> findByStatusAndRoleNot(Status status, Role role);
    long countByStatus(Status status);
    long countByStatusAndRoleNot(Status status, Role role);

    // Approved users (enabled = true)
    List<User> findByEnabledTrue();
    long countByEnabledTrue();

    // By role
    List<User> findByRole(Role role);
    List<User> findByRoleAndEnabledFalse(Role role);
    List<User> findByRoleAndStatus(Role role, Status status);
    long countByRole(Role role);
    long countByRoleAndEnabledFalse(Role role);

    // Search pending users by name or email (with status check)
    @Query("SELECT u FROM User u WHERE u.status = :status AND u.role <> 'ADMIN' AND " +
           "(LOWER(u.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<User> searchPendingUsersByStatus(@Param("search") String search, @Param("status") Status status);

    // Search pending users by name or email (old method kept for compatibility)
    @Query("SELECT u FROM User u WHERE u.enabled = false AND " +
           "(LOWER(u.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<User> searchPendingUsers(@Param("search") String search);

    // Search pending users by name, email, role and status
    @Query("SELECT u FROM User u WHERE u.status = :status AND " +
           "(LOWER(u.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:role IS NULL OR u.role = :role)")
    List<User> searchPendingUsersByRoleAndStatus(@Param("search") String search, @Param("role") Role role, @Param("status") Status status);

    // Search pending users by name, email, or role (old method kept for compatibility)
    @Query("SELECT u FROM User u WHERE u.enabled = false AND " +
           "(LOWER(u.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:role IS NULL OR u.role = :role)")
    List<User> searchPendingUsersByRole(@Param("search") String search, @Param("role") Role role);

    // Bulk operations
    @Query("SELECT u FROM User u WHERE u.id IN :ids")
    List<User> findByIdIn(@Param("ids") List<UUID> ids);

    // Enterprise verification queries
    List<User> findByRoleAndVerificationStatus(Role role, VerificationStatus verificationStatus);
    
    long countByRoleAndVerificationStatus(Role role, VerificationStatus verificationStatus);

    @Query("SELECT u FROM User u WHERE u.role = :role AND " +
           "(LOWER(u.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.businessRegistrationNumber) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<User> searchEnterprisesByText(@Param("role") Role role, @Param("search") String search);

    @Query("SELECT u FROM User u WHERE u.role = :role AND u.verificationStatus = :status AND " +
           "(LOWER(u.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.businessRegistrationNumber) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<User> searchEnterprisesByTextAndStatus(
            @Param("role") Role role, 
            @Param("search") String search, 
            @Param("status") VerificationStatus status);

    /** Anciens comptes : email_verified NULL → false avant chargement JPA. */
    @Modifying
    @Query(value = "UPDATE users SET email_verified = 0 WHERE email_verified IS NULL", nativeQuery = true)
    int backfillNullEmailVerified();
}
