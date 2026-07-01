package tn.esprit.espritconnect2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.espritconnect2.Entitie.ForumGroup;
import tn.esprit.espritconnect2.Entitie.GroupStatus;
import java.util.List;

@Repository
public interface ForumGroupRepository extends JpaRepository<ForumGroup, Long> {
    List<ForumGroup> findByStatus(GroupStatus status);
}
