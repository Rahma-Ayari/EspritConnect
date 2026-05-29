package tn.esprit.espritconnect2.Service.frontOffice;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.espritconnect2.DTO.frontOffice.ForumHomeDTO;
import tn.esprit.espritconnect2.DTO.frontOffice.LikeToggleResponseDTO;
import tn.esprit.espritconnect2.DTO.frontOffice.PagedResponseDTO;
import tn.esprit.espritconnect2.Entitie.*;
import tn.esprit.espritconnect2.Repository.*;
import tn.esprit.espritconnect2.Service.ForumService;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ForumFrontOfficeServiceImpl implements IForumFrontOfficeService {

    private final ForumService forumService;
    private final ForumCategoryRepository categoryRepository;
    private final ForumPostRepository postRepository;
    private final ForumReplyRepository replyRepository;
    private final ForumPostLikeRepository likeRepository;

    @Override
    @Transactional(readOnly = true)
    public ForumHomeDTO getHome() {
        List<ForumCategory> categories = forumService.getAllCategories();
        Map<Long, Long> counts = forumService.getPostCountsPerCategory();

        List<ForumPost> publicPosts = postRepository.findByReportedFalseOrderByPinnedDescCreatedAtDesc();
        List<ForumPost> pinned = publicPosts.stream().filter(ForumPost::isPinned).limit(5).toList();
        List<ForumPost> recent = publicPosts.stream().limit(6).toList();

        return ForumHomeDTO.builder()
                .totalPosts(publicPosts.size())
                .totalReplies(replyRepository.count())
                .totalCategories(categories.size())
                .categories(categories)
                .postCountsPerCategory(counts)
                .pinnedPosts(pinned)
                .recentPosts(recent)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ForumCategory> getCategories() {
        return forumService.getAllCategories();
    }

    @Override
    @Transactional(readOnly = true)
    public ForumCategory getCategory(Long id) {
        return forumService.getCategoryById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDTO<ForumPost> getPublicPosts(Long categoryId, String authorRole, String search,
                                                      int page, int size, String viewerEmail) {
        Role role = parseRole(authorRole);
        List<ForumPost> all = postRepository.filterPublicPosts(categoryId, role, search, null);
        return paginate(all, page, size, viewerEmail);
    }

    @Override
    @Transactional
    public ForumPost getPublicPost(Long id, String viewerEmail) {
        ForumPost post = forumService.getPostById(id);
        if (post.isReported()) {
            throw new IllegalArgumentException("Cette publication n'est plus disponible.");
        }
        enrichLikeState(post, viewerEmail);
        return post;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ForumPost> getMyPosts(String authorEmail) {
        if (authorEmail == null || authorEmail.isBlank()) {
            throw new IllegalArgumentException("L'email de l'utilisateur est obligatoire.");
        }
        return postRepository.findByAuthorEmailIgnoreCaseAndReportedFalseOrderByCreatedAtDesc(authorEmail.trim());
    }

    @Override
    @Transactional
    public ForumPost createPost(ForumPost post) {
        validateAuthor(post.getAuthorName(), post.getAuthorEmail(), post.getAuthorRole());
        return forumService.createPost(post);
    }

    @Override
    @Transactional
    public ForumPost updatePost(Long id, ForumPost post, String requesterEmail) {
        ForumPost existing = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Publication introuvable."));
        assertOwner(existing.getAuthorEmail(), requesterEmail);
        return forumService.updatePost(id, post);
    }

    @Override
    @Transactional
    public void deletePost(Long id, String requesterEmail) {
        ForumPost existing = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Publication introuvable."));
        assertOwner(existing.getAuthorEmail(), requesterEmail);
        forumService.deletePost(id);
    }

    @Override
    @Transactional
    public ForumReply addReply(Long postId, ForumReply reply) {
        validateAuthor(reply.getAuthorName(), reply.getAuthorEmail(), reply.getAuthorRole());
        ForumPost post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Publication introuvable."));
        if (post.isReported()) {
            throw new IllegalArgumentException("Impossible de commenter une publication signalée.");
        }
        return forumService.addReply(postId, reply);
    }

    @Override
    @Transactional
    public ForumReply updateReply(Long replyId, ForumReply reply, String requesterEmail) {
        ForumReply existing = replyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("Commentaire introuvable."));
        assertOwner(existing.getAuthorEmail(), requesterEmail);
        return forumService.updateReply(replyId, reply);
    }

    @Override
    @Transactional
    public void deleteReply(Long replyId, String requesterEmail) {
        ForumReply existing = replyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("Commentaire introuvable."));
        assertOwner(existing.getAuthorEmail(), requesterEmail);
        forumService.deleteReply(replyId);
    }

    @Override
    @Transactional
    public LikeToggleResponseDTO toggleLike(Long postId, String userEmail) {
        if (userEmail == null || userEmail.isBlank()) {
            throw new IllegalArgumentException("L'email est obligatoire pour aimer une publication.");
        }
        ForumPost post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Publication introuvable."));
        if (post.isReported()) {
            throw new IllegalArgumentException("Publication indisponible.");
        }

        String email = userEmail.trim();
        var existing = likeRepository.findByPostIdAndUserEmailIgnoreCase(postId, email);
        boolean liked;
        if (existing.isPresent()) {
            likeRepository.delete(existing.get());
            post.setLikesCount(Math.max(0, post.getLikesCount() - 1));
            liked = false;
        } else {
            likeRepository.save(ForumPostLike.builder().post(post).userEmail(email).build());
            post.setLikesCount(post.getLikesCount() + 1);
            liked = true;
        }
        postRepository.save(post);

        return LikeToggleResponseDTO.builder()
                .likesCount(post.getLikesCount())
                .likedByCurrentUser(liked)
                .build();
    }

    @Override
    @Transactional
    public ForumPost reportPost(Long id, String reason) {
        return forumService.reportPost(id, reason);
    }

    @Override
    @Transactional
    public ForumReply reportReply(Long replyId, String reason) {
        return forumService.reportReply(replyId, reason);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Long> getPostCountsPerCategory() {
        return forumService.getPostCountsPerCategory();
    }

    private PagedResponseDTO<ForumPost> paginate(List<ForumPost> all, int page, int size, String viewerEmail) {
        int safeSize = Math.max(1, Math.min(size, 50));
        int safePage = Math.max(0, page);
        int total = all.size();
        int totalPages = total == 0 ? 0 : (int) Math.ceil((double) total / safeSize);
        int from = Math.min(safePage * safeSize, total);
        int to = Math.min(from + safeSize, total);

        List<ForumPost> slice = all.subList(from, to).stream()
                .peek(p -> enrichLikeState(p, viewerEmail))
                .toList();

        return PagedResponseDTO.<ForumPost>builder()
                .content(slice)
                .page(safePage)
                .size(safeSize)
                .totalElements(total)
                .totalPages(totalPages)
                .build();
    }

    private void enrichLikeState(ForumPost post, String viewerEmail) {
        if (viewerEmail != null && !viewerEmail.isBlank()) {
            post.setLikesCount((int) likeRepository.countByPostId(post.getId()));
        }
    }

    private Role parseRole(String authorRole) {
        if (authorRole == null || authorRole.isBlank()) {
            return null;
        }
        try {
            return Role.valueOf(authorRole.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private void validateAuthor(String name, String email, Role role) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Le nom de l'auteur est obligatoire.");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("L'email de l'auteur est obligatoire.");
        }
        if (role == null) {
            throw new IllegalArgumentException("Le profil de l'auteur est obligatoire.");
        }
    }

    private void assertOwner(String ownerEmail, String requesterEmail) {
        if (requesterEmail == null || requesterEmail.isBlank()) {
            throw new IllegalArgumentException("Email du demandeur manquant.");
        }
        if (!ownerEmail.equalsIgnoreCase(requesterEmail.trim())) {
            throw new IllegalArgumentException("Vous n'êtes pas autorisé à modifier ce contenu.");
        }
    }
}
