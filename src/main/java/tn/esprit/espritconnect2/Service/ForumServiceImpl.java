package tn.esprit.espritconnect2.Service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.espritconnect2.DTO.CreatePostRequest;
import tn.esprit.espritconnect2.DTO.PostMatchResponse;
import tn.esprit.espritconnect2.Entitie.*;
import tn.esprit.espritconnect2.Repository.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ForumServiceImpl implements ForumService {

    private final ForumCategoryRepository categoryRepository;
    private final ForumPostRepository postRepository;
    private final ForumReplyRepository replyRepository;
    private final ForumDiscussionRepository discussionRepository;
    private final ForumDiscussionMemberRepository discussionMemberRepository;
    private final PostMatchingService postMatchingService;
    private final UserRepository userRepository;
    private final ForumPostLikeRepository postLikeRepository;
    private final ForumPostFavoriteRepository postFavoriteRepository;
    private final ForumReplyLikeRepository replyLikeRepository;
    private final ForumPostRecommendationCacheService recommendationCacheService;

    @Override
    @Transactional(readOnly = true)
    public List<ForumCategory> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public ForumCategory getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Catégorie introuvable avec l'ID: " + id));
    }

    @Override
    @Transactional
    public ForumCategory createCategory(ForumCategory category) {
        if (categoryRepository.existsByName(category.getName())) {
            throw new IllegalArgumentException("Une catégorie avec ce nom existe déjà.");
        }
        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public ForumCategory updateCategory(Long id, ForumCategory categoryDetails) {
        ForumCategory category = getCategoryById(id);
        category.setName(categoryDetails.getName());
        category.setDescription(categoryDetails.getDescription());
        category.setIcon(categoryDetails.getIcon());
        category.setColor(categoryDetails.getColor());
        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        ForumCategory category = getCategoryById(id);
        // Supprime tous les posts associés en cascade (géré au niveau DB ou service)
        categoryRepository.delete(category);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Long> getPostCountsPerCategory() {
        return categoryRepository.findAll().stream()
                .collect(Collectors.toMap(
                        ForumCategory::getId,
                        cat -> postRepository.countByCategoryId(cat.getId())
                ));
    }

    @Override
    @Transactional
    public List<ForumPost> getFilteredPosts(Long categoryId, String authorRole, Boolean reported, String search, Long discussionId, PostStatus status, String authorEmail) {
        Role role = null;
        if (authorRole != null && !authorRole.trim().isEmpty()) {
            try {
                role = Role.valueOf(authorRole.toUpperCase());
            } catch (IllegalArgumentException e) {
                // ignore invalid role filter
            }
        }
        String searchQuery = (search != null && !search.trim().isEmpty()) ? search : null;
        return postRepository.filterPosts(categoryId, role, reported, searchQuery, discussionId, status, authorEmail);
    }

    @Override
    @Transactional
    public ForumPost getPostById(Long id) {
        ForumPost post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Publication introuvable avec l'ID: " + id));
        // Incrémente le compteur de vues
        post.setViewsCount(post.getViewsCount() + 1);
        return postRepository.save(post);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ForumPost> getDiscussionPostsWithReplies(Long discussionId) {
        return postRepository.findDiscussionPostsWithReplies(discussionId);
    }

    @Override
    @Transactional
    public Map<String, Object> togglePostLike(Long postId, String userEmail) {
        ForumPost post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Publication introuvable."));
        Optional<ForumPostLike> existing = postLikeRepository.findByPostIdAndUserEmail(postId, userEmail);
        boolean liked;
        if (existing.isPresent()) {
            postLikeRepository.delete(existing.get());
            post.setLikesCount(Math.max(0, post.getLikesCount() - 1));
            liked = false;
        } else {
            postLikeRepository.save(ForumPostLike.builder().post(post).userEmail(userEmail).build());
            post.setLikesCount(post.getLikesCount() + 1);
            liked = true;
            
            // Automatically add to favorites if not already favorited
            Optional<ForumPostFavorite> existingFav = postFavoriteRepository.findByPostIdAndUserEmail(postId, userEmail);
            if (existingFav.isEmpty()) {
                postFavoriteRepository.save(ForumPostFavorite.builder().post(post).userEmail(userEmail).build());
            }
        }
        postRepository.save(post);
        return Map.of("liked", liked, "likesCount", post.getLikesCount());
    }

    @Override
    @Transactional
    public Map<String, Object> togglePostFavorite(Long postId, String userEmail) {
        ForumPost post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Publication introuvable."));
        Optional<ForumPostFavorite> existing = postFavoriteRepository.findByPostIdAndUserEmail(postId, userEmail);
        boolean favorited;
        if (existing.isPresent()) {
            postFavoriteRepository.delete(existing.get());
            favorited = false;
        } else {
            postFavoriteRepository.save(ForumPostFavorite.builder().post(post).userEmail(userEmail).build());
            favorited = true;
        }
        recommendationCacheService.evict(userEmail);
        return Map.of("favorited", favorited);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getUserPostEngagement(String userEmail, Long discussionId) {
        List<Long> likedPostIds = postLikeRepository.findByUserEmail(userEmail).stream()
                .filter(l -> discussionId == null || (l.getPost().getForumDiscussion() != null
                        && discussionId.equals(l.getPost().getForumDiscussion().getId())))
                .map(l -> l.getPost().getId())
                .collect(Collectors.toList());
        List<Long> favoritedPostIds = postFavoriteRepository.findByUserEmailOrderByCreatedAtDesc(userEmail).stream()
                .filter(f -> discussionId == null || (f.getPost().getForumDiscussion() != null
                        && discussionId.equals(f.getPost().getForumDiscussion().getId())))
                .map(f -> f.getPost().getId())
                .collect(Collectors.toList());
        return Map.of("likedPostIds", likedPostIds, "favoritedPostIds", favoritedPostIds);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ForumPost> getFavoritePosts(String userEmail) {
        // Get post IDs from favorites
        List<Long> postIds = postFavoriteRepository.findByUserEmailOrderByCreatedAtDesc(userEmail).stream()
                .map(f -> f.getPost().getId())
                .collect(Collectors.toList());

        if (postIds.isEmpty()) {
            return new ArrayList<>();
        }

        // Load posts with EntityGraph to ensure all fields are loaded
        List<ForumPost> posts = postRepository.findByIdsWithDetails(postIds);

        // Sort by favorite creation time
        List<ForumPostFavorite> favorites = postFavoriteRepository.findByUserEmailOrderByCreatedAtDesc(userEmail);
        Map<Long, LocalDateTime> favoriteTimes = favorites.stream()
                .collect(Collectors.toMap(f -> f.getPost().getId(), ForumPostFavorite::getCreatedAt));

        posts.sort((a, b) -> {
            LocalDateTime timeA = favoriteTimes.get(a.getId());
            LocalDateTime timeB = favoriteTimes.get(b.getId());
            if (timeA == null || timeB == null) return 0;
            return timeB.compareTo(timeA);
        });

        return posts;
    }

    @Override
    @Transactional
    public ForumPost incrementPostView(Long postId) {
        ForumPost post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Publication introuvable."));
        post.setViewsCount(post.getViewsCount() + 1);
        return postRepository.save(post);
    }

    @Override
    @Transactional
    public ForumPost createPost(ForumPost post) {
        if (post.getCategory() == null || post.getCategory().getId() == null) {
            if (post.getStatus() != PostStatus.DRAFT) {
                throw new IllegalArgumentException("Category is required.");
            }
        } else {
            ForumCategory category = getCategoryById(post.getCategory().getId());
            post.setCategory(category);
        }
        if (post.getStatus() == null) {
            post.setStatus(PostStatus.PUBLISHED);
        }
        if (post.getPostType() == null) {
            post.setPostType(PostType.QUESTION);
        }
        post.setViewsCount(0);
        post.setPinned(false);
        post.setReported(false);

        if (post.getForumDiscussion() != null && post.getForumDiscussion().getId() != null) {
            ForumDiscussion discussion = discussionRepository.findById(post.getForumDiscussion().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Discussion not found."));
            if (discussion.getStatus() != DiscussionStatus.APPROVED) {
                throw new IllegalArgumentException("This discussion is not active.");
            }
            boolean isMember = discussionMemberRepository.existsByDiscussionIdAndUserEmailAndStatus(
                    discussion.getId(), post.getAuthorEmail(), MemberStatus.APPROVED
            );
            if (!isMember) {
                throw new IllegalArgumentException("You must be an approved member to post in this discussion.");
            }
            post.setForumDiscussion(discussion);
            // Set status to PENDING for discussion posts requiring admin approval
            if (post.getStatus() == null || post.getStatus() == PostStatus.PUBLISHED) {
                post.setStatus(PostStatus.PENDING);
            }
        }

        ForumPost saved = postRepository.save(post);
        if (saved.getStatus() == PostStatus.PENDING || saved.getStatus() == PostStatus.PUBLISHED) {
            postMatchingService.computeMatches(saved);
        }
        return saved;
    }

    @Override
    @Transactional
    public ForumPost createPostFromRequest(CreatePostRequest request) {
        validateCreateRequest(request, true);
        ForumPost post = mapRequestToPost(new ForumPost(), request);
        if (request.isDraft()) {
            post.setStatus(PostStatus.DRAFT);
        } else if (request.getDiscussionId() != null) {
            post.setStatus(PostStatus.PUBLISHED);
            post.setPublishedAt(LocalDateTime.now());
        } else {
            post.setStatus(PostStatus.PENDING);
        }
        return createPost(post);
    }

    @Override
    @Transactional
    public ForumPost updatePostFromRequest(Long id, CreatePostRequest request) {
        ForumPost post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post not found."));
        if (request.getAuthorEmail() != null
                && !post.getAuthorEmail().equalsIgnoreCase(request.getAuthorEmail().trim())) {
            throw new IllegalArgumentException("You can only edit your own posts.");
        }
        if (post.getStatus() == PostStatus.PUBLISHED || post.getStatus() == PostStatus.ARCHIVED) {
            throw new IllegalArgumentException("Published posts cannot be edited this way.");
        }
        validateCreateRequest(request, request.isDraft());
        mapRequestToPost(post, request);
        if (!request.isDraft() && post.getStatus() == PostStatus.DRAFT) {
            post.setStatus(PostStatus.PENDING);
        } else if (request.isDraft()) {
            post.setStatus(PostStatus.DRAFT);
        }
        return postRepository.save(post);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ForumPost> getMyPosts(String authorEmail, PostStatus status) {
        if (authorEmail == null || authorEmail.isBlank()) {
            throw new IllegalArgumentException("Author email is required.");
        }
        if (status != null) {
            return postRepository.findByAuthorEmailIgnoreCaseAndStatusOrderByUpdatedAtDesc(authorEmail.trim(), status);
        }
        return postRepository.findByAuthorEmailIgnoreCaseAndReportedFalseOrderByCreatedAtDesc(authorEmail.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ForumPost> getPendingPosts() {
        return postRepository.findByStatusOrderByCreatedAtDesc(PostStatus.PENDING);
    }

    @Override
    @Transactional
    public ForumPost submitPost(Long id, String authorEmail) {
        ForumPost post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post not found."));
        if (!post.getAuthorEmail().equalsIgnoreCase(authorEmail)) {
            throw new IllegalArgumentException("You can only submit your own posts.");
        }
        if (post.getStatus() != PostStatus.DRAFT && post.getStatus() != PostStatus.REJECTED) {
            throw new IllegalArgumentException("Only drafts or rejected posts can be submitted.");
        }
        validatePostForSubmission(post);
        post.setStatus(PostStatus.PENDING);
        post.setRejectionReason(null);
        ForumPost saved = postRepository.save(post);
        postMatchingService.computeMatches(saved);
        return saved;
    }

    @Override
    @Transactional
    public ForumPost approvePost(Long id) {
        ForumPost post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post not found."));
        if (post.getStatus() != PostStatus.PENDING) {
            throw new IllegalArgumentException("Only pending posts can be approved.");
        }
        post.setStatus(PostStatus.PUBLISHED);
        post.setPublishedAt(LocalDateTime.now());
        post.setRejectionReason(null);
        ForumPost saved = postRepository.save(post);
        postMatchingService.computeMatches(saved);
        return saved;
    }

    @Override
    @Transactional
    public ForumPost rejectPost(Long id, String reason) {
        ForumPost post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post not found."));
        if (post.getStatus() != PostStatus.PENDING) {
            throw new IllegalArgumentException("Only pending posts can be rejected.");
        }
        post.setStatus(PostStatus.REJECTED);
        post.setRejectionReason(reason != null && !reason.isBlank() ? reason.trim() : "Rejected by moderator.");
        return postRepository.save(post);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostMatchResponse> getPostMatchesForUser(String email) {
        return postMatchingService.getMatchesForUser(email);
    }

    private ForumPost mapRequestToPost(ForumPost post, CreatePostRequest request) {
        post.setTitle(request.getTitle().trim());
        post.setContent(request.getContent() != null ? request.getContent().trim() : "");
        post.setPostType(request.getPostType() != null ? request.getPostType() : PostType.QUESTION);
        post.setTags(normalizeTags(request.getTags()));
        post.setWebsite(trimToNull(request.getWebsite()));
        post.setCoverImageUrl(trimToNull(request.getCoverImageUrl()));
        post.setVideoUrl(trimToNull(request.getVideoUrl()));
        post.setPdfUrl(trimToNull(request.getPdfUrl()));
        post.setPdfExtractedText(trimToNull(request.getPdfExtractedText()));
        post.setAllowMentions(request.isAllowMentions());
        post.setAiGenerated(request.isAiGenerated());
        post.setAuthorName(request.getAuthorName().trim());
        post.setAuthorEmail(request.getAuthorEmail().trim());
        post.setAuthorRole(request.getAuthorRole());

        if (request.getCategoryId() != null) {
            post.setCategory(getCategoryById(request.getCategoryId()));
        }
        if (request.getDiscussionId() != null) {
            post.setForumDiscussion(discussionRepository.findById(request.getDiscussionId()).orElse(null));
        }
        return post;
    }

    private void validateCreateRequest(CreatePostRequest request, boolean allowDraft) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required.");
        }
        if (request.getTitle() == null || request.getTitle().trim().length() < 5) {
            throw new IllegalArgumentException("Title must be at least 5 characters.");
        }
        if (request.getAuthorName() == null || request.getAuthorName().isBlank()) {
            throw new IllegalArgumentException("Author name is required.");
        }
        if (request.getAuthorEmail() == null || request.getAuthorEmail().isBlank()) {
            throw new IllegalArgumentException("Author email is required.");
        }
        if (request.getAuthorRole() == null) {
            throw new IllegalArgumentException("Author role is required.");
        }
        if (!allowDraft) {
            validatePostForSubmission(mapRequestToPost(new ForumPost(), request));
        }
    }

    private void validatePostForSubmission(ForumPost post) {
        if (post.getCategory() == null) {
            throw new IllegalArgumentException("Category is required before submission.");
        }
        if (post.getContent() == null || post.getContent().trim().length() < 20) {
            throw new IllegalArgumentException("Content must be at least 20 characters.");
        }
        if (post.getTags() == null || post.getTags().isEmpty()) {
            throw new IllegalArgumentException("At least one tag is required.");
        }
    }

    private List<String> normalizeTags(List<String> tags) {
        if (tags == null) return new ArrayList<>();
        return tags.stream()
                .filter(t -> t != null && !t.trim().isEmpty())
                .map(t -> t.startsWith("#") ? t.trim() : "#" + t.trim())
                .distinct()
                .toList();
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }

    @Override
    @Transactional
    public ForumPost updatePost(Long id, ForumPost postDetails) {
        ForumPost post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post not found."));
        post.setTitle(postDetails.getTitle());
        post.setContent(postDetails.getContent());
        if (postDetails.getCategory() != null && postDetails.getCategory().getId() != null) {
            post.setCategory(getCategoryById(postDetails.getCategory().getId()));
        }
        if (postDetails.getTags() != null) {
            post.setTags(postDetails.getTags());
        }
        if (postDetails.getPostType() != null) {
            post.setPostType(postDetails.getPostType());
        }
        if (postDetails.getStatus() != null) {
            post.setStatus(postDetails.getStatus());
        }
        post.setWebsite(postDetails.getWebsite());
        post.setCoverImageUrl(postDetails.getCoverImageUrl());
        post.setVideoUrl(postDetails.getVideoUrl());
        post.setPdfUrl(postDetails.getPdfUrl());
        return postRepository.save(post);
    }

    @Override
    @Transactional
    public void deletePost(Long id) {
        ForumPost post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Publication introuvable."));
        postRepository.delete(post);
    }

    @Override
    @Transactional
    public ForumPost togglePinPost(Long id) {
        ForumPost post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Publication introuvable."));
        post.setPinned(!post.isPinned());
        return postRepository.save(post);
    }

    @Override
    @Transactional
    public ForumPost reportPost(Long id, String reason) {
        ForumPost post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Publication introuvable."));
        post.setReported(true);
        post.setReportReason(reason);
        return postRepository.save(post);
    }

    @Override
    @Transactional
    public ForumPost resolvePostReport(Long id) {
        ForumPost post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Publication introuvable."));
        post.setReported(false);
        post.setReportReason(null);
        return postRepository.save(post);
    }

    @Override
    @Transactional
    public ForumReply addReply(Long postId, ForumReply reply, Long parentReplyId) {
        ForumPost post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Publication introuvable."));
        reply.setPost(post);
        reply.setReported(false);
        if (parentReplyId != null) {
            ForumReply parent = replyRepository.findById(parentReplyId)
                    .orElseThrow(() -> new IllegalArgumentException("Commentaire parent introuvable."));
            reply.setParentReply(parent);
        }
        return replyRepository.save(reply);
    }

    @Override
    @Transactional
    public ForumReply updateReply(Long replyId, ForumReply replyDetails) {
        ForumReply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("Commentaire introuvable."));
        reply.setContent(replyDetails.getContent());
        return replyRepository.save(reply);
    }

    @Override
    @Transactional
    public void deleteReply(Long replyId, String userEmail) {
        ForumReply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("Commentaire introuvable."));
        if (!reply.getAuthorEmail().equalsIgnoreCase(userEmail)) {
            throw new IllegalArgumentException("Vous ne pouvez supprimer que vos propres commentaires.");
        }
        replyRepository.delete(reply);
    }

    @Override
    @Transactional
    public Map<String, Object> toggleReplyLike(Long replyId, String userEmail) {
        ForumReply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("Commentaire introuvable."));
        Optional<ForumReplyLike> existing = replyLikeRepository.findByReplyIdAndUserEmail(replyId, userEmail);
        boolean liked;
        if (existing.isPresent()) {
            replyLikeRepository.delete(existing.get());
            reply.setLikesCount(Math.max(0, reply.getLikesCount() - 1));
            liked = false;
        } else {
            replyLikeRepository.save(ForumReplyLike.builder().reply(reply).userEmail(userEmail).build());
            reply.setLikesCount(reply.getLikesCount() + 1);
            liked = true;
        }
        replyRepository.save(reply);
        return Map.of("liked", liked, "likesCount", reply.getLikesCount());
    }

    @Override
    @Transactional
    public ForumReply reportReply(Long replyId, String reason) {
        ForumReply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("Commentaire introuvable."));
        reply.setReported(true);
        reply.setReportReason(reason);
        return replyRepository.save(reply);
    }

    @Override
    @Transactional
    public ForumReply resolveReplyReport(Long replyId) {
        ForumReply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("Commentaire introuvable."));
        reply.setReported(false);
        reply.setReportReason(null);
        return replyRepository.save(reply);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ForumPost> getReportedPosts() {
        return postRepository.findByReportedTrueOrderByCreatedAtDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ForumReply> getReportedReplies() {
        return replyRepository.findByReportedTrueOrderByCreatedAtDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        long totalPosts = postRepository.count();
        long totalReplies = replyRepository.count();
        long reportedPosts = postRepository.findByReportedTrueOrderByCreatedAtDesc().size();
        long reportedReplies = replyRepository.findByReportedTrueOrderByCreatedAtDesc().size();
        long totalReported = reportedPosts + reportedReplies;
        long totalCategories = categoryRepository.count();

        // Répartition par rôle
        Map<String, Long> roleDistribution = new HashMap<>();
        for (Role role : Role.values()) {
            long count = postRepository.countByAuthorRole(role) + replyRepository.countByAuthorRole(role);
            roleDistribution.put(role.name(), count);
        }

        stats.put("totalPosts", totalPosts);
        stats.put("totalReplies", totalReplies);
        stats.put("totalReported", totalReported);
        stats.put("reportedPostsCount", reportedPosts);
        stats.put("reportedRepliesCount", reportedReplies);
        stats.put("totalCategories", totalCategories);
        stats.put("roleDistribution", roleDistribution);

        return stats;
    }

    // --- SEEDING DES DONNÉES DE SIMULATION ---
    @PostConstruct
    @Transactional
    public void seedForumData() {
        // Nettoyage des tags [Seed] existants dans la base de données
        List<ForumDiscussion> allDiscussions = discussionRepository.findAll();
        for (ForumDiscussion d : allDiscussions) {
            if (d.getName() != null && d.getName().toLowerCase().contains("[seed]")) {
                d.setName(d.getName().replaceAll("(?i)\\[seed\\]\\s*", ""));
                discussionRepository.save(d);
            }
        }
        
        List<ForumPost> allPosts = postRepository.findAll();
        for (ForumPost p : allPosts) {
            if (p.getTitle() != null && p.getTitle().toLowerCase().contains("[seed]")) {
                p.setTitle(p.getTitle().replaceAll("(?i)\\[seed\\]\\s*", ""));
                postRepository.save(p);
            }
        }

        // Seeding des catégories d'abord
        ForumCategory softwareCategory;
        ForumCategory careerCategory;
        ForumCategory lifeCategory;
        ForumCategory pfeCategory;

        if (categoryRepository.count() == 0) {
            softwareCategory = categoryRepository.save(ForumCategory.builder()
                    .name("Génie Logiciel & Programmation")
                    .description("Discussions autour des technologies Web, Mobile, Algorithmique et architectures logicielles.")
                    .icon("settings")
                    .color("#3b82f6")
                    .build());

            careerCategory = categoryRepository.save(ForumCategory.builder()
                    .name("Orientation & Carrières")
                    .description("Conseils professionnels, mentorat, opportunités de stages et insertion pour alumni et futurs diplômés.")
                    .icon("briefcase")
                    .color("#8b5cf6")
                    .build());

            lifeCategory = categoryRepository.save(ForumCategory.builder()
                    .name("Vie Estudiantine & Clubs")
                    .description("Activités des clubs d'Esprit, événements, intégration, vie sur le campus et logement.")
                    .icon("home")
                    .color("#10b981")
                    .build());

            pfeCategory = categoryRepository.save(ForumCategory.builder()
                    .name("Projets de Fin d'Études (PFE)")
                    .description("Partage d'opportunités de PFE, conseils pour les rapports, soutenances et encadrement.")
                    .icon("hand")
                    .color("#f59e0b")
                    .build());
        } else {
            // Récupérer les catégories existantes
            List<ForumCategory> categories = categoryRepository.findAll();
            softwareCategory = categories.stream().filter(c -> c.getName().equals("Génie Logiciel & Programmation")).findFirst().orElse(categories.get(0));
            careerCategory = categories.stream().filter(c -> c.getName().equals("Orientation & Carrières")).findFirst().orElse(categories.get(0));
            lifeCategory = categories.stream().filter(c -> c.getName().equals("Vie Estudiantine & Clubs")).findFirst().orElse(categories.get(0));
            pfeCategory = categories.stream().filter(c -> c.getName().equals("Projets de Fin d'Études (PFE)")).findFirst().orElse(categories.get(0));

            // Mettre à jour les couleurs des catégories existantes
            if (softwareCategory != null) {
                softwareCategory.setColor("#3b82f6");
                categoryRepository.save(softwareCategory);
            }
            if (careerCategory != null) {
                careerCategory.setColor("#8b5cf6");
                categoryRepository.save(careerCategory);
            }
            if (lifeCategory != null) {
                lifeCategory.setColor("#10b981");
                categoryRepository.save(lifeCategory);
            }
            if (pfeCategory != null) {
                pfeCategory.setColor("#f59e0b");
                categoryRepository.save(pfeCategory);
            }
        }

        // Mettre à jour les discussions existantes sans catégorie (toujours exécuté)
        List<ForumDiscussion> discussionsWithoutCategory = discussionRepository.findAll().stream()
                .filter(d -> d.getCategory() == null)
                .toList();

        for (ForumDiscussion discussion : discussionsWithoutCategory) {
            if (discussion.getName() != null) {
                if (discussion.getName().contains("IA") || discussion.getName().contains("Cybersécurité") ||
                    discussion.getName().contains("DevOps") || discussion.getName().contains("Cloud")) {
                    discussion.setCategory(softwareCategory);
                } else if (discussion.getName().contains("Alumni") || discussion.getName().contains("Carrières")) {
                    discussion.setCategory(careerCategory);
                } else if (discussion.getName().contains("E-Sports") || discussion.getName().contains("Gaming") ||
                           discussion.getName().contains("Clubs")) {
                    discussion.setCategory(lifeCategory);
                } else {
                    discussion.setCategory(lifeCategory); // Default
                }
                discussionRepository.save(discussion);
            }
        }

        // Seeding des Groupes et adhésions si inexistant
        if (discussionRepository.count() == 0) {
            // Groupes en attente de validation (Modération Admin)
            discussionRepository.save(ForumDiscussion.builder()
                    .name("Club IA & Data Science Esprit")
                    .description("Communauté d'apprentissage et de partage de projets autour du Deep Learning, NLP et Computer Vision.")
                    .creatorEmail("etudiant.demo@esprit.tn")
                    .creatorName("Étudiant Demo")
                    .isPrivate(true)
                    .status(DiscussionStatus.PENDING_CREATION)
                    .category(softwareCategory)
                    .createdAt(LocalDateTime.now())
                    .build());

            discussionRepository.save(ForumDiscussion.builder()
                    .name("ESPRIT Alumni à l'International")
                    .description("Réseau d'entraide pour la recherche de postes et l'intégration des diplômés ESPRIT à l'étranger.")
                    .creatorEmail("alumni.demo@esprit.tn")
                    .creatorName("Alumni Demo")
                    .isPrivate(false)
                    .status(DiscussionStatus.PENDING_CREATION)
                    .category(careerCategory)
                    .createdAt(LocalDateTime.now())
                    .build());

            discussionRepository.save(ForumDiscussion.builder()
                    .name("Esprit E-Sports & Gaming")
                    .description("Organisation des tournois internes et compétitions universitaires d'E-Sports.")
                    .creatorEmail("firas.ghorbel@esprit.tn")
                    .creatorName("Firas Ghorbel")
                    .isPrivate(false)
                    .status(DiscussionStatus.PENDING_CREATION)
                    .category(lifeCategory)
                    .createdAt(LocalDateTime.now())
                    .build());

            // Groupes actifs avec demandes de membres en attente (Modération Créateur/Owner)
            ForumDiscussion cyberDiscussion = discussionRepository.save(ForumDiscussion.builder()
                    .name("Club Cybersécurité Esprit")
                    .description("Discussions, partages de Write-ups de CTF et ateliers pratiques de Pentesting.")
                    .creatorEmail("etudiant.demo@esprit.tn")
                    .creatorName("Étudiant Demo")
                    .isPrivate(true)
                    .status(DiscussionStatus.APPROVED)
                    .category(softwareCategory)
                    .createdAt(LocalDateTime.now().minusDays(5))
                    .build());

            // Propriétaire approuvé
            discussionMemberRepository.save(ForumDiscussionMember.builder()
                    .discussion(cyberDiscussion)
                    .userEmail("etudiant.demo@esprit.tn")
                    .userName("Étudiant Demo")
                    .role(DiscussionRole.CREATOR)
                    .status(MemberStatus.APPROVED)
                    .joinedAt(LocalDateTime.now().minusDays(5))
                    .build());

            // Demandes de membres en attente
            discussionMemberRepository.save(ForumDiscussionMember.builder()
                    .discussion(cyberDiscussion)
                    .userEmail("alumni.demo@esprit.tn")
                    .userName("Alumni Demo")
                    .role(DiscussionRole.MEMBER)
                    .status(MemberStatus.PENDING)
                    .joinedAt(LocalDateTime.now().minusDays(1))
                    .build());

            discussionMemberRepository.save(ForumDiscussionMember.builder()
                    .discussion(cyberDiscussion)
                    .userEmail("yasmine.ayari@esprit.tn")
                    .userName("Yasmine Ayari")
                    .role(DiscussionRole.MEMBER)
                    .status(MemberStatus.PENDING)
                    .joinedAt(LocalDateTime.now().minusHours(4))
                    .build());

            discussionMemberRepository.save(ForumDiscussionMember.builder()
                    .discussion(cyberDiscussion)
                    .userEmail("ahmed.mansour@esprit.tn")
                    .userName("Ahmed Mansour")
                    .role(DiscussionRole.MEMBER)
                    .status(MemberStatus.PENDING)
                    .joinedAt(LocalDateTime.now().minusHours(2))
                    .build());

            // Autre groupe actif pour tester l'autre rôle
            ForumDiscussion cloudDiscussion = discussionRepository.save(ForumDiscussion.builder()
                    .name("DevOps & Cloud Computing")
                    .description("Communauté autour de AWS, Azure, Docker, Kubernetes et pipelines CI/CD.")
                    .creatorEmail("alumni.demo@esprit.tn")
                    .creatorName("Alumni Demo")
                    .isPrivate(true)
                    .status(DiscussionStatus.APPROVED)
                    .category(softwareCategory)
                    .createdAt(LocalDateTime.now().minusDays(10))
                    .build());

            discussionMemberRepository.save(ForumDiscussionMember.builder()
                    .discussion(cloudDiscussion)
                    .userEmail("alumni.demo@esprit.tn")
                    .userName("Alumni Demo")
                    .role(DiscussionRole.CREATOR)
                    .status(MemberStatus.APPROVED)
                    .joinedAt(LocalDateTime.now().minusDays(10))
                    .build());

            discussionMemberRepository.save(ForumDiscussionMember.builder()
                    .discussion(cloudDiscussion)
                    .userEmail("etudiant.demo@esprit.tn")
                    .userName("Étudiant Demo")
                    .role(DiscussionRole.MEMBER)
                    .status(MemberStatus.PENDING)
                    .joinedAt(LocalDateTime.now().minusDays(2))
                    .build());
        }

        // 2. Création de publications et commentaires réalistes
        // Post 1 - PFE & Stages (Épinglé)
        ForumPost post1 = postRepository.save(ForumPost.builder()
                .title("Conseils clés pour décrocher et réussir son stage PFE en Data / IA")
                .content("Bonjour à tous les Espritiens ! Anciennement étudiant à Esprit, j'ai décroché mon PFE dans une grande multinationale et j'y travaille aujourd'hui en tant que Data Scientist. Voici mes 3 conseils essentiels :\n\n1. Soyez irréprochables sur les fondamentaux (Python, SQL et notions de Machine Learning).\n2. Développez au moins un projet Github personnel complet (de l'ingestion à la modélisation) et mettez-le en valeur sur votre CV.\n3. Entraînez-vous à pitcher votre projet en 2 minutes.\n\nBon courage à tous !")
                .category(softwareCategory)
                .authorName("Karim Trabelsi")
                .authorEmail("karim.trabelsi.alumni@esprit.tn")
                .authorRole(Role.ALUMNI)
                .pinned(true)
                .reported(false)
                .viewsCount(240)
                .build());

        replyRepository.save(ForumReply.builder()
                .post(post1)
                .content("Merci beaucoup pour ces conseils précieux Karim ! Conseilles-tu de passer des certifications Cloud (AWS/Azure) pour se démarquer avant d'entamer le stage ?")
                .authorName("Yasmine Ayari")
                .authorEmail("yasmine.ayari@esprit.tn")
                .authorRole(Role.ETUDIANT)
                .build());

        replyRepository.save(ForumReply.builder()
                .post(post1)
                .content("Bonjour Yasmine et Karim ! En tant qu'enseignant, je confirme à 100% les propos de Karim. Concernant ta question Yasmine : les certifications Cloud sont en effet un excellent signal, mais la pratique et la maîtrise d'un projet Git structuré restent prioritaires aux yeux des recruteurs techniques. Concentrez-vous sur le concret !")
                .authorName("Prof. Mohamed Ben Ali")
                .authorEmail("mohamed.benali@esprit.tn")
                .authorRole(Role.ENSEIGNANT)
                .build());

        // Post 2 - Vie Estudiantine & Clubs
        ForumPost post2 = postRepository.save(ForumPost.builder()
                .title("Quels sont les meilleurs clubs pour débuter en Cybersécurité à Esprit ?")
                .content("Salut tout le monde ! Je suis actuellement en 2ème année et je souhaite m'orienter vers la spécialité sécurité. J'aimerais savoir quels clubs ou communautés au sein d'Esprit organisent le plus d'ateliers pratiques et de CTF pour les grands débutants. Merci d'avance !")
                .category(softwareCategory)
                .authorName("Skander Feki")
                .authorEmail("skander.feki@esprit.tn")
                .authorRole(Role.ETUDIANT)
                .pinned(false)
                .reported(false)
                .viewsCount(85)
                .build());

        replyRepository.save(ForumReply.builder()
                .post(post2)
                .content("Salut Skander ! Tu devrais absolument faire un tour chez EspritSec ou Esprit Hack. Ils animent des formations hebdomadaires de vulgarisation et préparent les membres aux compétitions nationales de CTF. L'ambiance y est super collaborative !")
                .authorName("Salma Rebai")
                .authorEmail("salma.rebai@esprit.tn")
                .authorRole(Role.ETUDIANT)
                .build());

        // Post 3 - Génie Logiciel
        ForumPost post3 = postRepository.save(ForumPost.builder()
                .title("Supports de cours additionnels - Architecture Microservices & Angular")
                .content("Chers étudiants de 4ème année, j'ai mis à votre disposition sur notre espace partagé des exemples complets d'implémentation de passerelles API (Spring Cloud Gateway) ainsi que l'interconnexion avec un frontend Angular. Ces ressources complètent notre séance de travaux pratiques de cette semaine.")
                .category(softwareCategory)
                .authorName("Prof. Leila Jouini")
                .authorEmail("leila.jouini@esprit.tn")
                .authorRole(Role.ENSEIGNANT)
                .pinned(false)
                .reported(false)
                .viewsCount(150)
                .build());

        replyRepository.save(ForumReply.builder()
                .post(post3)
                .content("Un grand merci Madame ! Les templates nous font gagner un temps précieux pour notre projet intégré.")
                .authorName("Ahmed Mansour")
                .authorEmail("ahmed.mansour@esprit.tn")
                .authorRole(Role.ETUDIANT)
                .build());

        // Post 4 - Post Signalé pour tester la modération
        ForumPost post4 = postRepository.save(ForumPost.builder()
                .title("Vente de projets intégrés tout faits - 100% garantis")
                .content("Hey les gars ! Si vous avez la flemme de coder votre projet de génie logiciel ou de Web, je propose des projets Angular/Spring complets prêts à l'envoi avec rapports rédigés. Contactez-moi par message privé sur Telegram @HackEsprit. Prix très attractif !")
                .category(softwareCategory)
                .authorName("Utilisateur Suspect")
                .authorEmail("suspect@esprit.tn")
                .authorRole(Role.ETUDIANT)
                .pinned(false)
                .reported(true)
                .reportReason("Vente illégale de projets universitaires - Plagiat académique")
                .viewsCount(12)
                .build());

        replyRepository.save(ForumReply.builder()
                .post(post4)
                .content("C'est totalement interdit et passible d'exclusion définitive du conseil de discipline ! Merci de supprimer ce message.")
                .authorName("Firas Ghorbel")
                .authorEmail("firas.ghorbel@esprit.tn")
                .authorRole(Role.ETUDIANT)
                .build());

        // Commentaire signalé sur le post 1 pour démo
        ForumReply replySignale = replyRepository.save(ForumReply.builder()
                .post(post1)
                .content("Message publicitaire spam : Visitez mon site web frauduleux pour acheter des Bitcoins faciles !")
                .authorName("Spammer Anonyme")
                .authorEmail("spam@spambot.com")
                .authorRole(Role.ALUMNI)
                .reported(true)
                .reportReason("Publicité / Spam commercial indésirable")
                .build());

    }

    // ==========================================
    //            GROUPS IMPLEMENTATION
    // ==========================================

    @Override
    @Transactional
    public ForumDiscussion createDiscussion(ForumDiscussion discussion) {
        if (discussion.getName() == null || discussion.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Discussion name is required.");
        }
        if (discussionRepository.findAll().stream().anyMatch(g -> g.getName().equalsIgnoreCase(discussion.getName().trim()))) {
            throw new IllegalArgumentException("A discussion with this name already exists.");
        }
        discussion.setStatus(DiscussionStatus.PENDING_CREATION);
        if (discussion.getTags() == null) {
            discussion.setTags(new ArrayList<>());
        }
        if (discussion.getCategory() != null && discussion.getCategory().getId() != null) {
            discussion.setCategory(getCategoryById(discussion.getCategory().getId()));
        } else {
            // Assign default category if none provided
            List<ForumCategory> categories = categoryRepository.findAll();
            if (!categories.isEmpty()) {
                discussion.setCategory(categories.get(0));
            }
        }
        ForumDiscussion savedDiscussion = discussionRepository.save(discussion);

        // Rejoindre automatiquement le créateur en tant que OWNER et APPROVED
        ForumDiscussionMember creatorMember = ForumDiscussionMember.builder()
                .discussion(savedDiscussion)
                .userEmail(discussion.getCreatorEmail())
                .userName(discussion.getCreatorName())
                .role(DiscussionRole.CREATOR)
                .status(MemberStatus.APPROVED)
                .joinedAt(LocalDateTime.now())
                .build();
        discussionMemberRepository.save(creatorMember);

        return savedDiscussion;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ForumDiscussion> getActiveDiscussions() {
        return discussionRepository.findByStatus(DiscussionStatus.APPROVED);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ForumDiscussion> getPendingDiscussions() {
        return discussionRepository.findByStatus(DiscussionStatus.PENDING_CREATION);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ForumDiscussion> getPendingModifications() {
        return discussionRepository.findByStatus(DiscussionStatus.PENDING_MODIFICATION);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ForumDiscussion> getPendingDeletions() {
        return discussionRepository.findByStatus(DiscussionStatus.PENDING_DELETION);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ForumDiscussion> getMyDiscussions(String userEmail) {
        return discussionMemberRepository.findByUserEmailAndStatus(userEmail, MemberStatus.APPROVED)
                .stream()
                .map(ForumDiscussionMember::getDiscussion)
                .filter(g -> g.getStatus() == DiscussionStatus.APPROVED)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ForumDiscussion> getMyCreatedDiscussions(String userEmail) {
        return discussionRepository.findByCreatorEmail(userEmail);
    }

    @Override
    @Transactional
    public ForumDiscussion approveDiscussion(Long discussionId) {
        ForumDiscussion discussion = discussionRepository.findById(discussionId)
                .orElseThrow(() -> new IllegalArgumentException("Groupe introuvable."));
        if (discussion.getStatus() != DiscussionStatus.PENDING_CREATION) {
            throw new IllegalArgumentException("Le groupe n'est pas en attente d'approbation.");
        }
        discussion.setStatus(DiscussionStatus.APPROVED);
        return discussionRepository.save(discussion);
    }

    @Override
    @Transactional
    public ForumDiscussion rejectDiscussion(Long discussionId) {
        ForumDiscussion discussion = discussionRepository.findById(discussionId)
                .orElseThrow(() -> new IllegalArgumentException("Groupe introuvable."));
        if (discussion.getStatus() != DiscussionStatus.PENDING_CREATION) {
            throw new IllegalArgumentException("Le groupe n'est pas en attente d'approbation.");
        }
        discussion.setStatus(DiscussionStatus.REJECTED);
        return discussionRepository.save(discussion);
    }

    @Override
    @Transactional
    public ForumDiscussionMember requestJoinDiscussion(Long discussionId, String userEmail, String userName) {
        ForumDiscussion discussion = discussionRepository.findById(discussionId)
                .orElseThrow(() -> new IllegalArgumentException("Groupe introuvable."));
        if (discussion.getStatus() != DiscussionStatus.APPROVED) {
            throw new IllegalArgumentException("Ce groupe n'est pas actif.");
        }

        Optional<ForumDiscussionMember> existingOpt = discussionMemberRepository.findByDiscussionIdAndUserEmail(discussionId, userEmail);
        if (existingOpt.isPresent()) {
            ForumDiscussionMember existing = existingOpt.get();
            if (existing.getStatus() == MemberStatus.APPROVED) {
                throw new IllegalArgumentException("Vous êtes déjà membre de ce groupe.");
            } else if (existing.getStatus() == MemberStatus.PENDING) {
                throw new IllegalArgumentException("Votre demande d'adhésion est déjà en attente.");
            } else {
                existing.setStatus(discussion.isPrivate() ? MemberStatus.PENDING : MemberStatus.APPROVED);
                existing.setJoinedAt(LocalDateTime.now());
                return discussionMemberRepository.save(existing);
            }
        }

        MemberStatus initialStatus = discussion.isPrivate() ? MemberStatus.PENDING : MemberStatus.APPROVED;
        ForumDiscussionMember member = ForumDiscussionMember.builder()
                .discussion(discussion)
                .userEmail(userEmail)
                .userName(userName)
                .role(DiscussionRole.MEMBER)
                .status(initialStatus)
                .joinedAt(LocalDateTime.now())
                .build();

        return discussionMemberRepository.save(member);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ForumDiscussionMember> getPendingMemberships(Long discussionId, String currentUserEmail) {
        ForumDiscussionMember requester = discussionMemberRepository.findByDiscussionIdAndUserEmail(discussionId, currentUserEmail)
                .orElseThrow(() -> new IllegalArgumentException("Accès refusé. Vous n'êtes pas membre de ce groupe."));
        if (requester.getRole() != DiscussionRole.CREATOR) {
            throw new IllegalArgumentException("Accès refusé. Seul le propriétaire peut gérer les demandes d'accès.");
        }
        return discussionMemberRepository.findByDiscussionIdAndStatus(discussionId, MemberStatus.PENDING);
    }

    @Override
    @Transactional
    public ForumDiscussionMember approveMembership(Long discussionId, Long memberId, String currentUserEmail) {
        ForumDiscussionMember requester = discussionMemberRepository.findByDiscussionIdAndUserEmail(discussionId, currentUserEmail)
                .orElseThrow(() -> new IllegalArgumentException("Accès refusé."));
        if (requester.getRole() != DiscussionRole.CREATOR) {
            throw new IllegalArgumentException("Accès refusé. Seul le propriétaire peut approuver les demandes.");
        }

        ForumDiscussionMember member = discussionMemberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Demande d'adhésion introuvable."));
        if (!member.getDiscussion().getId().equals(discussionId)) {
            throw new IllegalArgumentException("Le membre ne correspond pas au groupe spécifié.");
        }
        member.setStatus(MemberStatus.APPROVED);
        member.setJoinedAt(LocalDateTime.now());
        return discussionMemberRepository.save(member);
    }

    @Override
    @Transactional
    public ForumDiscussionMember rejectMembership(Long discussionId, Long memberId, String currentUserEmail) {
        ForumDiscussionMember requester = discussionMemberRepository.findByDiscussionIdAndUserEmail(discussionId, currentUserEmail)
                .orElseThrow(() -> new IllegalArgumentException("Accès refusé."));
        if (requester.getRole() != DiscussionRole.CREATOR) {
            throw new IllegalArgumentException("Accès refusé. Seul le propriétaire peut refuser les demandes.");
        }

        ForumDiscussionMember member = discussionMemberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Demande d'adhésion introuvable."));
        if (!member.getDiscussion().getId().equals(discussionId)) {
            throw new IllegalArgumentException("Le membre ne correspond pas au groupe spécifié.");
        }
        member.setStatus(MemberStatus.REJECTED);
        return discussionMemberRepository.save(member);
    }

    @Override
    @Transactional
    public void leaveDiscussion(Long discussionId, String userEmail) {
        ForumDiscussionMember member = discussionMemberRepository.findByDiscussionIdAndUserEmail(discussionId, userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Vous n'êtes pas membre de ce groupe."));
        if (member.getRole() == DiscussionRole.CREATOR) {
            throw new IllegalArgumentException("Le propriétaire ne peut pas quitter le groupe sans d'abord transférer la propriété.");
        }
        discussionMemberRepository.delete(member);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ForumDiscussionMember> getDiscussionMembers(Long discussionId) {
        return discussionMemberRepository.findByDiscussionIdAndStatus(discussionId, MemberStatus.APPROVED);
    }

    @Override
    @Transactional(readOnly = true)
    public ForumDiscussion getDiscussionById(Long discussionId) {
        return discussionRepository.findById(discussionId)
                .orElseThrow(() -> new IllegalArgumentException("Groupe introuvable avec l'ID: " + discussionId));
    }

    @Override
    @Transactional(readOnly = true)
    public ForumDiscussion getDiscussionByName(String name) {
        return discussionRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new IllegalArgumentException("Groupe introuvable avec le nom: " + name));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ForumDiscussionMember> getUserMemberships(String userEmail) {
        return discussionMemberRepository.findByUserEmail(userEmail);
    }

    @Override
    @Transactional
    public ForumDiscussion updateDiscussion(Long discussionId, ForumDiscussion discussionDetails, String userEmail) {
        ForumDiscussion discussion = discussionRepository.findById(discussionId)
                .orElseThrow(() -> new IllegalArgumentException("Discussion introuvable."));
        
        if (!discussion.getCreatorEmail().equalsIgnoreCase(userEmail)) {
            throw new IllegalArgumentException("Seul le créateur peut modifier cette discussion.");
        }
        
        discussion.setPendingName(discussionDetails.getName());
        discussion.setPendingDescription(discussionDetails.getDescription());
        discussion.setPendingIsPrivate(discussionDetails.isPrivate());
        discussion.setStatus(DiscussionStatus.PENDING_MODIFICATION);
        
        return discussionRepository.save(discussion);
    }

    @Override
    @Transactional
    public ForumDiscussion approveDiscussionModification(Long discussionId) {
        ForumDiscussion discussion = discussionRepository.findById(discussionId)
                .orElseThrow(() -> new IllegalArgumentException("Discussion introuvable."));
        
        if (discussion.getStatus() != DiscussionStatus.PENDING_MODIFICATION) {
            throw new IllegalArgumentException("La discussion n'est pas en attente de modification.");
        }
        
        if (discussion.getPendingName() != null) discussion.setName(discussion.getPendingName());
        if (discussion.getPendingDescription() != null) discussion.setDescription(discussion.getPendingDescription());
        if (discussion.getPendingIsPrivate() != null) discussion.setPrivate(discussion.getPendingIsPrivate());
        
        discussion.setPendingName(null);
        discussion.setPendingDescription(null);
        discussion.setPendingIsPrivate(null);
        discussion.setStatus(DiscussionStatus.APPROVED);
        
        return discussionRepository.save(discussion);
    }

    @Override
    @Transactional
    public void requestDeleteDiscussion(Long discussionId, String userEmail) {
        ForumDiscussion discussion = discussionRepository.findById(discussionId)
                .orElseThrow(() -> new IllegalArgumentException("Discussion introuvable."));
                
        if (!discussion.getCreatorEmail().equalsIgnoreCase(userEmail)) {
            throw new IllegalArgumentException("Seul le créateur peut supprimer cette discussion.");
        }
        
        discussion.setStatus(DiscussionStatus.PENDING_DELETION);
        discussionRepository.save(discussion);
    }

    @Override
    @Transactional
    public void approveDeleteDiscussion(Long discussionId) {
        ForumDiscussion discussion = discussionRepository.findById(discussionId)
                .orElseThrow(() -> new IllegalArgumentException("Discussion not found."));

        if (discussion.getStatus() != DiscussionStatus.PENDING_DELETION) {
            throw new IllegalArgumentException("Discussion is not pending deletion.");
        }

        discussionRepository.delete(discussion);
    }

    @Override
    @Transactional
    public void adminDeleteDiscussion(Long discussionId) {
        ForumDiscussion discussion = discussionRepository.findById(discussionId)
                .orElseThrow(() -> new IllegalArgumentException("Discussion not found."));

        if (discussion.getStatus() != DiscussionStatus.APPROVED) {
            throw new IllegalArgumentException("Only approved discussions can be deleted from this action.");
        }

        discussionRepository.delete(discussion);
    }

    @Override
    @Transactional
    public ForumDiscussion rejectDiscussionModification(Long discussionId) {
        ForumDiscussion discussion = discussionRepository.findById(discussionId)
                .orElseThrow(() -> new IllegalArgumentException("Discussion not found."));
        if (discussion.getStatus() != DiscussionStatus.PENDING_MODIFICATION) {
            throw new IllegalArgumentException("Discussion is not pending modification.");
        }
        discussion.setPendingName(null);
        discussion.setPendingDescription(null);
        discussion.setPendingIsPrivate(null);
        discussion.setStatus(DiscussionStatus.APPROVED);
        return discussionRepository.save(discussion);
    }

    @Override
    @Transactional
    public void rejectDeletionRequest(Long discussionId) {
        ForumDiscussion discussion = discussionRepository.findById(discussionId)
                .orElseThrow(() -> new IllegalArgumentException("Discussion not found."));
        if (discussion.getStatus() != DiscussionStatus.PENDING_DELETION) {
            throw new IllegalArgumentException("Discussion is not pending deletion.");
        }
        discussion.setStatus(DiscussionStatus.APPROVED);
        discussionRepository.save(discussion);
    }

    @Override
    @Transactional
    public void deletePostInDiscussion(Long discussionId, Long postId, String userEmail) {
        ForumDiscussion discussion = discussionRepository.findById(discussionId)
                .orElseThrow(() -> new IllegalArgumentException("Discussion not found."));

        ForumDiscussionMember member = discussionMemberRepository.findByDiscussionIdAndUserEmail(discussionId, userEmail)
                .orElse(null);
        boolean isCreator = discussion.getCreatorEmail().equalsIgnoreCase(userEmail);
        boolean isModerator = member != null && (member.getRole() == DiscussionRole.CREATOR || member.getRole() == DiscussionRole.MODERATOR);

        if (!isCreator && !isModerator) {
            throw new IllegalArgumentException("Only the discussion creator or moderators can delete posts.");
        }

        ForumPost post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found."));

        if (post.getForumDiscussion() == null || !post.getForumDiscussion().getId().equals(discussionId)) {
            throw new IllegalArgumentException("This post does not belong to this discussion.");
        }

        postRepository.delete(post);
    }

}