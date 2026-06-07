package tn.esprit.espritconnect2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.espritconnect2.Entitie.FaqComment;

import java.util.List;

public interface FaqCommentRepository extends JpaRepository<FaqComment, Long> {
    List<FaqComment> findByFaqIdOrderByCreatedAtAsc(Long faqId);
    long countByFaqId(Long faqId);
}
