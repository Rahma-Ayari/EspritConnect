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
public class QuestionCaptchaStrategy implements CaptchaChallengeStrategy {

    private final CaptchaAssetCatalog catalog;
    private final ObjectMapper objectMapper;

    @Override
    public CaptchaType getType() {
        return CaptchaType.QUESTION;
    }

    @Override
    public GeneratedChallengePayload generate() {
        Map<String, CaptchaAssetCatalog.QuestionTemplate> templates = catalog.questionTemplates();
        List<String> keys = new ArrayList<>(templates.keySet());
        String key = keys.get(new Random().nextInt(keys.size()));
        CaptchaAssetCatalog.QuestionTemplate template = templates.get(key);

        List<CaptchaQuestionOptionDto> options = template.options().stream()
                .map(o -> CaptchaQuestionOptionDto.builder()
                        .id(o.getId())
                        .text(o.getLabel())
                        .imageUrl(o.getUrl())
                        .build())
                .collect(Collectors.toList());
        Collections.shuffle(options);

        try {
            String imagesJson = objectMapper.writeValueAsString(options);
            String correctJson = objectMapper.writeValueAsString(template.correctOptionIds());

            CaptchaGenerateResponse client = CaptchaGenerateResponse.builder()
                    .captchaType(CaptchaType.QUESTION)
                    .question(template.question())
                    .options(options)
                    .build();

            return GeneratedChallengePayload.builder()
                    .captchaType(CaptchaType.QUESTION)
                    .question(template.question())
                    .imagesJson(imagesJson)
                    .correctAnswersJson(correctJson)
                    .clientPayload(client)
                    .build();
        } catch (Exception e) {
            throw new IllegalStateException("Erreur génération captcha QUESTION", e);
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
