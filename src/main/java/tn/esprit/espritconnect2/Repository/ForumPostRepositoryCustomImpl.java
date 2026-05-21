package tn.esprit.espritconnect2.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import tn.esprit.espritconnect2.Entitie.ForumPost;
import tn.esprit.espritconnect2.Entitie.Role;

import java.util.List;

public class ForumPostRepositoryCustomImpl implements ForumPostRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<ForumPost> filterPosts(Long categoryId, Role authorRole, Boolean reported, String search) {

        StringBuilder jpql = new StringBuilder("SELECT p FROM ForumPost p WHERE 1=1");

        if (categoryId != null) {
            jpql.append(" AND p.category.id = :categoryId");
        }
        if (authorRole != null) {
            jpql.append(" AND p.authorRole = :authorRole");
        }
        if (reported != null) {
            jpql.append(" AND p.reported = :reported");
        }
        if (search != null && !search.isBlank()) {
            jpql.append(" AND (p.title LIKE :search OR p.content LIKE :search)");
        }

        jpql.append(" ORDER BY p.pinned DESC, p.createdAt DESC");

        TypedQuery<ForumPost> query = entityManager.createQuery(jpql.toString(), ForumPost.class);

        if (categoryId != null) {
            query.setParameter("categoryId", categoryId);
        }
        if (authorRole != null) {
            query.setParameter("authorRole", authorRole);
        }
        if (reported != null) {
            query.setParameter("reported", reported);
        }
        if (search != null && !search.isBlank()) {
            query.setParameter("search", "%" + search.toLowerCase() + "%");
        }

        return query.getResultList();
    }
}