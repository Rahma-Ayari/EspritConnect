package tn.esprit.espritconnect2.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.espritconnect2.Entitie.ForumCategory;

@Repository
public interface ForumCategoryRepository extends JpaRepository<ForumCategory, Long> {
    boolean existsByName(String name);
}
