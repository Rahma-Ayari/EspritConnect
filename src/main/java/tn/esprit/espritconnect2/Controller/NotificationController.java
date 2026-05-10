package tn.esprit.espritconnect2.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.espritconnect2.DTO.NotificationRequestDTO;
import tn.esprit.espritconnect2.DTO.NotificationResponseDTO;
import tn.esprit.espritconnect2.Service.NotificationServiceImpl;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationServiceImpl notificationService;

    @PostMapping
    public ResponseEntity<NotificationResponseDTO> ajouter(@Valid @RequestBody NotificationRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notificationService.ajouterNotification(dto));
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponseDTO>> getAll() {
        return ResponseEntity.ok(notificationService.getAllNotifications());
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.getNotificationById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NotificationResponseDTO> update(@PathVariable Long id, @Valid @RequestBody NotificationRequestDTO dto) {
        return ResponseEntity.ok(notificationService.updateNotification(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }
}
