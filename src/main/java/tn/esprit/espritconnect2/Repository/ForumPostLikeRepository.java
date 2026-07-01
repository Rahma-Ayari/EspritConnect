package tn.esprit.espritconnect2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.espritconnect2.Entitie.ForumPostLike;

import java.util.List;
import java.util.Optional;

@Repository
public interface ForumPostLikeRepository extends JpaRepository<ForumPostLike, Long> {
    Optional<ForumPostLike> findByPostIdAndUserEmail(Long postId, String userEmail);
    boolean existsByPostIdAndUserEmail(Long postId, String userEmail);
    long countByPostId(Long postId);
    List<ForumPostLike> findByUserEmail(String userEmail);
    void deleteByPostIdAndUserEmail(Long postId, String userEmail);
}
