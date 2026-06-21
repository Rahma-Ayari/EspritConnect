package tn.esprit.espritconnect2.captcha.strategy;

import tn.esprit.espritconnect2.DTO.captcha.CaptchaGenerateResponse;
import tn.esprit.espritconnect2.Entitie.CaptchaType;

import java.util.List;

/**
 * Contrat pour chaque type de défi CAPTCHA.
 * Implémenter cette interface pour ajouter un nouveau type sans modifier le service principal.
 */
public interface CaptchaChallengeStrategy {

    CaptchaType getType();

    GeneratedChallengePayload generate();

    boolean verify(String correctAnswersJson, List<String> userAnswers, int puzzleTolerancePx);
}
