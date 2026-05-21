package tn.esprit.espritconnect2.Entitie.emailBackOffice.enums;

/**
 * Statut d'une campagne "Message users".
 */
public enum EmailCampaignStatus {
    /** Brouillon : l'admin peut encore modifier */
    DRAFT,
    /** En cours d'envoi (optionnel si tu veux traiter en asynchrone plus tard) */
    SENDING,
    /** Terminé : email(s) envoyé(s) */
    SENT,
    /** Annulé */
    CANCELLED
}