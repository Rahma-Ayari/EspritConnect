package tn.esprit.espritconnect2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tn.esprit.espritconnect2.Entitie.CaptchaChallenge;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CaptchaChallengeRepository extends JpaRepository<CaptchaChallenge, Long> {

    @Modifying
    @Query("DELETE FROM CaptchaChallenge c WHERE c.expirationDate < :cutoff OR (c.consumed = true AND c.verifiedAt < :consumedCutoff)")
    int deleteExpiredOrOldConsumed(LocalDateTime cutoff, LocalDateTime consumedCutoff);

    long countByClientIpAndCreatedAtAfter(String clientIp, LocalDateTime since);
}
