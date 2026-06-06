package tn.esprit.espritconnect2.Service;

import tn.esprit.espritconnect2.Entitie.ForumCategory;
import tn.esprit.espritconnect2.Entitie.ForumPost;
import tn.esprit.espritconnect2.Entitie.ForumReply;
import tn.esprit.espritconnect2.Entitie.ForumGroup;
import tn.esprit.espritconnect2.Entitie.ForumGroupMember;

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
    List<ForumPost> getFilteredPosts(Long categoryId, String authorRole, Boolean reported, String search, Long groupId);
    ForumPost getPostById(Long id);
    ForumPost createPost(ForumPost post);
    ForumPost updatePost(Long id, ForumPost post);
    void deletePost(Long id);
    ForumPost togglePinPost(Long id);
    ForumPost reportPost(Long id, String reason);
    ForumPost resolvePostReport(Long id);

    // --- Replies ---
    ForumReply addReply(Long postId, ForumReply reply);
    ForumReply updateReply(Long replyId, ForumReply reply);
    void deleteReply(Long replyId);
    ForumReply reportReply(Long replyId, String reason);
    ForumReply resolveReplyReport(Long replyId);

    // --- Moderation & Stats ---
    List<ForumPost> getReportedPosts();
    List<ForumReply> getReportedReplies();
    Map<String, Object> getDashboardStats();

    // --- Groups ---
    ForumGroup createGroup(ForumGroup group);
    List<ForumGroup> getActiveGroups();
    List<ForumGroup> getPendingGroups();
    List<ForumGroup> getMyGroups(String userEmail);
    ForumGroup approveGroup(Long groupId);
    ForumGroup rejectGroup(Long groupId);
    ForumGroupMember requestJoinGroup(Long groupId, String userEmail, String userName);
    List<ForumGroupMember> getPendingMemberships(Long groupId, String currentUserEmail);
    ForumGroupMember approveMembership(Long groupId, Long memberId, String currentUserEmail);
    ForumGroupMember rejectMembership(Long groupId, Long memberId, String currentUserEmail);
    void leaveGroup(Long groupId, String userEmail);
    List<ForumGroupMember> getGroupMembers(Long groupId);
    ForumGroup getGroupById(Long groupId);
    List<ForumGroupMember> getUserMemberships(String userEmail);
}
