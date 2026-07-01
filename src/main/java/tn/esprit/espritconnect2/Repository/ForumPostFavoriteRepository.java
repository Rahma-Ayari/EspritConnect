package tn.esprit.espritconnect2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tn.esprit.espritconnect2.Entitie.ForumPost;
import tn.esprit.espritconnect2.Entitie.ForumPostFavorite;

import java.util.List;
import java.util.Optional;

@Repository
public interface ForumPostFavoriteRepository extends JpaRepository<ForumPostFavorite, Long> {
    Optional<ForumPostFavorite> findByPostIdAndUserEmail(Long postId, String userEmail);
    boolean existsByPostIdAndUserEmail(Long postId, String userEmail);
    List<ForumPostFavorite> findByUserEmailOrderByCreatedAtDesc(String userEmail);
    void deleteByPostIdAndUserEmail(Long postId, String userEmail);

    @Query("SELECT DISTINCT f.post FROM ForumPostFavorite f " +
           "LEFT JOIN FETCH f.post.category " +
           "LEFT JOIN FETCH f.post.replies " +
           "LEFT JOIN FETCH f.post.forumDiscussion " +
           "WHERE f.userEmail = :userEmail " +
           "ORDER BY f.createdAt DESC")
    List<ForumPost> findPostsWithDetailsByUserEmail(String userEmail);
}
