package tn.esprit.espritconnect2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tn.esprit.espritconnect2.Entitie.Administrateur;

import java.util.List;
import java.util.Optional;

/**
 * Repository JPA pour l'entité Administrateur.
 * JpaRepository fournit automatiquement : save, findById, findAll, deleteById, existsById, etc.
 */
public interface AdministrateurRepository extends JpaRepository<Administrateur, Long> {

    boolean existsByEmail(String email);

    Optional<Administrateur> findByEmail(String email);

    @Query("SELECT a.email FROM Administrateur a WHERE a.email IS NOT NULL")
    List<String> findAllEmails();
}