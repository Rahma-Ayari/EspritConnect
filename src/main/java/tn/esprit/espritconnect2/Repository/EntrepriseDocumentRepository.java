package tn.esprit.espritconnect2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.espritconnect2.Entitie.EntrepriseDocument;

import java.util.List;

public interface EntrepriseDocumentRepository extends JpaRepository<EntrepriseDocument, Long> {
    List<EntrepriseDocument> findByEntrepriseIdEntrepriseOrderByUploadedAtDesc(Long entrepriseId);
    long countByEntrepriseIdEntreprise(Long entrepriseId);
}
