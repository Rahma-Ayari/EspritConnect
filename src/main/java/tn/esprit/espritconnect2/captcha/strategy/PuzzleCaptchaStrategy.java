package tn.esprit.espritconnect2.captcha.strategy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tn.esprit.espritconnect2.DTO.captcha.CaptchaGenerateResponse;
import tn.esprit.espritconnect2.DTO.captcha.CaptchaPuzzleDataDto;
import tn.esprit.espritconnect2.Entitie.CaptchaType;
import tn.esprit.espritconnect2.captcha.CaptchaAssetCatalog;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class PuzzleCaptchaStrategy implements CaptchaChallengeStrategy {

    private static final int CANVAS_WIDTH = 300;
    private static final int CANVAS_HEIGHT = 200;
    private static final int PIECE_SIZE = 50;

    private final CaptchaAssetCatalog catalog;
    private final ObjectMapper objectMapper;

    @Override
    public CaptchaType getType() {
        return CaptchaType.PUZZLE;
    }

    @Override
    public GeneratedChallengePayload generate() {
        int targetX = ThreadLocalRandom.current().nextInt(60, CANVAS_WIDTH - PIECE_SIZE - 20);
        int targetY = ThreadLocalRandom.current().nextInt(30, CANVAS_HEIGHT - PIECE_SIZE - 20);
        int startX = 10;
        int startY = CANVAS_HEIGHT - PIECE_SIZE - 10;

        CaptchaPuzzleDataDto puzzleData = CaptchaPuzzleDataDto.builder()
                .backgroundUrl(catalog.puzzleBackgroundUrl())
                .pieceUrl(catalog.puzzlePieceUrl())
                .canvasWidth(CANVAS_WIDTH)
                .canvasHeight(CANVAS_HEIGHT)
                .pieceWidth(PIECE_SIZE)
                .pieceHeight(PIECE_SIZE)
                .slotX(targetX)
                .slotY(targetY)
                .pieceStartX(startX)
                .pieceStartY(startY)
                .build();

        try {
            String imagesJson = objectMapper.writeValueAsString(puzzleData);
            String correctJson = objectMapper.writeValueAsString(Map.of("x", targetX, "y", targetY));

            CaptchaGenerateResponse client = CaptchaGenerateResponse.builder()
                    .captchaType(CaptchaType.PUZZLE)
                    .question("Déplacez la pièce du puzzle au bon emplacement sur le logo Esprit.")
                    .puzzleData(puzzleData)
                    .build();

            return GeneratedChallengePayload.builder()
                    .captchaType(CaptchaType.PUZZLE)
                    .question(client.getQuestion())
                    .imagesJson(imagesJson)
                    .correctAnswersJson(correctJson)
                    .clientPayload(client)
                    .build();
        } catch (Exception e) {
            throw new IllegalStateException("Erreur génération captcha PUZZLE", e);
        }
    }

    @Override
    public boolean verify(String correctAnswersJson, List<String> userAnswers, int puzzleTolerancePx) {
        try {
            Map<String, Integer> expected = objectMapper.readValue(correctAnswersJson, new TypeReference<>() {});
            if (userAnswers == null || userAnswers.size() < 2) {
                return false;
            }
            int userX = Integer.parseInt(userAnswers.get(0));
            int userY = Integer.parseInt(userAnswers.get(1));
            int deltaX = Math.abs(expected.get("x") - userX);
            int deltaY = Math.abs(expected.get("y") - userY);
            return deltaX <= puzzleTolerancePx && deltaY <= puzzleTolerancePx;
        } catch (Exception e) {
            return false;
        }
    }
}
