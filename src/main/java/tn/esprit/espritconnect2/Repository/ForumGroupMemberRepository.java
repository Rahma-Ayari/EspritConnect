package tn.esprit.espritconnect2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.espritconnect2.Entitie.ForumGroupMember;
import tn.esprit.espritconnect2.Entitie.MemberStatus;
import java.util.List;
import java.util.Optional;

@Repository
public interface ForumGroupMemberRepository extends JpaRepository<ForumGroupMember, Long> {
    List<ForumGroupMember> findByGroupIdAndStatus(Long groupId, MemberStatus status);
    List<ForumGroupMember> findByUserEmail(String userEmail);
    List<ForumGroupMember> findByUserEmailAndStatus(String userEmail, MemberStatus status);
    Optional<ForumGroupMember> findByGroupIdAndUserEmail(Long groupId, String userEmail);
    boolean existsByGroupIdAndUserEmailAndStatus(Long groupId, String userEmail, MemberStatus status);
}
