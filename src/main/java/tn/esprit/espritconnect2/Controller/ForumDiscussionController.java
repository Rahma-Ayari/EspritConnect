package tn.esprit.espritconnect2.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.espritconnect2.Entitie.ForumDiscussion;
import tn.esprit.espritconnect2.Entitie.ForumDiscussionMember;
import tn.esprit.espritconnect2.Service.ForumService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/forum/discussions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ForumDiscussionController {

    private final ForumService forumService;

    @PostMapping
    public ResponseEntity<?> createDiscussion(@RequestBody ForumDiscussion discussion) {
        try {
            ForumDiscussion created = forumService.createDiscussion(discussion);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<ForumDiscussion>> getActiveDiscussions() {
        return ResponseEntity.ok(forumService.getActiveDiscussions());
    }

    @GetMapping("/pending")
    public ResponseEntity<List<ForumDiscussion>> getPendingDiscussions() {
        return ResponseEntity.ok(forumService.getPendingDiscussions());
    }

    @GetMapping("/pending-modifications")
    public ResponseEntity<List<ForumDiscussion>> getPendingModifications() {
        return ResponseEntity.ok(forumService.getPendingModifications());
    }

    @GetMapping("/pending-deletions")
    public ResponseEntity<List<ForumDiscussion>> getPendingDeletions() {
        return ResponseEntity.ok(forumService.getPendingDeletions());
    }

    @GetMapping("/my-discussions")
    public ResponseEntity<List<ForumDiscussion>> getMyDiscussions(@RequestParam String email) {
        return ResponseEntity.ok(forumService.getMyDiscussions(email));
    }

    @GetMapping("/my-created")
    public ResponseEntity<List<ForumDiscussion>> getMyCreatedDiscussions(@RequestParam String email) {
        return ResponseEntity.ok(forumService.getMyCreatedDiscussions(email));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDiscussionById(@PathVariable Long id) {
        try {
            ForumDiscussion discussion = forumService.getDiscussionById(id);
            return ResponseEntity.ok(discussion);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/by-name/{name}")
    public ResponseEntity<?> getDiscussionByName(@PathVariable String name) {
        try {
            ForumDiscussion discussion = forumService.getDiscussionByName(name);
            return ResponseEntity.ok(discussion);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateDiscussion(@PathVariable Long id, @RequestBody ForumDiscussion discussion, @RequestParam String email) {
        try {
            ForumDiscussion updated = forumService.updateDiscussion(id, discussion, email);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> requestDeleteDiscussion(@PathVariable Long id, @RequestParam String email) {
        try {
            forumService.requestDeleteDiscussion(id, email);
            return ResponseEntity.ok(Map.of("message", "Discussion deletion request submitted."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}/reject-modification")
    public ResponseEntity<?> rejectDiscussionModification(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(forumService.rejectDiscussionModification(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}/reject-deletion")
    public ResponseEntity<?> rejectDeletionRequest(@PathVariable Long id) {
        try {
            forumService.rejectDeletionRequest(id);
            return ResponseEntity.ok(Map.of("message", "Deletion request rejected. Discussion restored."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}/posts/{postId}")
    public ResponseEntity<?> deletePostInDiscussion(@PathVariable Long id, @PathVariable Long postId, @RequestParam String email) {
        try {
            forumService.deletePostInDiscussion(id, postId, email);
            return ResponseEntity.ok(Map.of("message", "Post deleted successfully."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // --- Admin validation endpoints ---

    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveDiscussion(@PathVariable Long id) {
        try {
            ForumDiscussion approved = forumService.approveDiscussion(id);
            return ResponseEntity.ok(approved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectDiscussion(@PathVariable Long id) {
        try {
            ForumDiscussion rejected = forumService.rejectDiscussion(id);
            return ResponseEntity.ok(rejected);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}/approve-modification")
    public ResponseEntity<?> approveDiscussionModification(@PathVariable Long id) {
        try {
            ForumDiscussion approved = forumService.approveDiscussionModification(id);
            return ResponseEntity.ok(approved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}/approve-deletion")
    public ResponseEntity<?> approveDeleteDiscussion(@PathVariable Long id) {
        try {
            forumService.approveDeleteDiscussion(id);
            return ResponseEntity.ok(Map.of("message", "Discussion deleted permanently."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}/admin-delete")
    public ResponseEntity<?> adminDeleteDiscussion(@PathVariable Long id) {
        try {
            forumService.adminDeleteDiscussion(id);
            return ResponseEntity.ok(Map.of("message", "Discussion deleted permanently."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // --- Memberships ---

    @PostMapping("/{id}/join")
    public ResponseEntity<?> joinDiscussion(
            @PathVariable Long id,
            @RequestParam String email,
            @RequestParam String name) {
        try {
            ForumDiscussionMember member = forumService.requestJoinDiscussion(id, email, name);
            return ResponseEntity.ok(member);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{id}/pending-members")
    public ResponseEntity<?> getPendingMemberships(
            @PathVariable Long id,
            @RequestParam String email) {
        try {
            List<ForumDiscussionMember> pending = forumService.getPendingMemberships(id, email);
            return ResponseEntity.ok(pending);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}/members/{memberId}/approve")
    public ResponseEntity<?> approveMembership(
            @PathVariable Long id,
            @PathVariable Long memberId,
            @RequestParam String email) {
        try {
            ForumDiscussionMember approved = forumService.approveMembership(id, memberId, email);
            return ResponseEntity.ok(approved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}/members/{memberId}/reject")
    public ResponseEntity<?> rejectMembership(
            @PathVariable Long id,
            @PathVariable Long memberId,
            @RequestParam String email) {
        try {
            ForumDiscussionMember rejected = forumService.rejectMembership(id, memberId, email);
            return ResponseEntity.ok(rejected);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}/leave")
    public ResponseEntity<?> leaveDiscussion(
            @PathVariable Long id,
            @RequestParam String email) {
        try {
            forumService.leaveDiscussion(id, email);
            return ResponseEntity.ok(Map.of("message", "You left the discussion successfully."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<List<ForumDiscussionMember>> getDiscussionMembers(@PathVariable Long id) {
        return ResponseEntity.ok(forumService.getDiscussionMembers(id));
    }

    @GetMapping("/memberships")
    public ResponseEntity<List<ForumDiscussionMember>> getUserMemberships(@RequestParam String email) {
        return ResponseEntity.ok(forumService.getUserMemberships(email));
    }
}
