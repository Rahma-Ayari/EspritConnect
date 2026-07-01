package tn.esprit.espritconnect2.Repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.espritconnect2.Entitie.ForumPost;
import tn.esprit.espritconnect2.Entitie.PostStatus;
import tn.esprit.espritconnect2.Entitie.Role;

import java.util.List;

@Repository
public interface ForumPostRepository extends JpaRepository<ForumPost, Long>, ForumPostRepositoryCustom {

    List<ForumPost> findByReportedTrueOrderByCreatedAtDesc();

    long countByCategoryId(Long categoryId);

    @Query("SELECT COUNT(p) FROM ForumPost p WHERE p.authorRole = :role")
    long countByAuthorRole(@Param("role") Role role);

    List<ForumPost> findByAuthorEmailIgnoreCaseAndReportedFalseOrderByCreatedAtDesc(String authorEmail);

    List<ForumPost> findByReportedFalseOrderByPinnedDescCreatedAtDesc();

    List<ForumPost> findByCategoryIdAndReportedFalseOrderByPinnedDescCreatedAtDesc(Long categoryId);

    List<ForumPost> findByAuthorEmailIgnoreCaseAndStatusOrderByUpdatedAtDesc(String authorEmail, PostStatus status);

    List<ForumPost> findByStatusOrderByCreatedAtDesc(PostStatus status);

    List<ForumPost> findByForumDiscussionIdAndReportedFalseOrderByCreatedAtDesc(Long discussionId);

    @Query("SELECT DISTINCT p FROM ForumPost p LEFT JOIN FETCH p.replies WHERE p.forumDiscussion.id = :discussionId AND p.reported = false AND p.status = tn.esprit.espritconnect2.Entitie.PostStatus.PUBLISHED ORDER BY p.createdAt ASC")
    List<ForumPost> findDiscussionPostsWithReplies(@Param("discussionId") Long discussionId);

    @EntityGraph(attributePaths = {"category", "replies"})
    @Query("SELECT p FROM ForumPost p WHERE p.id IN :ids")
    List<ForumPost> findByIdsWithDetails(@Param("ids") List<Long> ids);

    @Query("""
            SELECT p FROM ForumPost p
            LEFT JOIN FETCH p.category
            LEFT JOIN FETCH p.forumDiscussion
            WHERE p.status = :status
            AND p.reported = false
            AND LOWER(p.authorEmail) <> LOWER(:email)
            ORDER BY p.createdAt DESC
            """)
    List<ForumPost> findPublishedPostsExcludingAuthor(
            @Param("status") PostStatus status,
            @Param("email") String email,
            org.springframework.data.domain.Pageable pageable);
}
