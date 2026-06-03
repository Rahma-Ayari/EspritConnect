package tn.esprit.espritconnect2.Entitie;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "type_evenement")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TypeEvenement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_type_evenement")
    private Long idTypeEvenement;

    @NotBlank(message = "Le nom du type est obligatoire")
    @Column(nullable = false, unique = true, length = 120)
    private String nom;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private Boolean actif = true;
}
