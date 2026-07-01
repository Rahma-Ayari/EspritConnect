package tn.esprit.espritconnect2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.espritconnect2.Entitie.ForumDiscussion;
import tn.esprit.espritconnect2.Entitie.DiscussionStatus;
import java.util.List;
import java.util.Optional;

@Repository
public interface ForumDiscussionRepository extends JpaRepository<ForumDiscussion, Long> {
    List<ForumDiscussion> findByStatus(DiscussionStatus status);
    Optional<ForumDiscussion> findByNameIgnoreCase(String name);
    List<ForumDiscussion> findByCreatorEmail(String creatorEmail);
}
