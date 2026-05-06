package tn.esprit.espritconnect2.Entitie;


import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "ai_career_assistant")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class AICareerAssistant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_assistant")
    private Long idAssistant;

    private String modele;

    @OneToMany(mappedBy = "aiCareerAssistant")
    private List<Profil> profils;
}
