package tn.esprit.espritconnect2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.espritconnect2.Entitie.Evenement;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Repository
public interface EvenementRepository extends JpaRepository<Evenement, Long> {
    List<Evenement> findByStatusInOrderByDateEvenementAsc(Collection<String> statuses);

    List<Evenement> findTop6ByDateEvenementAfterAndStatusInOrderByDateEvenementAsc(Date date, Collection<String> statuses);

    Long countByStatusIgnoreCase(String status);

    List<Evenement> findByOwnerIdOrderByDateDebutAscHeureDebutAsc(UUID ownerId);

    @Query("select coalesce(sum(e.capacite), 0) from Evenement e")
    Long sumCapacity();

    @Query("select coalesce(sum(e.nombreParticipants), 0) from Evenement e")
    Long sumParticipants();

    @Query("""
            select e from Evenement e
            where (:search is null or :search = '' or
                   lower(e.titre) like lower(concat('%', :search, '%')) or
                   lower(e.lieu) like lower(concat('%', :search, '%')) or
                   lower(e.type) like lower(concat('%', :search, '%')) or
                   lower(e.entreprise.nom) like lower(concat('%', :search, '%')))
              and (:status is null or :status = '' or lower(e.status) = lower(:status))
              and (:type is null or :type = '' or lower(e.type) = lower(:type))
            order by e.dateEvenement asc
            """)
    List<Evenement> searchEvents(
            @Param("search") String search,
            @Param("status") String status,
            @Param("type") String type
    );

    @Query("""
            select e from Evenement e
            where (e.status in :statuses or e.status is null)
              and (:search is null or :search = '' or
                   lower(e.titre) like lower(concat('%', :search, '%')) or
                   lower(e.lieu) like lower(concat('%', :search, '%')) or
                   lower(e.type) like lower(concat('%', :search, '%')) or
                   lower(e.entreprise.nom) like lower(concat('%', :search, '%')))
              and (:type is null or :type = '' or lower(e.type) = lower(:type))
            order by e.dateEvenement asc
            """)
    List<Evenement> searchPublicEvents(
            @Param("statuses") Collection<String> statuses,
            @Param("search") String search,
            @Param("type") String type
    );
}
