package tn.esprit.espritconnect2.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.espritconnect2.DTO.MessageRequestDTO;
import tn.esprit.espritconnect2.DTO.MessageResponseDTO;
import tn.esprit.espritconnect2.Service.IMessageService;

import java.util.List;

/**
 * Contrôleur REST pour la gestion des messages.
 * Toutes les routes sont préfixées par /api/messages.
 */
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MessageController {

    // Injection de l'interface → bonne pratique (inversion de dépendance)
    private final IMessageService messageService;

    // ─── POST /api/messages ───────────────────────────────────────────────────
    /**
     * Envoyer un nouveau message.
     * Retourne 201 Created avec le message créé.
     */
    @PostMapping
    public ResponseEntity<MessageResponseDTO> envoyer(
            @Valid @RequestBody MessageRequestDTO dto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(messageService.envoyerMessage(dto));
    }

    // ─── GET /api/messages ────────────────────────────────────────────────────
    /**
     * Récupérer tous les messages (toutes boîtes confondues).
     */
    @GetMapping
    public ResponseEntity<List<MessageResponseDTO>> getAll() {
        return ResponseEntity.ok(messageService.getAllMessages());
    }

    // ─── GET /api/messages/{id} ───────────────────────────────────────────────
    /**
     * Récupérer un message précis par son id.
     */
    @GetMapping("/{id}")
    public ResponseEntity<MessageResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(messageService.getMessageById(id));
    }

    // ─── GET /api/messages/profil/{idProfil} ──────────────────────────────────
    /**
     * Récupérer tous les messages liés à un profil donné.
     * Utile pour afficher la boîte de réception d'un utilisateur.
     */
    @GetMapping("/profil/{idProfil}")
    public ResponseEntity<List<MessageResponseDTO>> getByProfil(
            @PathVariable Long idProfil) {
        return ResponseEntity.ok(messageService.getMessagesByProfil(idProfil));
    }

    // ─── GET /api/messages/profil/{idProfil}/non-lus ─────────────────────────
    /**
     * Récupérer uniquement les messages non lus d'un profil.
     * Utile pour le badge de notifications.
     */
    @GetMapping("/profil/{idProfil}/non-lus")
    public ResponseEntity<List<MessageResponseDTO>> getNonLus(
            @PathVariable Long idProfil) {
        return ResponseEntity.ok(messageService.getMessagesNonLus(idProfil));
    }

    // ─── PATCH /api/messages/{id}/lire ───────────────────────────────────────
    /**
     * Marquer un message comme lu.
     * On utilise PATCH car c'est une mise à jour partielle (un seul champ).
     */
    @PatchMapping("/{id}/lire")
    public ResponseEntity<MessageResponseDTO> marquerLu(@PathVariable Long id) {
        return ResponseEntity.ok(messageService.marquerCommeLu(id));
    }

    // ─── PUT /api/messages/{id} ───────────────────────────────────────────────
    /**
     * Mettre à jour un message existant (contenu, expéditeur, profil…).
     */
    @PutMapping("/{id}")
    public ResponseEntity<MessageResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody MessageRequestDTO dto) {
        return ResponseEntity.ok(messageService.updateMessage(id, dto));
    }

    // ─── DELETE /api/messages/{id} ────────────────────────────────────────────
    /**
     * Supprimer un message.
     * Retourne 204 No Content si succès.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        messageService.deleteMessage(id);
        return ResponseEntity.noContent().build();
    }
}






















