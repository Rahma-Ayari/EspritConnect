package tn.esprit.espritconnect2.Controller.frontOffice;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.espritconnect2.DTO.frontOffice.ForumHomeDTO;
import tn.esprit.espritconnect2.DTO.frontOffice.LikeToggleResponseDTO;
import tn.esprit.espritconnect2.DTO.frontOffice.PagedResponseDTO;
import tn.esprit.espritconnect2.Entitie.ForumCategory;
import tn.esprit.espritconnect2.Entitie.ForumPost;
import tn.esprit.espritconnect2.Entitie.ForumReply;
import tn.esprit.espritconnect2.Service.frontOffice.IForumFrontOfficeService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/front-office/forum")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ForumFrontOfficeController {

    private final IForumFrontOfficeService frontOfficeService;

    @GetMapping("/home")
    public ResponseEntity<ForumHomeDTO> getHome() {
        return ResponseEntity.ok(frontOfficeService.getHome());
    }

    @GetMapping("/categories")
    public ResponseEntity<List<ForumCategory>> getCategories() {
        return ResponseEntity.ok(frontOfficeService.getCategories());
    }

    @GetMapping("/categories/{id}")
    public ResponseEntity<ForumCategory> getCategory(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(frontOfficeService.getCategory(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/categories/post-counts")
    public ResponseEntity<Map<Long, Long>> getPostCounts() {
        return ResponseEntity.ok(frontOfficeService.getPostCountsPerCategory());
    }

    @GetMapping("/posts")
    public ResponseEntity<PagedResponseDTO<ForumPost>> getPosts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String authorRole,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String viewerEmail) {
        return ResponseEntity.ok(frontOfficeService.getPublicPosts(categoryId, authorRole, search, page, size, viewerEmail));
    }

    @GetMapping("/posts/{id}")
    public ResponseEntity<?> getPost(@PathVariable Long id,
                                     @RequestParam(required = false) String viewerEmail) {
        try {
            return ResponseEntity.ok(frontOfficeService.getPublicPost(id, viewerEmail));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/my-posts")
    public ResponseEntity<?> getMyPosts(@RequestParam String authorEmail) {
        try {
            return ResponseEntity.ok(frontOfficeService.getMyPosts(authorEmail));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/posts")
    public ResponseEntity<?> createPost(@RequestBody ForumPost post) {
        try {
            ForumPost created = frontOfficeService.createPost(post);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/posts/{id}")
    public ResponseEntity<?> updatePost(@PathVariable Long id,
                                        @RequestParam String authorEmail,
                                        @RequestBody ForumPost post) {
        try {
            return ResponseEntity.ok(frontOfficeService.updatePost(id, post, authorEmail));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id, @RequestParam String authorEmail) {
        try {
            frontOfficeService.deletePost(id, authorEmail);
            return ResponseEntity.ok(Map.of("message", "Publication supprimée."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/posts/{id}/replies")
    public ResponseEntity<?> addReply(@PathVariable Long id, @RequestBody ForumReply reply) {
        try {
            ForumReply created = frontOfficeService.addReply(id, reply);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/replies/{id}")
    public ResponseEntity<?> updateReply(@PathVariable Long id,
                                         @RequestParam String authorEmail,
                                         @RequestBody ForumReply reply) {
        try {
            return ResponseEntity.ok(frontOfficeService.updateReply(id, reply, authorEmail));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/replies/{id}")
    public ResponseEntity<?> deleteReply(@PathVariable Long id, @RequestParam String authorEmail) {
        try {
            frontOfficeService.deleteReply(id, authorEmail);
            return ResponseEntity.ok(Map.of("message", "Commentaire supprimé."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/posts/{id}/like")
    public ResponseEntity<?> toggleLike(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String userEmail = body.get("userEmail");
            LikeToggleResponseDTO result = frontOfficeService.toggleLike(id, userEmail);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/posts/{id}/report")
    public ResponseEntity<?> reportPost(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String reason = body.getOrDefault("reason", "Contenu inapproprié");
            return ResponseEntity.ok(frontOfficeService.reportPost(id, reason));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/replies/{id}/report")
    public ResponseEntity<?> reportReply(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String reason = body.getOrDefault("reason", "Contenu inapproprié");
            return ResponseEntity.ok(frontOfficeService.reportReply(id, reason));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
