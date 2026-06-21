package tn.esprit.espritconnect2.captcha.strategy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tn.esprit.espritconnect2.DTO.captcha.CaptchaGenerateResponse;
import tn.esprit.espritconnect2.DTO.captcha.CaptchaImageOptionDto;
import tn.esprit.espritconnect2.DTO.captcha.CaptchaQuestionOptionDto;
import tn.esprit.espritconnect2.Entitie.CaptchaType;
import tn.esprit.espritconnect2.captcha.CaptchaAssetCatalog;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ImageCaptchaStrategy implements CaptchaChallengeStrategy {

    private final CaptchaAssetCatalog catalog;
    private final ObjectMapper objectMapper;

    @Override
    public CaptchaType getType() {
        return CaptchaType.IMAGE;
    }

    @Override
    public GeneratedChallengePayload generate() {
        List<CaptchaAssetCatalog.Asset> esprit = new ArrayList<>(catalog.getEspritAssets());
        List<CaptchaAssetCatalog.Asset> decoys = new ArrayList<>(catalog.getDecoyAssets());
        Collections.shuffle(esprit);
        Collections.shuffle(decoys);

        List<CaptchaAssetCatalog.Asset> selected = new ArrayList<>();
        selected.addAll(esprit.subList(0, Math.min(3, esprit.size())));
        selected.addAll(decoys.subList(0, Math.min(3, decoys.size())));
        Collections.shuffle(selected);

        List<CaptchaImageOptionDto> images = selected.stream()
                .map(catalog::toImageOption)
                .collect(Collectors.toList());

        List<String> correctIds = selected.stream()
                .filter(CaptchaAssetCatalog.Asset::isEspritRelated)
                .map(CaptchaAssetCatalog.Asset::getId)
                .sorted()
                .collect(Collectors.toList());

        try {
            String imagesJson = objectMapper.writeValueAsString(images);
            String correctJson = objectMapper.writeValueAsString(correctIds);

            CaptchaGenerateResponse client = CaptchaGenerateResponse.builder()
                    .captchaType(CaptchaType.IMAGE)
                    .question("Sélectionnez toutes les images représentant Esprit.")
                    .images(images)
                    .build();

            return GeneratedChallengePayload.builder()
                    .captchaType(CaptchaType.IMAGE)
                    .question(client.getQuestion())
                    .imagesJson(imagesJson)
                    .correctAnswersJson(correctJson)
                    .clientPayload(client)
                    .build();
        } catch (Exception e) {
            throw new IllegalStateException("Erreur génération captcha IMAGE", e);
        }
    }

    @Override
    public boolean verify(String correctAnswersJson, List<String> userAnswers, int puzzleTolerancePx) {
        try {
            List<String> expected = objectMapper.readValue(correctAnswersJson, new TypeReference<>() {});
            List<String> provided = userAnswers == null ? List.of() :
                    userAnswers.stream().filter(Objects::nonNull).sorted().collect(Collectors.toList());
            List<String> expectedSorted = expected.stream().sorted().collect(Collectors.toList());
            return expectedSorted.equals(provided);
        } catch (Exception e) {
            return false;
        }
    }
}
