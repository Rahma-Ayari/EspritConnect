package tn.esprit.espritconnect2.Entitie;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "ai_career_assistant")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AICareerAssistant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_assistant")
    private Long idAssistant;

    // Nom du modèle AI utilisé
    private String modele;

    // Fonction principale de l'assistant
    // Exemple :
    // Job Matching
    // Mentor Matching
    // CV Improvement
    // Candidate Ranking
    private String fonctionnalite;

    // Version du modèle IA
    private String versionAssistant;

    // Actif ou non
    private Boolean actif;

    // précision du matching (%)
    private Float scorePrecision;

    @OneToMany(mappedBy = "aiCareerAssistant")
    private List<Profil> profils;
}