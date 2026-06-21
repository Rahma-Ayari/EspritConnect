package tn.esprit.espritconnect2.captcha;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tn.esprit.espritconnect2.DTO.captcha.CaptchaImageOptionDto;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Catalogue des ressources visuelles Esprit / leurres.
 * Étendre ce catalogue pour enrichir les défis sans toucher aux stratégies.
 */
@Component
@RequiredArgsConstructor
public class CaptchaAssetCatalog {

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    @Getter
    public static final class Asset {
        private final String id;
        private final String path;
        private final boolean espritRelated;
        private final String label;

        public Asset(String id, String path, boolean espritRelated, String label) {
            this.id = id;
            this.path = path;
            this.espritRelated = espritRelated;
            this.label = label;
        }
    }

    private static final List<Asset> ALL_ASSETS = List.of(
            // Images Esprit
            new Asset("esprit-logo", "/assets/captcha/universities/esprit-logo.webp", true, "Logo Esprit"),
            new Asset("esprit-campus", "/assets/captcha/universities/esprit-campus.webp", true, "Campus Esprit"),
            new Asset("esprit-event", "/assets/captcha/universities/esprit-event.webp", true, "Événement Esprit"),

            // Autres universités tunisiennes
            new Asset("isi-ariana-logo", "/assets/captcha/universities/isi-ariana-logo.webp", false, "Logo ISI Ariana"),
            new Asset("isi-ariana-campus", "/assets/captcha/universities/isi-ariana-campus.webp", false, "Campus ISI Ariana"),
            new Asset("insat-logo", "/assets/captcha/universities/insat-logo.webp", false, "Logo INSAT"),
            new Asset("insat-campus", "/assets/captcha/universities/insat-campus.webp", false, "Campus INSAT"),
            new Asset("islaib-logo", "/assets/captcha/universities/islaib-logo.webp", false, "Logo ISLAIB"),
            new Asset("islaib-campus", "/assets/captcha/universities/islaib-campus.webp", false, "Campus ISLAIB"),
            new Asset("iset-beja-logo", "/assets/captcha/universities/iset-beja-logo.webp", false, "Logo ISET Béja"),
            new Asset("iset-beja-campus", "/assets/captcha/universities/iset-beja-campus.webp", false, "Campus ISET Béja"),
            new Asset("universite-manar-campus", "/assets/captcha/universities/universite-manar-campus.webp", false, "Campus Université de La Manar")
    );

    public List<Asset> getEspritAssets() {
        return ALL_ASSETS.stream().filter(Asset::isEspritRelated).collect(Collectors.toList());
    }

    public List<Asset> getDecoyAssets() {
        return ALL_ASSETS.stream().filter(a -> !a.isEspritRelated()).collect(Collectors.toList());
    }

    public Asset getById(String id) {
        return ALL_ASSETS.stream()
                .filter(a -> a.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Asset inconnu: " + id));
    }

    public String resolveUrl(String path) {
        return frontendUrl + path;
    }

    public CaptchaImageOptionDto toImageOption(Asset asset) {
        return CaptchaImageOptionDto.builder()
                .id(asset.getId())
                .url(resolveUrl(asset.getPath()))
                .label(asset.getLabel())
                .build();
    }

    public String puzzleBackgroundUrl() {
        return resolveUrl("/assets/captcha/universities/esprit-logo.png");
    }

    public String puzzlePieceUrl() {
        return puzzleBackgroundUrl();
    }

    /** Questions à choix multiples — extensible via cette map. */
    public Map<String, QuestionTemplate> questionTemplates() {
        return Map.of(
                "logo-color", new QuestionTemplate(
                        "Quelle est la couleur principale du logo Esprit ?",
                        List.of(
                                option("red", "Rouge", null),
                                option("blue", "Bleu", null),
                                option("green", "Vert", null),
                                option("yellow", "Jaune", null)
                        ),
                        List.of("red")
                ),
                "logo-identify", new QuestionTemplate(
                        "Quel logo appartient à Esprit ?",
                        List.of(
                                option("esprit", "Logo Esprit", resolveUrl("/assets/captcha/universities/esprit-logo.webp")),
                                option("decoy1", "ISI Ariana", resolveUrl("/assets/captcha/universities/isi-ariana-logo.webp")),
                                option("decoy2", "INSAT", resolveUrl("/assets/captcha/universities/insat-logo.webp")),
                                option("decoy3", "ISLAIB", resolveUrl("/assets/captcha/universities/islaib-logo.webp"))
                        ),
                        List.of("esprit")
                ),
                "building", new QuestionTemplate(
                        "Quel bâtiment correspond au campus Esprit ?",
                        List.of(
                                option("esprit-campus", "Campus Esprit", resolveUrl("/assets/captcha/universities/esprit-campus.webp")),
                                option("decoy-building", "Campus ISLAIB", resolveUrl("/assets/captcha/universities/islaib-campus.webp")),
                                option("decoy-campus", "Campus ISET Béja", resolveUrl("/assets/captcha/universities/iset-beja-campus.webp")),
                                option("decoy-university", "Campus ISI Ariana", resolveUrl("/assets/captcha/universities/isi-ariana-campus.webp"))
                        ),
                        List.of("esprit-campus")
                ),
                "university-logo", new QuestionTemplate(
                        "Sélectionnez toutes les images qui contiennent le logo Esprit.",
                        List.of(
                                option("esprit-logo", "Logo Esprit", resolveUrl("/assets/captcha/universities/esprit-logo.webp")),
                                option("decoy1", "Logo INSAT", resolveUrl("/assets/captcha/universities/insat-logo.webp")),
                                option("decoy2", "Logo ISLAIB", resolveUrl("/assets/captcha/universities/islaib-logo.webp")),
                                option("decoy3", "Logo ISET Béja", resolveUrl("/assets/captcha/universities/iset-beja-logo.webp"))
                        ),
                        List.of("esprit-logo")
                ),
                "campus-choose", new QuestionTemplate(
                        "Quel campus appartient à Esprit ?",
                        List.of(
                                option("esprit-campus", "Campus Esprit", resolveUrl("/assets/captcha/universities/esprit-campus.webp")),
                                option("decoy-insat", "Campus INSAT", resolveUrl("/assets/captcha/universities/insat-campus.webp")),
                                option("decoy-islaib", "Campus ISLAIB", resolveUrl("/assets/captcha/universities/islaib-campus.webp")),
                                option("decoy-iset", "Campus ISET Béja", resolveUrl("/assets/captcha/universities/iset-beja-campus.webp"))
                        ),
                        List.of("esprit-campus")
                )
        );
    }

    private static CaptchaImageOptionDto option(String id, String label, String url) {
        return CaptchaImageOptionDto.builder().id(id).label(label).url(url).build();
    }

    public record QuestionTemplate(
            String question,
            List<CaptchaImageOptionDto> options,
            List<String> correctOptionIds
    ) {}
}
