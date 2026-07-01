package tn.esprit.espritconnect2.Service;

import tn.esprit.espritconnect2.DTO.CreatePostRequest;
import tn.esprit.espritconnect2.DTO.PostMatchResponse;
import tn.esprit.espritconnect2.Entitie.*;

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
    List<ForumPost> getFilteredPosts(Long categoryId, String authorRole, Boolean reported, String search, Long discussionId, PostStatus status, String authorEmail);
    ForumPost getPostById(Long id);
    ForumPost createPost(ForumPost post);
    ForumPost createPostFromRequest(CreatePostRequest request);
    ForumPost updatePost(Long id, ForumPost post);
    ForumPost updatePostFromRequest(Long id, CreatePostRequest request);
    void deletePost(Long id);
    ForumPost togglePinPost(Long id);
    ForumPost reportPost(Long id, String reason);
    ForumPost resolvePostReport(Long id);
    List<ForumPost> getMyPosts(String authorEmail, PostStatus status);
    List<ForumPost> getPendingPosts();
    ForumPost submitPost(Long id, String authorEmail);
    ForumPost approvePost(Long id);
    ForumPost rejectPost(Long id, String reason);
    List<PostMatchResponse> getPostMatchesForUser(String email);
    List<ForumPost> getDiscussionPostsWithReplies(Long discussionId);
    Map<String, Object> togglePostLike(Long postId, String userEmail);
    Map<String, Object> togglePostFavorite(Long postId, String userEmail);
    Map<String, Object> getUserPostEngagement(String userEmail, Long discussionId);
    List<ForumPost> getFavoritePosts(String userEmail);
    ForumPost incrementPostView(Long postId);

    // --- Replies ---
    ForumReply addReply(Long postId, ForumReply reply, Long parentReplyId);
    ForumReply updateReply(Long replyId, ForumReply reply);
    void deleteReply(Long replyId, String userEmail);
    Map<String, Object> toggleReplyLike(Long replyId, String userEmail);
    ForumReply reportReply(Long replyId, String reason);
    ForumReply resolveReplyReport(Long replyId);

    // --- Moderation & Stats ---
    List<ForumPost> getReportedPosts();
    List<ForumReply> getReportedReplies();
    Map<String, Object> getDashboardStats();

    // --- Discussions ---
    ForumDiscussion createDiscussion(ForumDiscussion discussion);
    List<ForumDiscussion> getActiveDiscussions();
    List<ForumDiscussion> getPendingDiscussions();
    List<ForumDiscussion> getPendingModifications();
    List<ForumDiscussion> getPendingDeletions();
    List<ForumDiscussion> getMyDiscussions(String userEmail);
    List<ForumDiscussion> getMyCreatedDiscussions(String userEmail);
    ForumDiscussion approveDiscussion(Long discussionId);
    ForumDiscussion rejectDiscussion(Long discussionId);
    ForumDiscussionMember requestJoinDiscussion(Long discussionId, String userEmail, String userName);
    List<ForumDiscussionMember> getPendingMemberships(Long discussionId, String currentUserEmail);
    ForumDiscussionMember approveMembership(Long discussionId, Long memberId, String currentUserEmail);
    ForumDiscussionMember rejectMembership(Long discussionId, Long memberId, String currentUserEmail);
    void leaveDiscussion(Long discussionId, String userEmail);
    List<ForumDiscussionMember> getDiscussionMembers(Long discussionId);
    ForumDiscussion getDiscussionById(Long discussionId);
    ForumDiscussion getDiscussionByName(String name);
    List<ForumDiscussionMember> getUserMemberships(String userEmail);
    ForumDiscussion updateDiscussion(Long discussionId, ForumDiscussion discussionDetails, String userEmail);
    ForumDiscussion approveDiscussionModification(Long discussionId);
    ForumDiscussion rejectDiscussionModification(Long discussionId);
    void rejectDeletionRequest(Long discussionId);
    void requestDeleteDiscussion(Long discussionId, String userEmail);
    void approveDeleteDiscussion(Long discussionId);
    void adminDeleteDiscussion(Long discussionId);
    void deletePostInDiscussion(Long discussionId, Long postId, String userEmail);
}
