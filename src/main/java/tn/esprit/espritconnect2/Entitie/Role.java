package tn.esprit.espritconnect2.Entitie;

public enum Role {
    ETUDIANT,
    ALUMNI,
    ENTREPRISE,
    ENSEIGNANT,
    ADMIN;

    /** 2FA réservée aux comptes utilisateurs (pas admin ni entreprise). */
    public boolean isMfaEligible() {
        return this != ADMIN && this != ENTREPRISE;
    }
}

