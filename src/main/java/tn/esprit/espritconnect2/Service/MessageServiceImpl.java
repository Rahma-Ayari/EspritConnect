package tn.esprit.espritconnect2.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.espritconnect2.DTO.MessageRequestDTO;
import tn.esprit.espritconnect2.DTO.MessageResponseDTO;
import tn.esprit.espritconnect2.Entitie.Message;
import tn.esprit.espritconnect2.Entitie.Profil;
import tn.esprit.espritconnect2.Repository.MessageRepository;
import tn.esprit.espritconnect2.Repository.ProfilRepository;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implémentation du service Message.
 * Gère la logique métier : validation, mapping, persistance.
 */
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements IMessageService {

    private final MessageRepository messageRepository;

    // Nécessaire pour retrouver le Profil lié avant de sauvegarder le message
    private final ProfilRepository profilRepository;

    // ─── Mapper DTO → Entité ─────────────────────────────────────────────────
    /**
     * Convertit un MessageRequestDTO en entité Message.
     * On résout la relation ManyToOne en chargeant le Profil depuis la BDD.
     */
    private Message toEntity(MessageRequestDTO dto) {
        // Vérifier que le profil cible existe bien en base
        Profil profil = profilRepository.findById(dto.getIdProfil())
                .orElseThrow(() -> new RuntimeException(
                        "Profil introuvable avec l'id : " + dto.getIdProfil()));

        Message m = new Message();
        m.setUserId(dto.getUserId());
        m.setExpediteur(dto.getExpediteur());
        m.setContenu(dto.getContenu());
        m.setLu(dto.getLu() != null ? dto.getLu() : false); // false par défaut
        m.setProfil(profil); // liaison avec le profil
        return m;
    }

    // ─── Mapper Entité → ResponseDTO ─────────────────────────────────────────
    /**
     * Convertit une entité Message en MessageResponseDTO.
     * On extrait seulement l'idProfil pour éviter la récursion infinie.
     */
    private MessageResponseDTO toDTO(Message m) {
        return MessageResponseDTO.builder()
                .idMessage(m.getIdMessage())
                .userId(m.getUserId())
                .dateEnvoi(m.getDateEnvoi())
                .lu(m.getLu())
                .expediteur(m.getExpediteur())
                .contenu(m.getContenu())
                // On expose seulement l'id du profil, pas l'objet entier
                .idProfil(m.getProfil() != null ? m.getProfil().getIdProfil() : null)
                .build();
    }

    // ─── CREATE ──────────────────────────────────────────────────────────────
    @Override
    public MessageResponseDTO envoyerMessage(MessageRequestDTO dto) {
        Message message = toEntity(dto);
        // Date d'envoi générée automatiquement côté serveur
        message.setDateEnvoi(new Date());
        Message saved = messageRepository.save(message);
        return toDTO(saved);
    }

    // ─── READ ALL ─────────────────────────────────────────────────────────────
    @Override
    public List<MessageResponseDTO> getAllMessages() {
        return messageRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ─── READ BY ID ───────────────────────────────────────────────────────────
    @Override
    public MessageResponseDTO getMessageById(Long id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Message introuvable avec l'id : " + id));
        return toDTO(message);
    }

    // ─── READ BY PROFIL ───────────────────────────────────────────────────────
    @Override
    public List<MessageResponseDTO> getMessagesByProfil(Long idProfil) {
        // Vérifier que le profil existe avant de chercher ses messages
        if (!profilRepository.existsById(idProfil)) {
            throw new RuntimeException("Profil introuvable avec l'id : " + idProfil);
        }
        return messageRepository.findByProfil_IdProfil(idProfil)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ─── READ NON LUS ─────────────────────────────────────────────────────────
    @Override
    public List<MessageResponseDTO> getMessagesNonLus(Long idProfil) {
        // Retourne uniquement les messages avec lu = false pour ce profil
        return messageRepository.findByProfil_IdProfilAndLuFalse(idProfil)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ─── MARQUER COMME LU ─────────────────────────────────────────────────────
    @Override
    public MessageResponseDTO marquerCommeLu(Long id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Message introuvable avec l'id : " + id));
        // On change uniquement le flag lu → true
        message.setLu(true);
        return toDTO(messageRepository.save(message));
    }

    // ─── UPDATE ───────────────────────────────────────────────────────────────
    @Override
    public MessageResponseDTO updateMessage(Long id, MessageRequestDTO dto) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Message introuvable avec l'id : " + id));

        // Mise à jour des champs modifiables
        message.setUserId(dto.getUserId());
        message.setExpediteur(dto.getExpediteur());
        message.setContenu(dto.getContenu());
        message.setLu(dto.getLu() != null ? dto.getLu() : message.getLu());

        // Si l'idProfil a changé, on recharge le nouveau profil
        if (!message.getProfil().getIdProfil().equals(dto.getIdProfil())) {
            Profil nouveauProfil = profilRepository.findById(dto.getIdProfil())
                    .orElseThrow(() -> new RuntimeException(
                            "Profil introuvable avec l'id : " + dto.getIdProfil()));
            message.setProfil(nouveauProfil);
        }

        // On ne modifie PAS la dateEnvoi → elle reste celle de l'envoi original
        return toDTO(messageRepository.save(message));
    }

    // ─── DELETE ───────────────────────────────────────────────────────────────
    @Override
    public void deleteMessage(Long id) {
        if (!messageRepository.existsById(id)) {
            throw new RuntimeException("Message introuvable avec l'id : " + id);
        }
        messageRepository.deleteById(id);
    }
}