package tn.esprit.espritconnect2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.espritconnect2.Entitie.ForumPostLike;

import java.util.Optional;

@Repository
public interface ForumPostLikeRepository extends JpaRepository<ForumPostLike, Long> {

    Optional<ForumPostLike> findByPostIdAndUserEmailIgnoreCase(Long postId, String userEmail);

    boolean existsByPostIdAndUserEmailIgnoreCase(Long postId, String userEmail);

    long countByPostId(Long postId);
}
