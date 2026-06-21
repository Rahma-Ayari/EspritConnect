package tn.esprit.espritconnect2.captcha.strategy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tn.esprit.espritconnect2.DTO.captcha.CaptchaGenerateResponse;
import tn.esprit.espritconnect2.DTO.captcha.CaptchaImageOptionDto;
import tn.esprit.espritconnect2.Entitie.CaptchaType;
import tn.esprit.espritconnect2.captcha.CaptchaAssetCatalog;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class LogoCaptchaStrategy implements CaptchaChallengeStrategy {

    private final CaptchaAssetCatalog catalog;
    private final ObjectMapper objectMapper;

    @Override
    public CaptchaType getType() {
        return CaptchaType.LOGO;
    }

    @Override
    public GeneratedChallengePayload generate() {
        CaptchaAssetCatalog.Asset correct = catalog.getEspritAssets().stream()
                .filter(a -> "esprit-logo".equals(a.getId()))
                .findFirst()
                .orElse(catalog.getEspritAssets().get(0));

        List<CaptchaAssetCatalog.Asset> decoys = new ArrayList<>(catalog.getDecoyAssets());
        Collections.shuffle(decoys);

        List<CaptchaImageOptionDto> logos = new ArrayList<>();
        logos.add(catalog.toImageOption(correct));
        decoys.stream().limit(5).map(catalog::toImageOption).forEach(logos::add);
        Collections.shuffle(logos);

        List<String> correctIds = List.of(correct.getId());

        try {
            String imagesJson = objectMapper.writeValueAsString(logos);
            String correctJson = objectMapper.writeValueAsString(correctIds);

            CaptchaGenerateResponse client = CaptchaGenerateResponse.builder()
                    .captchaType(CaptchaType.LOGO)
                    .question("Où se trouve le logo Esprit ?")
                    .images(logos)
                    .build();

            return GeneratedChallengePayload.builder()
                    .captchaType(CaptchaType.LOGO)
                    .question(client.getQuestion())
                    .imagesJson(imagesJson)
                    .correctAnswersJson(correctJson)
                    .clientPayload(client)
                    .build();
        } catch (Exception e) {
            throw new IllegalStateException("Erreur génération captcha LOGO", e);
        }
    }

    @Override
    public boolean verify(String correctAnswersJson, List<String> userAnswers, int puzzleTolerancePx) {
        try {
            List<String> expected = objectMapper.readValue(correctAnswersJson, new TypeReference<>() {});
            if (userAnswers == null || userAnswers.size() != 1) {
                return false;
            }
            return expected.contains(userAnswers.get(0));
        } catch (Exception e) {
            return false;
        }
    }
}
