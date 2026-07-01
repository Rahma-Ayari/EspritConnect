package tn.esprit.espritconnect2.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import tn.esprit.espritconnect2.Entitie.ForumPost;
import tn.esprit.espritconnect2.Entitie.PostStatus;
import tn.esprit.espritconnect2.Entitie.Role;

import java.util.List;

public class ForumPostRepositoryCustomImpl implements ForumPostRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<ForumPost> filterPosts(Long categoryId, Role authorRole, Boolean reported, String search, Long discussionId, PostStatus status, String authorEmail) {

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
        if (status != null) {
            jpql.append(" AND p.status = :status");
        } else if (authorEmail == null || authorEmail.isBlank()) {
            jpql.append(" AND p.status = :defaultStatus");
        }
        if (search != null && !search.isBlank()) {
            jpql.append(" AND (LOWER(p.title) LIKE :search OR LOWER(p.content) LIKE :search OR LOWER(p.authorName) LIKE :search)");
        }
        if (authorEmail != null && !authorEmail.isBlank()) {
            jpql.append(" AND LOWER(p.authorEmail) = LOWER(:authorEmail)");
        }
        if (discussionId != null) {
            jpql.append(" AND p.forumDiscussion.id = :discussionId");
        } else if (reported == null || !reported) {
            jpql.append(" AND p.forumDiscussion IS NULL");
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
        if (status != null) {
            query.setParameter("status", status);
        } else if (authorEmail == null || authorEmail.isBlank()) {
            query.setParameter("defaultStatus", PostStatus.PUBLISHED);
        }
        if (search != null && !search.isBlank()) {
            query.setParameter("search", "%" + search.toLowerCase() + "%");
        }
        if (authorEmail != null && !authorEmail.isBlank()) {
            query.setParameter("authorEmail", authorEmail.trim());
        }
        if (discussionId != null) {
            query.setParameter("discussionId", discussionId);
        }

        return query.getResultList();
    }

    @Override
    public List<ForumPost> filterPublicPosts(Long categoryId, Role authorRole, String search, String authorEmail) {
        return filterPosts(categoryId, authorRole, false, search, null, PostStatus.PUBLISHED, authorEmail);
    }
}
