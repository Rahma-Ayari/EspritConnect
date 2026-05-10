package tn.esprit.espritconnect2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.espritconnect2.Entitie.Administrateur;

import java.util.Optional;

/**
 * Repository JPA pour l'entité Administrateur.
 * JpaRepository fournit automatiquement : save, findById, findAll, deleteById, existsById, etc.
 */
public interface AdministrateurRepository extends JpaRepository<Administrateur, Long> {

    // Vérifie si un admin avec cet email existe déjà (éviter les doublons)
    boolean existsByEmail(String email);

    // Trouve un admin par email (utile pour Spring Security plus tard)
    Optional<Administrateur> findByEmail(String email);
}