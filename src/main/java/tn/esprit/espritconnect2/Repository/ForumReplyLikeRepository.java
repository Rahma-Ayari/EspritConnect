package tn.esprit.espritconnect2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.espritconnect2.Entitie.ForumReplyLike;

import java.util.Optional;

public interface ForumReplyLikeRepository extends JpaRepository<ForumReplyLike, Long> {
    Optional<ForumReplyLike> findByReplyIdAndUserEmail(Long replyId, String userEmail);
}
