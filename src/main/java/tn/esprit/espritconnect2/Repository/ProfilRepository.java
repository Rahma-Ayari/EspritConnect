package tn.esprit.espritconnect2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.espritconnect2.Entitie.Profil;

import java.util.Optional;

/**
 * Repository JPA pour l'entité Profil.
 * JpaRepository fournit automatiquement : save, findById, findAll, deleteById, existsById, etc.
 */
public interface ProfilRepository extends JpaRepository<Profil, Long> {

    // Trouver un profil par userId (identifiant métier de l'utilisateur)
    Optional<Profil> findByUserId(String userId);

    // Vérifier si un userId est déjà utilisé (éviter les doublons)
    boolean existsByUserId(String userId);

    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Modifying
    void deleteByUserId(String userId);
}