package tn.esprit.espritconnect2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tn.esprit.espritconnect2.Entitie.Alumni;

import java.util.Optional;

public interface AlumniRepository extends JpaRepository<Alumni, Long> {
    boolean existsByEmail(String email);

    Optional<Alumni> findByEmail(String email);

    @Query("SELECT COUNT(a) FROM Alumni a, User u WHERE u.email = a.email AND u.enabled = true AND u.inscriptionRefusee = false")
    long countApprovedAlumni();
}