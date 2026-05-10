package tn.esprit.espritconnect2.Service;

import tn.esprit.espritconnect2.DTO.MessageRequestDTO;
import tn.esprit.espritconnect2.DTO.MessageResponseDTO;

import java.util.List;

/**
 * Interface du service Message.
 * Définit le contrat métier — l'implémentation est dans MessageServiceImpl.
 */
public interface IMessageService {

    // Envoyer (créer) un nouveau message
    MessageResponseDTO envoyerMessage(MessageRequestDTO dto);

    // Récupérer tous les messages
    List<MessageResponseDTO> getAllMessages();

    // Récupérer un message par son id
    MessageResponseDTO getMessageById(Long id);

    // Récupérer tous les messages d'un profil donné
    List<MessageResponseDTO> getMessagesByProfil(Long idProfil);

    // Récupérer tous les messages non lus d'un profil
    List<MessageResponseDTO> getMessagesNonLus(Long idProfil);

    // Marquer un message comme lu
    MessageResponseDTO marquerCommeLu(Long id);

    // Modifier le contenu d'un message
    MessageResponseDTO updateMessage(Long id, MessageRequestDTO dto);

    // Supprimer un message
    void deleteMessage(Long id);
}