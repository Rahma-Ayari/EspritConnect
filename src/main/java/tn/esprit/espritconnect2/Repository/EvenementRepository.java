package tn.esprit.espritconnect2.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.espritconnect2.Entitie.Evenement;
import tn.esprit.espritconnect2.Entitie.ListeAttente;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Repository
public interface EvenementRepository extends JpaRepository<Evenement, Long> {
    List<Evenement> findTop10ByDateEvenementAfterOrderByDateEvenementDesc(Date after);
    List<Evenement> findByStatusInOrderByDateEvenementAsc(Collection<String> statuses);
    List<Evenement> findTop6ByDateEvenementAfterAndStatusInOrderByDateEvenementAsc(Date date, Collection<String> statuses);
    Long countByStatusIgnoreCase(String status);
    List<Evenement> findByOwnerIdOrderByDateDebutAscHeureDebutAsc(UUID ownerId);
    Page<Evenement> findByStatusIgnoreCase(String status, Pageable pageable);
    Page<Evenement> findByStatusInAndDateFinBefore(Collection<String> statuses, java.time.LocalDate date, Pageable pageable);

    @Query("select coalesce(sum(e.capacite), 0) from Evenement e")
    Long sumCapacity();

    @Query("select coalesce(sum(e.nombreParticipants), 0) from Evenement e")
    Long sumParticipants();

    @Query("""
            select e from Evenement e
            left join e.entreprise ent
            where (:search is null or :search = '' or
                   lower(e.titre) like lower(concat('%', :search, '%')) or
                   lower(coalesce(e.lieu, '')) like lower(concat('%', :search, '%')) or
                   lower(coalesce(e.type, '')) like lower(concat('%', :search, '%')) or
                   lower(coalesce(ent.nom, '')) like lower(concat('%', :search, '%')))
              and (:status is null or :status = '' or lower(e.status) = lower(:status))
              and (:type is null or :type = '' or lower(coalesce(e.type, '')) = lower(:type))
            order by e.dateEvenement asc
            """)
    List<Evenement> searchEvents(
            @Param("search") String search,
            @Param("status") String status,
            @Param("type") String type
    );

    @Query("""
            select e from Evenement e
            left join e.entreprise ent
            where (:search is null or :search = '' or
                   lower(e.titre) like lower(concat('%', :search, '%')) or
                   lower(coalesce(e.lieu, '')) like lower(concat('%', :search, '%')) or
                   lower(coalesce(e.type, '')) like lower(concat('%', :search, '%')) or
                   lower(coalesce(ent.nom, '')) like lower(concat('%', :search, '%')))
              and (:status is null or :status = '' or lower(e.status) = lower(:status))
              and (:type is null or :type = '' or lower(coalesce(e.type, '')) = lower(:type))
            order by e.dateEvenement asc
            """)
    Page<Evenement> searchEvents(
            @Param("search") String search,
            @Param("status") String status,
            @Param("type") String type,
            Pageable pageable
    );

    @Query("""
            select e from Evenement e
            left join e.entreprise ent
            where (e.status in :statuses or e.status is null)
              and (:search is null or :search = '' or
                   lower(e.titre) like lower(concat('%', :search, '%')) or
                   lower(coalesce(e.lieu, '')) like lower(concat('%', :search, '%')) or
                   lower(coalesce(e.type, '')) like lower(concat('%', :search, '%')) or
                   lower(coalesce(ent.nom, '')) like lower(concat('%', :search, '%')))
              and (:type is null or :type = '' or lower(coalesce(e.type, '')) = lower(:type))
            order by e.dateEvenement asc
            """)
    List<Evenement> searchPublicEvents(
            @Param("statuses") Collection<String> statuses,
            @Param("search") String search,
            @Param("type") String type
    );

    @Query("select type.nom from TypeEvenement type where type.actif = true")
    List<String> findActiveTypeNames();

    @Query("""
            select e.typeEvenement.nom, count(distinct p.user) as users
            from Evenement e
            join e.participations p
            where e.typeEvenement is not null and e.typeEvenement.actif = true
            group by e.typeEvenement.nom
            order by users desc
            """)
    List<Object[]> countUsersByEventType();

    @Query("""
            select e.typeEvenement.nom, e
            from Evenement e
            where e.typeEvenement is not null
              and e.typeEvenement.actif = true
              and (e.status = 'ACTIVE' or e.status = 'UPCOMING')
            """)
    List<Object[]> findActiveEventsByType();

    default long countActiveEventsByType(String typeName) {
        return findActiveEventsByType().stream()
                .filter(row -> typeName.equalsIgnoreCase((String) row[0]))
                .map(row -> ((Evenement) row[1]))
                .count();
    }
}
