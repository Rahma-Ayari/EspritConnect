package tn.esprit.espritconnect2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.espritconnect2.Entitie.LoginHistory;
import tn.esprit.espritconnect2.Entitie.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {
    List<LoginHistory> findTop10ByUserOrderByLoginTimeDesc(User user);

    Optional<LoginHistory> findFirstByUserAndStatusInOrderByLoginTimeDesc(User user, Collection<String> statuses);
    
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Modifying
    void deleteByUser(User user);
}
