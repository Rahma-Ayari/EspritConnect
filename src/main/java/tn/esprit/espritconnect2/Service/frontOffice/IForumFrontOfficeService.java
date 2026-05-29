package tn.esprit.espritconnect2.Service.frontOffice;

import tn.esprit.espritconnect2.DTO.frontOffice.ForumHomeDTO;
import tn.esprit.espritconnect2.DTO.frontOffice.LikeToggleResponseDTO;
import tn.esprit.espritconnect2.DTO.frontOffice.PagedResponseDTO;
import tn.esprit.espritconnect2.Entitie.ForumCategory;
import tn.esprit.espritconnect2.Entitie.ForumPost;
import tn.esprit.espritconnect2.Entitie.ForumReply;

import java.util.List;
import java.util.Map;

public interface IForumFrontOfficeService {

    ForumHomeDTO getHome();

    List<ForumCategory> getCategories();

    ForumCategory getCategory(Long id);

    PagedResponseDTO<ForumPost> getPublicPosts(Long categoryId, String authorRole, String search,
                                               int page, int size, String viewerEmail);

    ForumPost getPublicPost(Long id, String viewerEmail);

    List<ForumPost> getMyPosts(String authorEmail);

    ForumPost createPost(ForumPost post);

    ForumPost updatePost(Long id, ForumPost post, String requesterEmail);

    void deletePost(Long id, String requesterEmail);

    ForumReply addReply(Long postId, ForumReply reply);

    ForumReply updateReply(Long replyId, ForumReply reply, String requesterEmail);

    void deleteReply(Long replyId, String requesterEmail);

    LikeToggleResponseDTO toggleLike(Long postId, String userEmail);

    ForumPost reportPost(Long id, String reason);

    ForumReply reportReply(Long replyId, String reason);

    Map<Long, Long> getPostCountsPerCategory();
}
