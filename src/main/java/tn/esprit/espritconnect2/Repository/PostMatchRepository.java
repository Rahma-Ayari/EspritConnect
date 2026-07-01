package tn.esprit.espritconnect2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.espritconnect2.Entitie.PostMatch;

import java.util.List;

@Repository
public interface PostMatchRepository extends JpaRepository<PostMatch, Long> {

    List<PostMatch> findByMatchedUserEmailIgnoreCaseOrderByScoreDescCreatedAtDesc(String email);

    List<PostMatch> findByPostId(Long postId);

    void deleteByPostId(Long postId);
}
