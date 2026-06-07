package tn.esprit.espritconnect2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.espritconnect2.Entitie.EmailVerificationToken;
import tn.esprit.espritconnect2.Entitie.User;

import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {

    Optional<EmailVerificationToken> findByTokenAndUsedAtIsNull(String token);

    Optional<EmailVerificationToken> findTopByUserAndUsedAtIsNullOrderByCreatedAtDesc(User user);

    @Modifying
    @Query("UPDATE EmailVerificationToken t SET t.usedAt = CURRENT_TIMESTAMP WHERE t.user = :user AND t.usedAt IS NULL")
    void invalidateActiveTokensForUser(@Param("user") User user);
}
