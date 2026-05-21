package tn.esprit.espritconnect2.Service;

import tn.esprit.espritconnect2.Entitie.ForumCategory;
import tn.esprit.espritconnect2.Entitie.ForumPost;
import tn.esprit.espritconnect2.Entitie.ForumReply;

import java.util.List;
import java.util.Map;

public interface ForumService {

    // --- Categories ---
    List<ForumCategory> getAllCategories();
    ForumCategory getCategoryById(Long id);
    ForumCategory createCategory(ForumCategory category);
    ForumCategory updateCategory(Long id, ForumCategory category);
    void deleteCategory(Long id);
    Map<Long, Long> getPostCountsPerCategory();

    // --- Posts ---
    List<ForumPost> getFilteredPosts(Long categoryId, String authorRole, Boolean reported, String search);
    ForumPost getPostById(Long id);
    ForumPost createPost(ForumPost post);
    ForumPost updatePost(Long id, ForumPost post);
    void deletePost(Long id);
    ForumPost togglePinPost(Long id);
    ForumPost reportPost(Long id, String reason);
    ForumPost resolvePostReport(Long id);

    // --- Replies ---
    ForumReply addReply(Long postId, ForumReply reply);
    void deleteReply(Long replyId);
    ForumReply reportReply(Long replyId, String reason);
    ForumReply resolveReplyReport(Long replyId);

    // --- Moderation & Stats ---
    List<ForumPost> getReportedPosts();
    List<ForumReply> getReportedReplies();
    Map<String, Object> getDashboardStats();
}
