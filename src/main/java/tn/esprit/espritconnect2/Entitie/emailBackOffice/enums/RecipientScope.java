package tn.esprit.espritconnect2.Entitie.emailBackOffice.enums;

/**
 * À qui envoyer la campagne.
 */
public enum RecipientScope {
    /** Tous les comptes User activés (enabled=true) */
    ALL_ENABLED_USERS,
    /** Tous les étudiants (table etudiant) */
    ALL_STUDENTS,
    /** Une liste de diffusion précise */
    MAILING_LIST
}