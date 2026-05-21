package tn.esprit.espritconnect2.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.espritconnect2.Entitie.ForumCategory;
import tn.esprit.espritconnect2.Entitie.ForumPost;
import tn.esprit.espritconnect2.Entitie.ForumReply;
import tn.esprit.espritconnect2.Service.ForumService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/forum")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ForumController {

    private final ForumService forumService;

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
            return ResponseEntity.ok(Map.of("message", "Catégorie supprimée avec succès."));
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
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(forumService.getFilteredPosts(categoryId, authorRole, reported, search));
    }

    @GetMapping("/posts/{id}")
    public ResponseEntity<ForumPost> getPostById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(forumService.getPostById(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/posts")
    public ResponseEntity<?> createPost(@RequestBody ForumPost post) {
        try {
            ForumPost created = forumService.createPost(post);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
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

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id) {
        try {
            forumService.deletePost(id);
            return ResponseEntity.ok(Map.of("message", "Publication supprimée avec succès."));
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
            String reason = body.getOrDefault("reason", "Contenu inapproprié");
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
    public ResponseEntity<?> addReply(@PathVariable Long id, @RequestBody ForumReply reply) {
        try {
            ForumReply created = forumService.addReply(id, reply);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/replies/{id}")
    public ResponseEntity<?> deleteReply(@PathVariable Long id) {
        try {
            forumService.deleteReply(id);
            return ResponseEntity.ok(Map.of("message", "Commentaire supprimé avec succès."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/replies/{id}/report")
    public ResponseEntity<?> reportReply(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String reason = body.getOrDefault("reason", "Contenu inapproprié");
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
