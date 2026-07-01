package tn.esprit.espritconnect2.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.espritconnect2.DTO.*;
import tn.esprit.espritconnect2.Entitie.ForumCategory;
import tn.esprit.espritconnect2.Entitie.ForumPost;
import tn.esprit.espritconnect2.Entitie.ForumReply;
import tn.esprit.espritconnect2.Entitie.PostStatus;
import tn.esprit.espritconnect2.Service.ForumMediaService;
import tn.esprit.espritconnect2.Service.ForumPostAiService;
import tn.esprit.espritconnect2.Service.ForumPostRecommendationService;
import tn.esprit.espritconnect2.Service.ForumService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/forum")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ForumController {

    private final ForumService forumService;
    private final ForumPostAiService forumPostAiService;
    private final ForumMediaService forumMediaService;
    private final ForumPostRecommendationService recommendationService;

    // ==========================================
    //            CATEGORIES ENDPOINTS
    // ==========================================

    @GetMapping("/categories")
    public ResponseEntity<List<ForumCategory>> getAllCategories() {
        return ResponseEntity.ok(forumService.getAllCategories());
    }

    @GetMapping("/categories/{id}")
    public ResponseEntity<ForumCategory> getCategoryById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(forumService.getCategoryById(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/categories")
    public ResponseEntity<?> createCategory(@RequestBody ForumCategory category) {
        try {
            ForumCategory created = forumService.createCategory(category);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable Long id, @RequestBody ForumCategory category) {
        try {
            ForumCategory updated = forumService.updateCategory(id, category);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        try {
            forumService.deleteCategory(id);
            return ResponseEntity.ok(Map.of("message", "Category deleted successfully."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/categories/post-counts")
    public ResponseEntity<Map<Long, Long>> getPostCountsPerCategory() {
        return ResponseEntity.ok(forumService.getPostCountsPerCategory());
    }

    // ==========================================
    //               POSTS ENDPOINTS
    // ==========================================

    @GetMapping("/posts")
    public ResponseEntity<List<ForumPost>> getFilteredPosts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String authorRole,
            @RequestParam(required = false) Boolean reported,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long discussionId,
            @RequestParam(required = false) PostStatus status,
            @RequestParam(required = false) String authorEmail) {
        return ResponseEntity.ok(forumService.getFilteredPosts(categoryId, authorRole, reported, search, discussionId, status, authorEmail));
    }

    @GetMapping("/posts/mine")
    public ResponseEntity<?> getMyPosts(
            @RequestParam String authorEmail,
            @RequestParam(required = false) PostStatus status) {
        try {
            return ResponseEntity.ok(forumService.getMyPosts(authorEmail, status));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/posts/pending")
    public ResponseEntity<List<ForumPost>> getPendingPosts() {
        return ResponseEntity.ok(forumService.getPendingPosts());
    }

    @GetMapping("/posts/matches")
    public ResponseEntity<List<PostMatchResponse>> getPostMatches(@RequestParam String email) {
        return ResponseEntity.ok(forumService.getPostMatchesForUser(email));
    }

    @GetMapping("/posts/recommendations")
    public ResponseEntity<PostRecommendationResponse> getPostRecommendations(
            @RequestParam String email,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "en") String lang,
            @RequestParam(defaultValue = "false") boolean refresh) {
        try {
            return ResponseEntity.ok(recommendationService.recommend(email, limit, lang, refresh));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/posts/recommendations/refresh")
    public ResponseEntity<Map<String, String>> refreshPostRecommendations(@RequestParam String email) {
        recommendationService.refresh(email);
        return ResponseEntity.ok(Map.of("message", "Recommendation cache cleared."));
    }

    @PostMapping("/posts/recommendations/dismiss")
    public ResponseEntity<Map<String, String>> dismissPostRecommendation(
            @RequestParam String email,
            @RequestParam Long postId) {
        recommendationService.dismiss(email, postId);
        return ResponseEntity.ok(Map.of("message", "Recommendation dismissed."));
    }

    @GetMapping("/posts/{id}")
    public ResponseEntity<ForumPost> getPostById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(forumService.getPostById(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/discussions/{discussionId}/posts")
    public ResponseEntity<List<ForumPost>> getDiscussionPosts(@PathVariable Long discussionId) {
        return ResponseEntity.ok(forumService.getDiscussionPostsWithReplies(discussionId));
    }

    @GetMapping("/posts/engagement")
    public ResponseEntity<Map<String, Object>> getPostEngagement(
            @RequestParam String email,
            @RequestParam(required = false) Long discussionId) {
        return ResponseEntity.ok(forumService.getUserPostEngagement(email, discussionId));
    }

    @GetMapping("/posts/favorites")
    public ResponseEntity<List<ForumPost>> getFavoritePosts(@RequestParam String email) {
        return ResponseEntity.ok(forumService.getFavoritePosts(email));
    }

    @PutMapping("/posts/{id}/like")
    public ResponseEntity<?> togglePostLike(@PathVariable Long id, @RequestParam String email) {
        try {
            return ResponseEntity.ok(forumService.togglePostLike(id, email));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/posts/{id}/favorite")
    public ResponseEntity<?> togglePostFavorite(@PathVariable Long id, @RequestParam String email) {
        try {
            return ResponseEntity.ok(forumService.togglePostFavorite(id, email));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/posts/{id}/view")
    public ResponseEntity<?> incrementPostView(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(forumService.incrementPostView(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/posts")
    public ResponseEntity<?> createPost(@RequestBody ForumPost post) {
        try {
            if (post.getStatus() == null) {
                post.setStatus(PostStatus.PUBLISHED);
            }
            ForumPost created = forumService.createPost(post);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/posts/create")
    public ResponseEntity<?> createPostAdvanced(@RequestBody CreatePostRequest request) {
        try {
            ForumPost created = forumService.createPostFromRequest(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/posts/generate")
    public ResponseEntity<?> generatePostContent(@RequestBody GeneratePostRequest request) {
        try {
            return ResponseEntity.ok(forumPostAiService.generateContent(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping(value = "/posts/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadMedia(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "image") String type) {
        try {
            String url = switch (type.toLowerCase()) {
                case "video" -> forumMediaService.uploadVideo(file);
                case "pdf" -> forumMediaService.uploadPdf(file);
                default -> forumMediaService.uploadImage(file);
            };
            return ResponseEntity.ok(Map.of("url", url, "type", type));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping(value = "/posts/import-pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> importPdf(@RequestParam("file") MultipartFile file) {
        try {
            String extracted = forumMediaService.extractPdfText(file);
            String pdfUrl = forumMediaService.uploadPdf(file);
            PdfImportResponse response = PdfImportResponse.builder()
                    .extractedText(extracted)
                    .pdfUrl(pdfUrl)
                    .pageCount(forumMediaService.countPdfPages(file))
                    .fileName(file.getOriginalFilename())
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/posts/{id}")
    public ResponseEntity<?> updatePost(@PathVariable Long id, @RequestBody ForumPost post) {
        try {
            ForumPost updated = forumService.updatePost(id, post);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/posts/{id}/edit")
    public ResponseEntity<?> updatePostAdvanced(@PathVariable Long id, @RequestBody CreatePostRequest request) {
        try {
            ForumPost updated = forumService.updatePostFromRequest(id, request);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PatchMapping("/posts/{id}/submit")
    public ResponseEntity<?> submitPost(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String email = body.get("authorEmail");
            return ResponseEntity.ok(forumService.submitPost(id, email));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PatchMapping("/posts/{id}/approve")
    public ResponseEntity<?> approvePost(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(forumService.approvePost(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PatchMapping("/posts/{id}/reject")
    public ResponseEntity<?> rejectPost(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String reason = body.getOrDefault("reason", "Rejected by moderator.");
            return ResponseEntity.ok(forumService.rejectPost(id, reason));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id) {
        try {
            forumService.deletePost(id);
            return ResponseEntity.ok(Map.of("message", "Post deleted successfully."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/posts/{id}/pin")
    public ResponseEntity<?> togglePinPost(@PathVariable Long id) {
        try {
            ForumPost updated = forumService.togglePinPost(id);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/posts/{id}/report")
    public ResponseEntity<?> reportPost(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String reason = body.getOrDefault("reason", "Inappropriate content");
            ForumPost updated = forumService.reportPost(id, reason);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/posts/{id}/resolve-report")
    public ResponseEntity<?> resolvePostReport(@PathVariable Long id) {
        try {
            ForumPost updated = forumService.resolvePostReport(id);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ==========================================
    //              REPLIES ENDPOINTS
    // ==========================================

    @PostMapping("/posts/{id}/replies")
    public ResponseEntity<?> addReply(
            @PathVariable Long id, 
            @RequestBody ForumReply reply,
            @RequestParam(required = false) Long parentReplyId) {
        try {
            ForumReply created = forumService.addReply(id, reply, parentReplyId);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/replies/{id}")
    public ResponseEntity<?> deleteReply(@PathVariable Long id, @RequestParam String email) {
        try {
            forumService.deleteReply(id, email);
            return ResponseEntity.ok(Map.of("message", "Reply deleted successfully."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/replies/{id}/like")
    public ResponseEntity<?> toggleReplyLike(@PathVariable Long id, @RequestParam String email) {
        try {
            return ResponseEntity.ok(forumService.toggleReplyLike(id, email));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/replies/{id}/report")
    public ResponseEntity<?> reportReply(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String reason = body.getOrDefault("reason", "Inappropriate content");
            ForumReply updated = forumService.reportReply(id, reason);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/replies/{id}/resolve-report")
    public ResponseEntity<?> resolveReplyReport(@PathVariable Long id) {
        try {
            ForumReply updated = forumService.resolveReplyReport(id);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ==========================================
    //           MODERATION & STATS ENDPOINTS
    // ==========================================

    @GetMapping("/reported-posts")
    public ResponseEntity<List<ForumPost>> getReportedPosts() {
        return ResponseEntity.ok(forumService.getReportedPosts());
    }

    @GetMapping("/reported-replies")
    public ResponseEntity<List<ForumReply>> getReportedReplies() {
        return ResponseEntity.ok(forumService.getReportedReplies());
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        return ResponseEntity.ok(forumService.getDashboardStats());
    }
}
