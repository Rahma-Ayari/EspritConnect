package tn.esprit.espritconnect2.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.espritconnect2.Entitie.ForumGroup;
import tn.esprit.espritconnect2.Entitie.ForumGroupMember;
import tn.esprit.espritconnect2.Service.ForumService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/forum/groups")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ForumGroupController {

    private final ForumService forumService;

    @PostMapping
    public ResponseEntity<?> createGroup(@RequestBody ForumGroup group) {
        try {
            ForumGroup created = forumService.createGroup(group);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<ForumGroup>> getActiveGroups() {
        return ResponseEntity.ok(forumService.getActiveGroups());
    }

    @GetMapping("/pending")
    public ResponseEntity<List<ForumGroup>> getPendingGroups() {
        return ResponseEntity.ok(forumService.getPendingGroups());
    }

    @GetMapping("/my-groups")
    public ResponseEntity<List<ForumGroup>> getMyGroups(@RequestParam String email) {
        return ResponseEntity.ok(forumService.getMyGroups(email));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getGroupById(@PathVariable Long id) {
        try {
            ForumGroup group = forumService.getGroupById(id);
            return ResponseEntity.ok(group);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveGroup(@PathVariable Long id) {
        try {
            ForumGroup approved = forumService.approveGroup(id);
            return ResponseEntity.ok(approved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectGroup(@PathVariable Long id) {
        try {
            ForumGroup rejected = forumService.rejectGroup(id);
            return ResponseEntity.ok(rejected);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<?> joinGroup(
            @PathVariable Long id,
            @RequestParam String email,
            @RequestParam String name) {
        try {
            ForumGroupMember member = forumService.requestJoinGroup(id, email, name);
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
            List<ForumGroupMember> pending = forumService.getPendingMemberships(id, email);
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
            ForumGroupMember approved = forumService.approveMembership(id, memberId, email);
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
            ForumGroupMember rejected = forumService.rejectMembership(id, memberId, email);
            return ResponseEntity.ok(rejected);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}/leave")
    public ResponseEntity<?> leaveGroup(
            @PathVariable Long id,
            @RequestParam String email) {
        try {
            forumService.leaveGroup(id, email);
            return ResponseEntity.ok(Map.of("message", "Vous avez quitté le groupe avec succès."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<List<ForumGroupMember>> getGroupMembers(@PathVariable Long id) {
        return ResponseEntity.ok(forumService.getGroupMembers(id));
    }

    @GetMapping("/memberships")
    public ResponseEntity<List<ForumGroupMember>> getUserMemberships(@RequestParam String email) {
        return ResponseEntity.ok(forumService.getUserMemberships(email));
    }
}
