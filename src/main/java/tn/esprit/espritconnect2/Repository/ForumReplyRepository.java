package tn.esprit.espritconnect2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.espritconnect2.Entitie.ForumReply;
import tn.esprit.espritconnect2.Entitie.Role;

import java.util.List;

@Repository
public interface ForumReplyRepository extends JpaRepository<ForumReply, Long> {

    List<ForumReply> findByReportedTrueOrderByCreatedAtDesc();

    @Query("SELECT COUNT(r) FROM ForumReply r WHERE r.authorRole = :role")
    long countByAuthorRole(@Param("role") Role role);
}
