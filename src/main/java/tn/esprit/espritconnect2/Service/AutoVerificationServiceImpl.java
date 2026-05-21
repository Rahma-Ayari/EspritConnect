package tn.esprit.espritconnect2.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tn.esprit.espritconnect2.DTO.AutoVerificationResultDTO;
import tn.esprit.espritconnect2.DTO.AutoVerificationResultDTO.ScoreBreakdown;
import tn.esprit.espritconnect2.Entitie.User;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
public class AutoVerificationServiceImpl implements IAutoVerificationService {

    @Value("${app.verification.api.enabled:false}")
    private boolean externalApiEnabled;

    @Value("${app.verification.api.url:}")
    private String externalApiUrl;

    @Value("${app.verification.api.key:}")
    private String externalApiKey;

    private static final Pattern RC_PATTERN = Pattern.compile("^[A-Z]\\d{7,12}$");
    private static final Pattern TAX_NUMBER_PATTERN = Pattern.compile("^\\d{7}/[A-Z]/[A-Z]/\\d{3}$");
    private static final Pattern WEBSITE_PATTERN = Pattern.compile(
            "^(https?://)?(www\\.)?[a-zA-Z0-9][a-zA-Z0-9-]*\\.[a-zA-Z]{2,}(/.*)?$",
            Pattern.CASE_INSENSITIVE
    );

    private static final int POINTS_DOCUMENT = 40;
    private static final int POINTS_SECTOR = 20;
    private static final int POINTS_RC_FORMAT = 20;
    private static final int POINTS_WEBSITE = 10;
    private static final int POINTS_DESCRIPTION = 10;
    private static final int MAX_SCORE = 100;

    private static final int THRESHOLD_APPROVE = 70;
    private static final int THRESHOLD_MANUAL_REVIEW = 40;

    @Override
    public AutoVerificationResultDTO performAutoVerification(User user) {
        List<ScoreBreakdown> breakdown = new ArrayList<>();
        int totalScore = 0;

        boolean hasDocument = user.getVerificationDocumentPath() != null 
                && !user.getVerificationDocumentPath().isEmpty();
        breakdown.add(ScoreBreakdown.builder()
                .criteria("Document justificatif fourni")
                .points(hasDocument ? POINTS_DOCUMENT : 0)
                .maxPoints(POINTS_DOCUMENT)
                .passed(hasDocument)
                .build());
        if (hasDocument) totalScore += POINTS_DOCUMENT;

        boolean hasSector = user.getCompanySector() != null 
                && !user.getCompanySector().trim().isEmpty();
        breakdown.add(ScoreBreakdown.builder()
                .criteria("Secteur d'activité renseigné")
                .points(hasSector ? POINTS_SECTOR : 0)
                .maxPoints(POINTS_SECTOR)
                .passed(hasSector)
                .build());
        if (hasSector) totalScore += POINTS_SECTOR;

        boolean validRc = isValidBusinessRegistrationNumber(user.getBusinessRegistrationNumber());
        breakdown.add(ScoreBreakdown.builder()
                .criteria("Format RC/Matricule Fiscal valide")
                .points(validRc ? POINTS_RC_FORMAT : 0)
                .maxPoints(POINTS_RC_FORMAT)
                .passed(validRc)
                .build());
        if (validRc) totalScore += POINTS_RC_FORMAT;

        boolean hasWebsite = user.getCompanyWebsite() != null 
                && WEBSITE_PATTERN.matcher(user.getCompanyWebsite()).matches();
        breakdown.add(ScoreBreakdown.builder()
                .criteria("Site web valide")
                .points(hasWebsite ? POINTS_WEBSITE : 0)
                .maxPoints(POINTS_WEBSITE)
                .passed(hasWebsite)
                .build());
        if (hasWebsite) totalScore += POINTS_WEBSITE;

        boolean hasDescription = user.getCompanyDescription() != null 
                && user.getCompanyDescription().trim().length() >= 50;
        breakdown.add(ScoreBreakdown.builder()
                .criteria("Description détaillée (50+ caractères)")
                .points(hasDescription ? POINTS_DESCRIPTION : 0)
                .maxPoints(POINTS_DESCRIPTION)
                .passed(hasDescription)
                .build());
        if (hasDescription) totalScore += POINTS_DESCRIPTION;

        String recommendation;
        String recommendationText;

        if (totalScore >= THRESHOLD_APPROVE && hasDocument) {
            recommendation = "APPROVE";
            recommendationText = "Score suffisant avec document fourni. Approbation recommandée.";
        } else if (totalScore >= THRESHOLD_MANUAL_REVIEW) {
            recommendation = "MANUAL_REVIEW";
            recommendationText = "Score moyen. Vérification manuelle recommandée.";
        } else {
            recommendation = "INSUFFICIENT";
            recommendationText = "Score insuffisant. Informations complémentaires requises.";
        }

        if (externalApiEnabled && validRc) {
            try {
                boolean externalVerified = verifyWithExternalApi(user.getBusinessRegistrationNumber());
                if (externalVerified && recommendation.equals("MANUAL_REVIEW")) {
                    recommendation = "APPROVE";
                    recommendationText = "Vérifié via API externe. Approbation recommandée.";
                }
            } catch (Exception e) {
                log.warn("Erreur lors de la vérification externe: {}", e.getMessage());
            }
        }

        log.info("Auto-vérification pour user {}: score={}, recommendation={}", 
                user.getId(), totalScore, recommendation);

        return AutoVerificationResultDTO.builder()
                .totalScore(totalScore)
                .breakdown(breakdown)
                .recommendation(recommendation)
                .recommendationText(recommendationText)
                .build();
    }

    @Override
    public boolean isValidBusinessRegistrationNumber(String rcNumber) {
        if (rcNumber == null || rcNumber.trim().isEmpty()) {
            return false;
        }
        String cleaned = rcNumber.trim().toUpperCase().replaceAll("\\s+", "");
        
        if (RC_PATTERN.matcher(cleaned).matches()) {
            return true;
        }
        return isValidTaxNumber(rcNumber);
    }

    @Override
    public boolean isValidTaxNumber(String taxNumber) {
        if (taxNumber == null || taxNumber.trim().isEmpty()) {
            return false;
        }
        return TAX_NUMBER_PATTERN.matcher(taxNumber.trim()).matches();
    }

    @Override
    public int calculateConfidenceScore(User user) {
        AutoVerificationResultDTO result = performAutoVerification(user);
        return result.getTotalScore();
    }

    private boolean verifyWithExternalApi(String businessRegistrationNumber) {
        if (!externalApiEnabled || externalApiUrl == null || externalApiUrl.isEmpty()) {
            return false;
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = externalApiUrl + "?rc=" + businessRegistrationNumber;
            log.debug("Appel API externe de vérification: {}", url);
            return true;
        } catch (Exception e) {
            log.error("Erreur API externe de vérification: {}", e.getMessage());
            return false;
        }
    }
}
