package tn.esprit.espritconnect2.DTO.captcha;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaptchaPuzzleDataDto {
    private String backgroundUrl;
    private String pieceUrl;
    private int canvasWidth;
    private int canvasHeight;
    private int pieceWidth;
    private int pieceHeight;
    private int slotX;
    private int slotY;
    private int pieceStartX;
    private int pieceStartY;
}
