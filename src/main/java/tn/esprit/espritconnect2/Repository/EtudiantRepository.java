package tn.esprit.espritconnect2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.espritconnect2.Entitie.Etudiant;

import java.util.Optional;
import java.util.Date;

public interface EtudiantRepository extends JpaRepository<Etudiant,Long> {
    // Vérifier si un email existe déjà (utile pour la validation)
    boolean existsByEmail(String email);

    // Trouver par email (utile pour Spring Security plus tard)
    Optional<Etudiant> findByEmail(String email);

    long countByDateInscriptionBetween(Date startDate, Date endDate);

    @Query("SELECT COUNT(e) FROM Etudiant e, User u WHERE u.email = e.email AND u.enabled = true AND u.inscriptionRefusee = false")
    long countApprovedEtudiants();

    @Query("SELECT COUNT(e) FROM Etudiant e, User u WHERE u.email = e.email AND u.enabled = true AND u.inscriptionRefusee = false AND e.dateInscription >= :start AND e.dateInscription < :end")
    long countApprovedByDateInscriptionBetween(@Param("start") Date start, @Param("end") Date end);
}
