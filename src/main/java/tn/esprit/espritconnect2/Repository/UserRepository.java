package tn.esprit.espritconnect2.Repository;

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
    long countByRole(Role role);

    List<User> findByEnabledFalseAndInscriptionRefuseeFalseAndRoleInOrderByCreatedAtDesc(Collection<Role> roles);

    long countByEnabledFalseAndInscriptionRefuseeFalseAndRoleIn(Collection<Role> roles);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.enabled = true AND u.inscriptionRefusee = false AND u.createdAt >= :start AND u.createdAt < :end")
    long countApprovedByRoleAndCreatedAtBetween(@Param("role") Role role, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
