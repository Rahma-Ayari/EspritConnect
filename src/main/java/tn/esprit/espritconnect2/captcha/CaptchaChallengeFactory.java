package tn.esprit.espritconnect2.captcha;

import org.springframework.stereotype.Component;
import tn.esprit.espritconnect2.Entitie.CaptchaType;
import tn.esprit.espritconnect2.captcha.strategy.CaptchaChallengeStrategy;
import tn.esprit.espritconnect2.captcha.strategy.GeneratedChallengePayload;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Sélectionne aléatoirement une stratégie de génération selon le type.
 */
@Component
public class CaptchaChallengeFactory {

    private final Map<CaptchaType, CaptchaChallengeStrategy> strategies;
    private final Random random = new Random();

    public CaptchaChallengeFactory(List<CaptchaChallengeStrategy> strategyList) {
        this.strategies = new EnumMap<>(CaptchaType.class);
        for (CaptchaChallengeStrategy strategy : strategyList) {
            this.strategies.put(strategy.getType(), strategy);
        }
    }

    public GeneratedChallengePayload generateRandom() {
        CaptchaType[] types = strategies.keySet().toArray(new CaptchaType[0]);
        CaptchaType type = types[random.nextInt(types.length)];
        return generate(type);
    }

    public GeneratedChallengePayload generate(CaptchaType type) {
        CaptchaChallengeStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("Type captcha non supporté: " + type);
        }
        return strategy.generate();
    }

    public CaptchaChallengeStrategy getStrategy(CaptchaType type) {
        CaptchaChallengeStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("Type captcha non supporté: " + type);
        }
        return strategy;
    }
}
