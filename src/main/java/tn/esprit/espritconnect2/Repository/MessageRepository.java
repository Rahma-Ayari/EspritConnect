package tn.esprit.espritconnect2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.espritconnect2.Entitie.Message;

import java.util.List;

/**
 * Repository JPA pour l'entité Message.
 * Spring Data génère automatiquement toutes les implémentations.
 */
public interface MessageRepository extends JpaRepository<Message, Long> {

    // Récupérer tous les messages d'un profil donné (par idProfil)
    List<Message> findByProfil_IdProfil(Long idProfil);

    // Récupérer tous les messages d'un userId donné
    List<Message> findByUserId(String userId);

    // Récupérer tous les messages non lus d'un profil (lu = false)
    List<Message> findByProfil_IdProfilAndLuFalse(Long idProfil);

    // Compter les messages non lus d'un profil (utile pour badge notifications)
    long countByProfil_IdProfilAndLuFalse(Long idProfil);
}