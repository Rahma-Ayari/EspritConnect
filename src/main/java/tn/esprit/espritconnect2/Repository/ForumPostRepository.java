package tn.esprit.espritconnect2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.espritconnect2.Entitie.ForumPost;
import tn.esprit.espritconnect2.Entitie.Role;

import java.util.List;

@Repository
public interface ForumPostRepository extends JpaRepository<ForumPost, Long>, ForumPostRepositoryCustom {

    List<ForumPost> findByReportedTrueOrderByCreatedAtDesc();

    long countByCategoryId(Long categoryId);

    @Query("SELECT COUNT(p) FROM ForumPost p WHERE p.authorRole = :role")
    long countByAuthorRole(@Param("role") Role role);
}