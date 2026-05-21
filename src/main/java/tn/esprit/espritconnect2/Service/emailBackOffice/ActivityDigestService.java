package tn.esprit.espritconnect2.Service.emailBackOffice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tn.esprit.espritconnect2.Entitie.*;
import tn.esprit.espritconnect2.Entitie.emailBackOffice.DigestConfig;
import tn.esprit.espritconnect2.Repository.*;

import jakarta.mail.internet.MimeMessage;
import tn.esprit.espritconnect2.Repository.emailBackOffice.DigestConfigRepository;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * Service qui envoie automatiquement le digest hebdomadaire ou quotidien.
 *
 * @Scheduled = Spring Boot se réveille automatiquement selon le calendrier défini
 * @Slf4j     = pour écrire des logs dans la console (utile pour déboguer)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityDigestService {

    // Tous les repositories nécessaires pour récupérer les données
    private final OffreRepository offreRepository;
    private final EvenementRepository evenementRepository;
    private final EtudiantRepository etudiantRepository;
    private final DigestConfigRepository digestConfigRepository;
    private final JavaMailSender mailSender;

    // ─── Tâche planifiée : s'exécute chaque lundi à 8h00 ─────────────────
    /**
     * cron = "0 0 8 * * MON"  signifie :
     *   - 0    secondes
     *   - 0    minutes
     *   - 8    heures (8h00 du matin)
     *   - *    n'importe quel jour du mois
     *   - *    n'importe quel mois
     *   - MON  seulement le lundi
     *
     * Pour tester en développement, remplace par :
     * fixedDelay = 60000  (s'exécute toutes les 60 secondes)
     */
    @Scheduled(cron = "0 0 8 * * MON")
    public void envoyerDigestHebdomadaire() {

        // 1. Lire la configuration (est-ce que le digest est actif ?)
        DigestConfig config = digestConfigRepository
                .findById(1L)
                .orElse(null);

        // Si pas de config ou digest désactivé → on ne fait rien
        if (config == null || !Boolean.TRUE.equals(config.getActif())) {
            log.info("Digest désactivé ou non configuré. Envoi annulé.");
            return;
        }

        // Vérifier la fréquence : si c'est DAILY, ce scheduler ne s'occupe
        // pas de lui (c'est l'autre scheduler plus bas qui gère)
        if ("DAILY".equals(config.getFrequence())) {
            log.info("Fréquence DAILY : ce scheduler ne s'exécute pas.");
            return;
        }

        // 2. Calculer la date d'il y a 7 jours
        Date dateDebut = Date.from(
                LocalDate.now()
                        .minusDays(7)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
        );

        // 3. Compter les nouvelles offres et événements de la semaine
        //    On récupère tout et on filtre (simple pour débutant)
        long nbNouvellesOffres = offreRepository.findAll()
                .stream()
                .filter(o -> o.getDatePublication() != null
                        && o.getDatePublication().after(dateDebut))
                .count();

        long nbNouveauxEvenements = evenementRepository.findAll()
                .stream()
                .filter(e -> e.getDateEvenement() != null
                        && e.getDateEvenement().after(dateDebut))
                .count();

        // 4. Récupérer tous les étudiants (les destinataires du digest)
        List<Etudiant> etudiants = etudiantRepository.findAll();

        if (etudiants.isEmpty()) {
            log.info("Aucun étudiant trouvé. Envoi annulé.");
            return;
        }

        // 5. Envoyer un email à chaque étudiant
        log.info("Envoi du digest hebdomadaire à {} étudiants...",
                etudiants.size());

        for (Etudiant etudiant : etudiants) {
            try {
                envoyerEmailDigest(
                        etudiant.getEmail(),
                        etudiant.getNom(),
                        config.getSujet(),
                        config.getBannerUrl(),
                        nbNouvellesOffres,
                        nbNouveauxEvenements,
                        "semaine"
                );
            } catch (Exception e) {
                // Si un email échoue, on continue avec le suivant
                log.error("Erreur envoi email à {} : {}",
                        etudiant.getEmail(), e.getMessage());
            }
        }

        log.info("Digest hebdomadaire envoyé avec succès !");
    }

    // ─── Tâche planifiée : s'exécute chaque jour à 8h00 ──────────────────
    /**
     * cron = "0 0 8 * * *"  signifie chaque jour à 8h00.
     * Ce scheduler s'exécute seulement si frequence = "DAILY".
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void envoyerDigestQuotidien() {

        DigestConfig config = digestConfigRepository
                .findById(1L)
                .orElse(null);

        if (config == null
                || !Boolean.TRUE.equals(config.getActif())
                || !"DAILY".equals(config.getFrequence())) {
            return; // Ce n'est pas le bon mode, on ne fait rien
        }

        // Calculer la date d'hier
        Date dateDebut = Date.from(
                LocalDate.now()
                        .minusDays(1)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
        );

        long nbOffres = offreRepository.findAll()
                .stream()
                .filter(o -> o.getDatePublication() != null
                        && o.getDatePublication().after(dateDebut))
                .count();

        long nbEvenements = evenementRepository.findAll()
                .stream()
                .filter(e -> e.getDateEvenement() != null
                        && e.getDateEvenement().after(dateDebut))
                .count();

        List<Etudiant> etudiants = etudiantRepository.findAll();

        for (Etudiant etudiant : etudiants) {
            try {
                envoyerEmailDigest(
                        etudiant.getEmail(),
                        etudiant.getNom(),
                        config.getSujet(),
                        config.getBannerUrl(),
                        nbOffres,
                        nbEvenements,
                        "jour"
                );
            } catch (Exception e) {
                log.error("Erreur digest quotidien pour {} : {}",
                        etudiant.getEmail(), e.getMessage());
            }
        }
    }

    // ─── Construction et envoi de l'email HTML ────────────────────────────
    /**
     * Construit l'email HTML et l'envoie via JavaMailSender.
     * L'email contient :
     *  - Une bannière
     *  - Le nombre de nouvelles offres
     *  - Le nombre de nouveaux événements
     *  - Des boutons liens vers le site
     */
    private void envoyerEmailDigest(
            String emailDestinataire,
            String nomDestinataire,
            String sujet,
            String bannerUrl,
            long nbOffres,
            long nbEvenements,
            String periode) throws Exception {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(
                message, true, "UTF-8"
        );

        helper.setTo(emailDestinataire);
        helper.setSubject(sujet != null ? sujet : "What's new on Esprit");
        helper.setFrom("noreply@esprit.tn");

        // Construction du contenu HTML de l'email
        String htmlContent = buildEmailHtml(
                nomDestinataire,
                bannerUrl,
                nbOffres,
                nbEvenements,
                periode
        );

        // true = c'est du HTML (pas du texte brut)
        helper.setText(htmlContent, true);

        mailSender.send(message);
        log.info("Email digest envoyé à {}", emailDestinataire);
    }

    // ─── Template HTML de l'email ─────────────────────────────────────────
    /**
     * Génère le HTML de l'email.
     * Le résultat ressemble à une vraie newsletter professionnelle.
     */
    private String buildEmailHtml(
            String nom,
            String bannerUrl,
            long nbOffres,
            long nbEvenements,
            String periode) {

        // URL de base de ton site Angular (à adapter si tu changes de port)
        String baseUrl = "http://localhost:4200";

        // Bannière : si une URL est configurée, on l'affiche ; sinon un fond coloré
        String bannerHtml = (bannerUrl != null && !bannerUrl.isBlank())
                ? "<img src='" + bannerUrl
                + "' style='width:100%;max-height:182px;"
                + "object-fit:cover;' alt='Esprit Banner'/>"
                : "<div style='background:#cc0000;height:80px;"
                + "display:flex;align-items:center;justify-content:center;'>"
                + "<span style='color:white;font-size:24px;"
                + "font-weight:bold;'>EspritConnect</span></div>";

        return """
            <!DOCTYPE html>
            <html lang="fr">
            <head>
              <meta charset="UTF-8"/>
              <style>
                body { font-family: Arial, sans-serif;
                       background:#f5f5f5; margin:0; padding:0; }
                .container { max-width:600px; margin:20px auto;
                             background:white; border-radius:8px;
                             overflow:hidden;
                             box-shadow:0 2px 8px rgba(0,0,0,0.1); }
                .header { background:#cc0000; color:white;
                          padding:20px; text-align:center; }
                .content { padding:30px; }
                .stat-box { background:#f9f9f9; border-left:4px solid #cc0000;
                            padding:15px; margin:15px 0;
                            border-radius:4px; }
                .stat-number { font-size:32px; font-weight:bold;
                               color:#cc0000; }
                .btn { display:inline-block; background:#cc0000;
                       color:white; padding:12px 24px;
                       text-decoration:none; border-radius:6px;
                       margin:8px 4px; font-weight:bold; }
                .footer { background:#333; color:#aaa;
                          padding:20px; text-align:center;
                          font-size:12px; }
              </style>
            </head>
            <body>
              <div class="container">

                <!-- Bannière -->
                %s

                <!-- Contenu principal -->
                <div class="content">
                  <h2 style="color:#333;">
                    Bonjour %s 👋
                  </h2>
                  <p style="color:#666;">
                    Voici ce qui s'est passé sur EspritConnect
                    cette %s :
                  </p>

                  <!-- Statistique Offres -->
                  <div class="stat-box">
                    <div class="stat-number">%d</div>
                    <div style="color:#666;">
                      nouvelle(s) offre(s) de stage / emploi
                    </div>
                    <a href="%s/offres" class="btn">
                      Voir les offres →
                    </a>
                  </div>

                  <!-- Statistique Événements -->
                  <div class="stat-box">
                    <div class="stat-number">%d</div>
                    <div style="color:#666;">
                      nouvel/nouveaux événement(s)
                    </div>
                    <a href="%s/evenements" class="btn">
                      Voir les événements →
                    </a>
                  </div>

                  <p style="color:#666;font-size:13px;margin-top:20px;">
                    Connecte-toi pour ne rien manquer !
                  </p>
                  <a href="%s" class="btn">
                    Accéder à EspritConnect
                  </a>
                </div>

                <!-- Pied de page -->
                <div class="footer">
                  <p>Tu reçois cet email car tu es inscrit(e)
                     sur EspritConnect.</p>
                  <p>© 2024 Esprit — Honoris United Universities</p>
                </div>

              </div>
            </body>
            </html>
            """.formatted(
                bannerHtml,
                nom,
                periode,
                nbOffres,
                baseUrl,
                nbEvenements,
                baseUrl,
                baseUrl
        );
    }
}