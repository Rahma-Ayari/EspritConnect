package tn.esprit.espritconnect2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.espritconnect2.Entitie.TypeEvenement;

import java.util.List;
import java.util.Optional;

@Repository
public interface TypeEvenementRepository extends JpaRepository<TypeEvenement, Long> {
    boolean existsByNomIgnoreCase(String nom);
    Optional<TypeEvenement> findByNomIgnoreCase(String nom);
    List<TypeEvenement> findByActifTrueOrderByNomAsc();
    List<TypeEvenement> findAllByOrderByNomAsc();
}
