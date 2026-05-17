package tn.esprit.espritconnect2.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.espritconnect2.DTO.BadgeDTO;
import tn.esprit.espritconnect2.DTO.BadgeReqDTO;
import tn.esprit.espritconnect2.DTO.UserBadgeDTO;
import tn.esprit.espritconnect2.DTO.BadgeRequestDTO;
import tn.esprit.espritconnect2.DTO.BadgeRequestReqDTO;
import tn.esprit.espritconnect2.Service.IBadgeService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/badges")
@RequiredArgsConstructor
public class BadgeController {

    private final IBadgeService badgeService;

    @PostMapping
    public ResponseEntity<BadgeDTO> createBadge(@Valid @RequestBody BadgeReqDTO req) {
        return new ResponseEntity<>(badgeService.createBadge(req), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BadgeDTO> updateBadge(@PathVariable Long id, @Valid @RequestBody BadgeReqDTO req) {
        return ResponseEntity.ok(badgeService.updateBadge(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBadge(@PathVariable Long id) {
        badgeService.deleteBadge(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<BadgeDTO>> getAllBadges() {
        return ResponseEntity.ok(badgeService.getAllBadges());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BadgeDTO> getBadgeById(@PathVariable Long id) {
        return ResponseEntity.ok(badgeService.getBadgeById(id));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<BadgeDTO> toggleBadgeStatus(@PathVariable Long id) {
        return ResponseEntity.ok(badgeService.toggleBadgeStatus(id));
    }

    @PostMapping("/{badgeId}/assign/{userId}")
    public ResponseEntity<UserBadgeDTO> assignBadgeToUser(@PathVariable Long badgeId, @PathVariable UUID userId) {
        return new ResponseEntity<>(badgeService.assignBadgeToUser(badgeId, userId), HttpStatus.CREATED);
    }

    @DeleteMapping("/{badgeId}/remove/{userId}")
    public ResponseEntity<Void> removeBadgeFromUser(@PathVariable Long badgeId, @PathVariable UUID userId) {
        badgeService.removeBadgeFromUser(badgeId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserBadgeDTO>> getUserBadges(@PathVariable UUID userId) {
        return ResponseEntity.ok(badgeService.getUserBadges(userId));
    }

    @PostMapping("/request")
    public ResponseEntity<BadgeRequestDTO> requestBadge(@Valid @RequestBody BadgeRequestReqDTO req) {
        return new ResponseEntity<>(badgeService.requestBadge(req), HttpStatus.CREATED);
    }

    @GetMapping("/requests")
    public ResponseEntity<List<BadgeRequestDTO>> getAllRequests() {
        return ResponseEntity.ok(badgeService.getAllRequests());
    }

    @PatchMapping("/requests/{requestId}/handle")
    public ResponseEntity<Void> handleRequest(@PathVariable Long requestId, @RequestParam boolean approved) {
        badgeService.handleRequest(requestId, approved);
        return ResponseEntity.noContent().build();
    }
}
