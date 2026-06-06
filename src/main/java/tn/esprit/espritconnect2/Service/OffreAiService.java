package tn.esprit.espritconnect2.Service;

import org.springframework.stereotype.Service;
import tn.esprit.espritconnect2.DTO.OffreAiSuggestionRequestDTO;
import tn.esprit.espritconnect2.DTO.OffreAiSuggestionResponseDTO;
import tn.esprit.espritconnect2.Entitie.Type;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class OffreAiService {

    public OffreAiSuggestionResponseDTO suggest(OffreAiSuggestionRequestDTO req) {
        String titre = safe(req.getTitre());
        String domaine = safe(req.getDomaine());
        Type type = req.getTypeOffre() != null ? req.getTypeOffre() : Type.STAGE;
        String location = safe(req.getLocalisation());
        String notes = safe(req.getBriefNotes());

        String typeLabel = switch (type) {
            case EMPLOI -> "emploi";
            case APPRENTISSAGE -> "apprentissage";
            case STAGE -> "stage";
            default -> "opportunite";
        };

        String description = """
                %s — %s

                Nous recherchons un profil motive pour rejoindre notre equipe%s.
                Domaine : %s.
                %s

                Missions principales :
                - Contribuer aux projets lies a %s
                - Collaborer avec les equipes techniques et metier
                - Participer aux revues et a l'amelioration continue

                Profil attendu :
                - Bonnes bases en %s
                - Esprit d'equipe, autonomie et communication
                - Capacite a apprendre rapidement

                %s
                """
                .formatted(
                        titre.isBlank() ? "Nouvelle offre" : titre,
                        typeLabel,
                        location.isBlank() ? "" : " a " + location,
                        domaine.isBlank() ? "General" : domaine,
                        notes.isBlank() ? "" : "Contexte : " + notes,
                        domaine.isBlank() ? "votre domaine" : domaine,
                        domaine.isBlank() ? "technologies associees" : domaine,
                        location.isBlank() ? "" : "Localisation : " + location + "."
                ).trim();

        List<String> skills = buildSkills(domaine, type, titre, notes);

        return OffreAiSuggestionResponseDTO.builder()
                .suggestedDescription(description)
                .suggestedSkills(skills)
                .aiDisclaimer("Suggestion IA — vous pouvez modifier ou supprimer tout le contenu avant publication.")
                .build();
    }

    private List<String> buildSkills(String domaine, Type type, String titre, String notes) {
        Set<String> skills = new LinkedHashSet<>();
        if (!domaine.isBlank()) {
            for (String part : domaine.split("[,;/]")) {
                String s = part.trim();
                if (s.length() >= 2) {
                    skills.add(capitalize(s));
                }
            }
        }
        String blob = (titre + " " + notes).toLowerCase(Locale.ROOT);
        addIfContains(skills, blob, "angular", "Angular");
        addIfContains(skills, blob, "react", "React");
        addIfContains(skills, blob, "java", "Java");
        addIfContains(skills, blob, "spring", "Spring Boot");
        addIfContains(skills, blob, "python", "Python");
        addIfContains(skills, blob, "sql", "SQL");
        addIfContains(skills, blob, "docker", "Docker");
        addIfContains(skills, blob, "communication", "Communication");
        addIfContains(skills, blob, "agile", "Agile");

        if (skills.isEmpty()) {
            skills.add("Travail en equipe");
            skills.add("Communication");
            if (type == Type.STAGE || type == Type.APPRENTISSAGE) {
                skills.add("Apprentissage rapide");
            } else {
                skills.add("Autonomie");
            }
        }
        return new ArrayList<>(skills);
    }

    private void addIfContains(Set<String> skills, String blob, String keyword, String label) {
        if (blob.contains(keyword)) {
            skills.add(label);
        }
    }

    private String capitalize(String s) {
        if (s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private String safe(String v) {
        return v == null ? "" : v.trim();
    }
}
