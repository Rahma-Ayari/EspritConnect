package tn.esprit.espritconnect2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.espritconnect2.Entitie.FAQ;

import java.util.List;

public interface FAQRepository extends JpaRepository<FAQ, Long> {

    List<FAQ> findByQuestionContainingIgnoreCaseOrAnswerContainingIgnoreCase(String query1, String query2);

    List<FAQ> findByCategoryId(Long categoryId);

    List<FAQ> findByIsImportantTrue();

    List<FAQ> findTop5ByOrderByViewCountDesc();

    @Modifying
    @Query("UPDATE FAQ f SET f.viewCount = f.viewCount + 1 WHERE f.id = :id")
    void incrementViewCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE FAQ f SET f.helpfulCount = f.helpfulCount + 1 WHERE f.id = :id")
    void incrementHelpfulCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE FAQ f SET f.notHelpfulCount = f.notHelpfulCount + 1 WHERE f.id = :id")
    void incrementNotHelpfulCount(@Param("id") Long id);
}
