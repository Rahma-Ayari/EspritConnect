package tn.esprit.espritconnect2.Repository;

import tn.esprit.espritconnect2.Entitie.ForumPost;
import tn.esprit.espritconnect2.Entitie.PostStatus;
import tn.esprit.espritconnect2.Entitie.Role;

import java.util.List;

public interface ForumPostRepositoryCustom {
    List<ForumPost> filterPosts(Long categoryId, Role authorRole, Boolean reported, String search, Long discussionId, PostStatus status, String authorEmail);

    List<ForumPost> filterPublicPosts(Long categoryId, Role authorRole, String search, String authorEmail);
}
