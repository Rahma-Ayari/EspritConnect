package tn.esprit.espritconnect2.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.espritconnect2.Entitie.Etudiant;
import tn.esprit.espritconnect2.Entitie.Niveau;
import tn.esprit.espritconnect2.Entitie.Role;
import tn.esprit.espritconnect2.Entitie.User;
import tn.esprit.espritconnect2.Repository.EtudiantRepository;
import tn.esprit.espritconnect2.Repository.UserRepository;
import tn.esprit.espritconnect2.exception.BusinessRuleException;

import java.util.Date;

/**
 * Resolves the {@link Etudiant} row for a logged-in account.
 * Some users exist in {@link User} with role ETUDIANT but never received a matching etudiant row
 * (admin approval, legacy data, etc.). This service auto-provisions the missing profile.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StudentAccountResolver {

    private final UserRepository userRepository;
    private final EtudiantRepository etudiantRepository;

    @Transactional
    public Etudiant resolveOrProvision(String email) {
        return etudiantRepository.findByEmail(email)
                .orElseGet(() -> provisionFromUser(email));
    }

    @Transactional
    public Etudiant provisionFromUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessRuleException("Account not found for email: " + email));

        if (user.getRole() != Role.ETUDIANT) {
            throw new BusinessRuleException(
                    "Student profile not found. This feature is available for student accounts only.");
        }

        Etudiant etudiant = new Etudiant();
        etudiant.setNom(user.getNom());
        etudiant.setEmail(user.getEmail());
        etudiant.setPassword(user.getPassword());
        etudiant.setNiveau(Niveau.DEBUTANT);
        etudiant.setScoreReadiness(0);
        etudiant.setDateInscription(new Date());

        Etudiant saved = etudiantRepository.save(etudiant);
        log.info("Auto-provisioned Etudiant profile for user {}", email);
        return saved;
    }

    /**
     * Backfill etudiant rows for all ETUDIANT users missing one (safe to run on startup).
     */
    @Transactional
    public int syncMissingProfiles() {
        int created = 0;
        for (User user : userRepository.findAll()) {
            if (user.getRole() != Role.ETUDIANT) {
                continue;
            }
            if (etudiantRepository.findByEmail(user.getEmail()).isEmpty()) {
                provisionFromUser(user.getEmail());
                created++;
            }
        }
        if (created > 0) {
            log.info("Student profile sync: created {} missing etudiant row(s)", created);
        }
        return created;
    }
}
