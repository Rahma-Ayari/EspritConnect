package tn.esprit.espritconnect2.frontoffice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.esprit.espritconnect2.frontoffice.dto.AIDescriptionRequest;
import tn.esprit.espritconnect2.frontoffice.dto.AIDescriptionResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class AIService {
    
    /**
     * Generate a professional job description based on provided information
     * This is a simplified implementation. In production, you would integrate with
     * an actual AI service like OpenAI, Google's PaLM, or Claude.
     */
    public AIDescriptionResponse generateJobDescription(AIDescriptionRequest request) {
        log.info("Generating job description for: {}", request.getTitre());
        
        // Build the description
        StringBuilder description = new StringBuilder();
        
        // Introduction
        description.append("**À propos du poste**\n\n");
        description.append(String.format("Nous recherchons un(e) %s talentueux(se) pour rejoindre notre équipe dynamique. ",
                request.getTitre()));
        
        if (request.getTypeOffre() != null) {
            String typeText = switch (request.getTypeOffre()) {
                case "STAGE" -> "Ce stage offre une excellente opportunité d'apprentissage et de développement professionnel.";
                case "EMPLOI" -> "Ce poste en CDI offre de nombreuses opportunités de croissance et d'évolution.";
                case "APPRENTISSAGE" -> "Cette alternance combine formation théorique et expérience pratique en entreprise.";
                default -> "Cette opportunité professionnelle vous permettra de développer vos compétences.";
            };
            description.append(typeText).append("\n\n");
        }
        
        // Responsibilities
        description.append("**Vos missions principales**\n\n");
        description.append(String.format("- Participer au développement et à l'amélioration de solutions dans le domaine %s\n",
                request.getDomaine() != null ? request.getDomaine() : "informatique"));
        description.append("- Collaborer avec l'équipe technique sur des projets innovants\n");
        description.append("- Contribuer à l'analyse des besoins et à la conception des solutions\n");
        description.append("- Assurer la qualité et la documentation du travail réalisé\n");
        description.append("- Participer aux réunions d'équipe et aux revues de code\n\n");
        
        // Requirements
        description.append("**Profil recherché**\n\n");
        if (request.getCompetences() != null && !request.getCompetences().isEmpty()) {
            description.append("**Compétences techniques requises :**\n");
            for (String comp : request.getCompetences()) {
                description.append(String.format("- Maîtrise de %s\n", comp));
            }
            description.append("\n");
        }
        
        description.append("**Compétences personnelles :**\n");
        description.append("- Esprit d'équipe et bonnes capacités de communication\n");
        description.append("- Autonomie et sens des responsabilités\n");
        description.append("- Curiosité technique et volonté d'apprendre\n");
        description.append("- Rigueur et sens de l'organisation\n\n");
        
        // Benefits
        description.append("**Ce que nous offrons**\n\n");
        description.append("- Un environnement de travail moderne et stimulant\n");
        description.append("- Des opportunités de formation et de développement professionnel\n");
        description.append("- Une équipe passionnée et bienveillante\n");
        description.append("- Des projets variés et challengeants\n");
        
        if (request.getLocalisation() != null && !request.getLocalisation().isEmpty()) {
            description.append(String.format("- Lieu de travail : %s\n", request.getLocalisation()));
        }
        
        // Add custom prompt if provided
        if (request.getPrompt() != null && !request.getPrompt().isEmpty()) {
            description.append("\n**Informations complémentaires**\n\n");
            description.append(request.getPrompt());
        }
        
        // Suggest additional skills based on the title and domain
        List<String> suggestedSkills = generateSuggestedSkills(request);
        
        return new AIDescriptionResponse(description.toString(), suggestedSkills);
    }
    
    private List<String> generateSuggestedSkills(AIDescriptionRequest request) {
        List<String> suggested = new ArrayList<>();
        
        // Add skills based on keywords in title and domain
        String titleLower = request.getTitre() != null ? request.getTitre().toLowerCase() : "";
        String domaineLower = request.getDomaine() != null ? request.getDomaine().toLowerCase() : "";
        
        // Web Development
        if (titleLower.contains("web") || titleLower.contains("full stack") || domaineLower.contains("web")) {
            suggested.addAll(Arrays.asList("HTML/CSS", "JavaScript", "REST APIs"));
        }
        
        // Frontend
        if (titleLower.contains("frontend") || titleLower.contains("front-end")) {
            suggested.addAll(Arrays.asList("React", "Angular", "Vue.js"));
        }
        
        // Backend
        if (titleLower.contains("backend") || titleLower.contains("back-end")) {
            suggested.addAll(Arrays.asList("Spring Boot", "Node.js", "Python"));
        }
        
        // Mobile
        if (titleLower.contains("mobile") || domaineLower.contains("mobile")) {
            suggested.addAll(Arrays.asList("React Native", "Flutter", "iOS/Android"));
        }
        
        // Data Science/AI
        if (titleLower.contains("data") || titleLower.contains("ai") || titleLower.contains("machine learning")) {
            suggested.addAll(Arrays.asList("Python", "TensorFlow", "SQL", "Data Analysis"));
        }
        
        // DevOps
        if (titleLower.contains("devops") || domaineLower.contains("devops")) {
            suggested.addAll(Arrays.asList("Docker", "Kubernetes", "CI/CD", "AWS/Azure"));
        }
        
        // General skills
        if (suggested.isEmpty()) {
            suggested.addAll(Arrays.asList("Git", "Agile/Scrum", "Problem Solving"));
        }
        
        // Remove duplicates if any skills were already in the request
        if (request.getCompetences() != null) {
            suggested.removeAll(request.getCompetences());
        }
        
        return suggested.stream().limit(5).toList();
    }
}
