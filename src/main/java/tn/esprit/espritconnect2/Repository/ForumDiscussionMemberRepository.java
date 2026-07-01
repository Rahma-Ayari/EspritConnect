package tn.esprit.espritconnect2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.espritconnect2.Entitie.ForumDiscussionMember;
import tn.esprit.espritconnect2.Entitie.MemberStatus;
import java.util.List;
import java.util.Optional;

@Repository
public interface ForumDiscussionMemberRepository extends JpaRepository<ForumDiscussionMember, Long> {
    List<ForumDiscussionMember> findByDiscussionIdAndStatus(Long discussionId, MemberStatus status);
    List<ForumDiscussionMember> findByUserEmail(String userEmail);
    List<ForumDiscussionMember> findByUserEmailAndStatus(String userEmail, MemberStatus status);
    Optional<ForumDiscussionMember> findByDiscussionIdAndUserEmail(Long discussionId, String userEmail);
    boolean existsByDiscussionIdAndUserEmailAndStatus(Long discussionId, String userEmail, MemberStatus status);
}
