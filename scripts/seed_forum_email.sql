-- ============================================================
-- SCRIPT DE SEED - Forum & Email Communications
-- Base de données : EspritConnect2
-- Compatible avec les entités JPA du projet
-- ============================================================
-- Usage : mysql -u root EspritConnect2 < seed_forum_email.sql
-- Ou exécutez dans MySQL Workbench
-- ============================================================

USE EspritConnect2;

-- ============================================================
-- 1. FORUM - CATÉGORIES (table: forum_category)
-- Colonnes : id, name, description, icon, color, created_at
-- ============================================================
INSERT INTO forum_category (name, description, icon, color, created_at) VALUES
('Informatique & Dev',     'Discussions autour du développement logiciel, des langages de programmation et des frameworks.', 'code',    'blue',   NOW() - INTERVAL 30 DAY),
('Vie Étudiante',          'Partage d''expériences, conseils et bons plans pour la vie sur le campus Esprit.',                'school',  'green',  NOW() - INTERVAL 30 DAY),
('Carrière & Emploi',      'Offres de stage, conseils CV, entretiens et opportunités professionnelles.',                      'work',    'purple', NOW() - INTERVAL 30 DAY),
('Projets & Collaboration','Rejoignez ou proposez des projets collaboratifs entre étudiants et alumni.',                     'group',   'amber',  NOW() - INTERVAL 29 DAY),
('Annonces & Événements',  'Calendrier des événements Esprit, hackathons, conférences et workshops.',                        'calendar','red',    NOW() - INTERVAL 29 DAY),
('Aide & Support',         'Posez vos questions sur les cours, examens et la plateforme EspritConnect.',                     'help',    'teal',   NOW() - INTERVAL 28 DAY)
ON DUPLICATE KEY UPDATE description = VALUES(description);


-- ============================================================
-- 2. FORUM - POSTS (table: forum_post)
-- Colonnes : id, title, content, category_id, author_name,
--            author_email, author_role, created_at, pinned,
--            reported, report_reason, views_count, likes_count, group_id
-- author_role ENUM : STUDENT, ADMIN, ALUMNI, ENTREPRISE
-- ============================================================

-- Récupération des IDs de catégories générés
SET @cat_dev    = (SELECT id FROM forum_category WHERE name = 'Informatique & Dev' LIMIT 1);
SET @cat_vie    = (SELECT id FROM forum_category WHERE name = 'Vie Étudiante' LIMIT 1);
SET @cat_job    = (SELECT id FROM forum_category WHERE name = 'Carrière & Emploi' LIMIT 1);
SET @cat_proj   = (SELECT id FROM forum_category WHERE name = 'Projets & Collaboration' LIMIT 1);
SET @cat_event  = (SELECT id FROM forum_category WHERE name = 'Annonces & Événements' LIMIT 1);
SET @cat_help   = (SELECT id FROM forum_category WHERE name = 'Aide & Support' LIMIT 1);

INSERT INTO forum_post (title, content, category_id, author_name, author_email, author_role, created_at, pinned, reported, report_reason, views_count, likes_count)
VALUES
-- --- Informatique & Dev ---
(
  'Quel framework Angular ou React pour un projet PI ?',
  '<p>Bonjour à tous ! Dans le cadre de notre projet intégré cette année, on hésite entre <strong>Angular 18</strong> et <strong>React 18</strong>. Notre équipe a 5 développeurs dont 3 avec une expérience Angular de base.</p><p>Quelqu''un a-t-il déjà fait les deux ? Quels sont les avantages/inconvénients selon vous ?</p>',
  @cat_dev, 'Safa Ben Nasr', 'safa.bennasr@esprit.tn', 'STUDENT',
  NOW() - INTERVAL 5 DAY, 1, 0, NULL, 142, 23
),
(
  'Résoudre les erreurs CORS avec Spring Boot',
  '<p>Je rencontre des erreurs <code>CORS</code> entre mon frontend Angular (port 4200) et mon backend Spring Boot (port 8089).</p><p><strong>Solution :</strong> Configurez un bean <code>WebMvcConfigurer</code> global pour gérer CORS proprement :</p><pre><code>@Bean\npublic WebMvcConfigurer corsConfigurer() {\n  return new WebMvcConfigurer() {\n    @Override\n    public void addCorsMappings(CorsRegistry r) {\n      r.addMapping("/api/**").allowedOrigins("http://localhost:4200");\n    }\n  };\n}</code></pre>',
  @cat_dev, 'Rahma Ayari', 'rahma.ayari@esprit.tn', 'STUDENT',
  NOW() - INTERVAL 3 DAY, 0, 0, NULL, 87, 15
),
(
  'Tutoriel : Déployer Spring Boot sur Railway gratuitement',
  '<p>Voici un guide pas-à-pas pour déployer votre application Spring Boot sur <strong>Railway.app</strong> sans frais :</p><ol><li>Créer un compte Railway</li><li>Connecter votre dépôt GitHub</li><li>Configurer les variables d''environnement (DB_URL, etc.)</li><li>Déployer en un clic</li></ol><p>N''hésitez pas à poser vos questions ci-dessous !</p>',
  @cat_dev, 'Ahmed Triki', 'ahmed.triki@esprit.tn', 'STUDENT',
  NOW() - INTERVAL 1 DAY, 0, 0, NULL, 65, 8
),
(
  'Problème : NullPointerException en Spring Boot - aide SVP',
  '<p>J''ai une <code>NullPointerException</code> dans mon service mais je ne comprends pas pourquoi. Voici le stack trace :</p><pre><code>java.lang.NullPointerException: Cannot invoke method findById()\n  at tn.esprit.MyService.getUser(MyService.java:42)</code></pre><p>Mon repo est bien annoté <code>@Autowired</code>. Quelqu''un voit le problème ?</p>',
  @cat_dev, 'Bilel Chaabane', 'bilel.chaabane@esprit.tn', 'STUDENT',
  NOW() - INTERVAL 12 HOUR, 0, 0, NULL, 18, 2
),

-- --- Vie Étudiante ---
(
  'Meilleures ressources pour apprendre Git & GitHub',
  '<p>Pour tous les débutants qui découvrent <strong>Git</strong>, voici mes ressources préférées :</p><ul><li><a href="https://learngitbranching.js.org">Learn Git Branching</a> (interactif)</li><li>La documentation officielle GitHub</li><li>La chaîne YouTube "The Net Ninja" (Git series)</li></ul><p>Partagez les vôtres en commentaire !</p>',
  @cat_vie, 'Mohamed Slim', 'mohamed.slim@esprit.tn', 'STUDENT',
  NOW() - INTERVAL 7 DAY, 0, 0, NULL, 201, 34
),
(
  'Retour d''expérience : Stage chez une startup tech tunisienne',
  '<p>Je viens de terminer mon stage de 2 mois chez <strong>DigitalTN</strong> à Lac 2. Voici mes impressions :</p><p>✅ Points positifs : ambiance startup, responsabilités réelles, stack moderne (React + Node.js).</p><p>⚠️ Points à améliorer : peu de documentation, longues heures.</p><p>Au global, une expérience très enrichissante ! Je vous recommande de chercher des startups pour votre prochain stage.</p>',
  @cat_vie, 'Nour Belhaj', 'nour.belhaj@esprit.tn', 'STUDENT',
  NOW() - INTERVAL 4 DAY, 0, 0, NULL, 178, 29
),

-- --- Carrière & Emploi ---
(
  '[Offre Stage] Développeur Full-Stack Angular/Spring Boot - 2 mois',
  '<p>Notre startup <strong>TechInnov</strong> recherche un stagiaire développeur full-stack pour juillet–août 2025.</p><p><strong>Profil :</strong> 3ème ou 4ème année, maîtrise Angular + Spring Boot, bases en DevOps.</p><p>📩 Envoyez votre CV à : rh@techinnov.tn</p>',
  @cat_job, 'Admin Esprit', 'admin@esprit.tn', 'ADMIN',
  NOW() - INTERVAL 2 DAY, 1, 0, NULL, 95, 12
),
(
  'Comment préparer son entretien technique en 30 jours ?',
  '<p>Voici le plan que j''ai suivi pour décrocher mon poste chez une SSII internationale :</p><ol><li><strong>Semaine 1-2 :</strong> Algorithmes et structures de données (LeetCode)</li><li><strong>Semaine 3 :</strong> Design patterns (SOLID, MVC, Microservices)</li><li><strong>Semaine 4 :</strong> Simulations d''entretiens avec des amis</li></ol><p>Courage à tous ! 💪</p>',
  @cat_job, 'Oussema Gharbi', 'oussema.gharbi@esprit.tn', 'STUDENT',
  NOW() - INTERVAL 6 DAY, 0, 0, NULL, 234, 41
),

-- --- Projets & Collaboration ---
(
  'Recherche coéquipiers pour hackathon StartupWeekend Tunis',
  '<p>Salut ! Je cherche 2-3 personnes motivées pour participer au <strong>Startup Weekend Tunis</strong> le mois prochain.</p><p>Idéalement : 1 dev frontend, 1 dev backend, et 1 profil business/design.</p><p>Contactez-moi en commentaire si vous êtes intéressés !</p>',
  @cat_proj, 'Ines Maaref', 'ines.maaref@esprit.tn', 'STUDENT',
  NOW() - INTERVAL 1 DAY, 0, 0, NULL, 43, 7
),

-- --- Annonces & Événements ---
(
  '[Événement] Journée Portes Ouvertes Esprit - 15 Juin 2025',
  '<p>L''administration Esprit organise sa <strong>Journée Portes Ouvertes</strong> le <strong>15 juin 2025</strong>.</p><p>Au programme :</p><ul><li>Présentation des filières</li><li>Démos de projets étudiants</li><li>Rencontres avec les partenaires industriels</li></ul><p>Bénévoles recherchés ! Contactez le bureau des étudiants.</p>',
  @cat_event, 'Admin Esprit', 'admin@esprit.tn', 'ADMIN',
  NOW() - INTERVAL 8 DAY, 1, 0, NULL, 312, 56
),

-- --- Aide & Support ---
(
  'Comment accéder au VPN Esprit depuis chez soi ?',
  '<p>Bonjour, j''essaie d''accéder aux ressources de la bibliothèque numérique depuis chez moi mais le lien ne fonctionne pas. Quelqu''un sait-il comment configurer le VPN Esprit ?</p>',
  @cat_help, 'Bilel Chaabane', 'bilel.chaabane@esprit.tn', 'STUDENT',
  NOW() - INTERVAL 2 DAY, 0, 0, NULL, 28, 3
);


-- ============================================================
-- 3. FORUM - RÉPONSES (table: forum_reply)
-- Colonnes : id, post_id, author_name, author_email, author_role,
--            content, created_at, reported, report_reason
-- ============================================================

-- Récupération des IDs des posts générés (par titre unique)
SET @post_angular = (SELECT id FROM forum_post WHERE title LIKE '%Angular ou React%' LIMIT 1);
SET @post_cors    = (SELECT id FROM forum_post WHERE title LIKE '%CORS%' LIMIT 1);
SET @post_git     = (SELECT id FROM forum_post WHERE title LIKE '%Git%GitHub%' LIMIT 1);
SET @post_stage   = (SELECT id FROM forum_post WHERE title LIKE '%Stage%startup%' LIMIT 1);
SET @post_entret  = (SELECT id FROM forum_post WHERE title LIKE '%entretien technique%' LIMIT 1);
SET @post_event   = (SELECT id FROM forum_post WHERE title LIKE '%Portes Ouvertes%' LIMIT 1);
SET @post_vpn     = (SELECT id FROM forum_post WHERE title LIKE '%VPN%' LIMIT 1);

INSERT INTO forum_reply (post_id, author_name, author_email, author_role, content, created_at, reported, report_reason)
VALUES
-- Réponses au post Angular vs React
(@post_angular, 'Rahma Ayari', 'rahma.ayari@esprit.tn', 'STUDENT',
 '<p>Perso je recommande Angular pour un projet PI car il impose une structure stricte qui facilite la collaboration en équipe. React est plus flexible mais peut devenir difficile à maintenir sans bonne architecture.</p>',
 NOW() - INTERVAL 4 DAY, 0, NULL),
(@post_angular, 'Ahmed Triki', 'ahmed.triki@esprit.tn', 'STUDENT',
 '<p>+1 pour Angular ! De plus, l''intégration avec Spring Boot est très documentée. Pleins de tutos sur YouTube pour le combo Angular + Spring Boot.</p>',
 NOW() - INTERVAL 4 DAY, 0, NULL),
(@post_angular, 'Admin Esprit', 'admin@esprit.tn', 'ADMIN',
 '<p>Pour un projet académique en équipe, Angular est préféré car son architecture imposée (modules, composants, services) facilite la notation et le suivi.</p>',
 NOW() - INTERVAL 3 DAY, 0, NULL),

-- Réponses au post CORS
(@post_cors, 'Mohamed Slim', 'mohamed.slim@esprit.tn', 'STUDENT',
 '<p>Merci ! Ça m''a sauvé. J''avais aussi des problèmes avec les credentials, il faut ajouter <code>.allowCredentials(true)</code> si vous utilisez des cookies de session.</p>',
 NOW() - INTERVAL 2 DAY, 0, NULL),
(@post_cors, 'Nour Belhaj', 'nour.belhaj@esprit.tn', 'STUDENT',
 '<p>Attention : si vous mettez <code>allowedOrigins("*")</code> avec <code>allowCredentials(true)</code>, Spring Boot va rejeter la config. Il faut spécifier l''origine exacte dans ce cas.</p>',
 NOW() - INTERVAL 1 DAY, 0, NULL),

-- Réponses au post Git
(@post_git, 'Safa Ben Nasr', 'safa.bennasr@esprit.tn', 'STUDENT',
 '<p>J''ajoute "Oh My Git!" qui est un jeu pour apprendre Git de manière très visuelle. Parfait pour les débutants complets !</p>',
 NOW() - INTERVAL 6 DAY, 0, NULL),
(@post_git, 'Nour Belhaj', 'nour.belhaj@esprit.tn', 'STUDENT',
 '<p>La playlist Git de "Grafikart.fr" en français est aussi très bien faite et complète. Je la recommande vivement !</p>',
 NOW() - INTERVAL 5 DAY, 0, NULL),

-- Réponses au post Retour Stage
(@post_stage, 'Oussema Gharbi', 'oussema.gharbi@esprit.tn', 'STUDENT',
 '<p>Merci pour ce retour ! Est-ce qu''ils recrutent encore pour l''été prochain ? Leur stack React + Node.js est exactement ce que je cherche.</p>',
 NOW() - INTERVAL 3 DAY, 0, NULL),

-- Réponses au post Entretien
(@post_entret, 'Ines Maaref', 'ines.maaref@esprit.tn', 'STUDENT',
 '<p>Super article ! J''ajouterais aussi de travailler sur les questions comportementales (STAR method) car les recruteurs internationaux y accordent beaucoup d''importance.</p>',
 NOW() - INTERVAL 5 DAY, 0, NULL),
(@post_entret, 'Bilel Chaabane', 'bilel.chaabane@esprit.tn', 'STUDENT',
 '<p>Est-ce que tu peux partager les ressources LeetCode que tu as utilisées ? Notamment pour les problèmes de graphes ?</p>',
 NOW() - INTERVAL 4 DAY, 0, NULL),
(@post_entret, 'Oussema Gharbi', 'oussema.gharbi@esprit.tn', 'STUDENT',
 '<p>@Bilel : Bien sûr ! Je t''enverrai un lien vers ma liste Leetcode en MP. Elle comporte 75 questions soigneusement sélectionnées.</p>',
 NOW() - INTERVAL 3 DAY, 0, NULL),

-- Réponses au post Événement
(@post_event, 'Mohamed Slim', 'mohamed.slim@esprit.tn', 'STUDENT',
 '<p>Je suis partant pour être bénévole ! Est-ce qu''il faut s''inscrire quelque part ou contacter directement le bureau ?</p>',
 NOW() - INTERVAL 7 DAY, 0, NULL),
(@post_event, 'Admin Esprit', 'admin@esprit.tn', 'ADMIN',
 '<p>Vous pouvez vous inscrire via le bureau des étudiants, salle B-204. Les places sont limitées à 20 bénévoles. Démarche rapide !</p>',
 NOW() - INTERVAL 7 DAY, 0, NULL),

-- Réponse au post VPN
(@post_vpn, 'Admin Esprit', 'admin@esprit.tn', 'ADMIN',
 '<p>Bonjour ! Voici les étapes :<br/>1. Téléchargez FortiClient VPN (pas OpenVPN)<br/>2. Serveur : <code>vpn.esprit.tn</code>, Port : <code>443</code><br/>3. Utilisez vos identifiants Esprit habituels<br/>Si le problème persiste, contactez : <strong>it-support@esprit.tn</strong></p>',
 NOW() - INTERVAL 1 DAY, 0, NULL);


-- ============================================================
-- 4. MAILING LISTS (table: mailing_list)
-- Colonnes : id, name, description, created_at
-- ============================================================
INSERT INTO mailing_list (name, description, created_at) VALUES
('Étudiants 4ème Année',   'Tous les étudiants en dernière année, toutes filières confondues.',                 NOW() - INTERVAL 30 DAY),
('Alumni EspritConnect',   'Anciens étudiants inscrits sur la plateforme EspritConnect.',                        NOW() - INTERVAL 20 DAY),
('Club Informatique',      'Membres du club informatique Esprit - actualités, events et workshops.',            NOW() - INTERVAL 15 DAY),
('Partenaires Entreprise', 'Recruteurs et entreprises partenaires ayant un compte sur EspritConnect.',          NOW() - INTERVAL 10 DAY),
('Newsletter Générale',    'Toute la communauté EspritConnect - annonces générales et événements importants.',  NOW() - INTERVAL 5 DAY);


-- ============================================================
-- 5. MAILING LIST MEMBERS (table: mailing_list_member)
-- Colonnes : id, mailing_list_id, email, nom, user_uuid
-- ============================================================
SET @ml1 = (SELECT id FROM mailing_list WHERE name = 'Étudiants 4ème Année' LIMIT 1);
SET @ml2 = (SELECT id FROM mailing_list WHERE name = 'Alumni EspritConnect' LIMIT 1);
SET @ml3 = (SELECT id FROM mailing_list WHERE name = 'Club Informatique' LIMIT 1);
SET @ml4 = (SELECT id FROM mailing_list WHERE name = 'Partenaires Entreprise' LIMIT 1);
SET @ml5 = (SELECT id FROM mailing_list WHERE name = 'Newsletter Générale' LIMIT 1);

INSERT INTO mailing_list_member (mailing_list_id, email, nom) VALUES
-- Étudiants 4ème Année
(@ml1, 'safa.bennasr@esprit.tn',   'Safa Ben Nasr'),
(@ml1, 'rahma.ayari@esprit.tn',    'Rahma Ayari'),
(@ml1, 'ahmed.triki@esprit.tn',    'Ahmed Triki'),
(@ml1, 'mohamed.slim@esprit.tn',   'Mohamed Slim'),
(@ml1, 'nour.belhaj@esprit.tn',    'Nour Belhaj'),
(@ml1, 'oussema.gharbi@esprit.tn', 'Oussema Gharbi'),
(@ml1, 'ines.maaref@esprit.tn',    'Ines Maaref'),
(@ml1, 'bilel.chaabane@esprit.tn', 'Bilel Chaabane'),
-- Alumni
(@ml2, 'karim.oueslati@gmail.com', 'Karim Oueslati'),
(@ml2, 'hela.jendoubi@outlook.com','Hela Jendoubi'),
(@ml2, 'ali.gharbi@yahoo.fr',      'Ali Gharbi'),
(@ml2, 'fatma.karray@gmail.com',   'Fatma Karray'),
-- Club Informatique
(@ml3, 'safa.bennasr@esprit.tn',   'Safa Ben Nasr'),
(@ml3, 'ahmed.triki@esprit.tn',    'Ahmed Triki'),
(@ml3, 'bilel.chaabane@esprit.tn', 'Bilel Chaabane'),
(@ml3, 'rami.benali@esprit.tn',    'Rami Ben Ali'),
-- Partenaires
(@ml4, 'rh@techinnov.tn',          'TechInnov RH'),
(@ml4, 'recrutement@digitalty.com','DigitalTN'),
(@ml4, 'hr@startuptn.com',         'StartupTN HR'),
-- Newsletter Générale
(@ml5, 'safa.bennasr@esprit.tn',   'Safa Ben Nasr'),
(@ml5, 'rahma.ayari@esprit.tn',    'Rahma Ayari'),
(@ml5, 'karim.oueslati@gmail.com', 'Karim Oueslati'),
(@ml5, 'admin@esprit.tn',          'Admin Esprit');


-- ============================================================
-- 6. EMAIL CAMPAIGNS (table: email_campaign)
-- Colonnes : id, subject, html_body, from_email, status,
--            recipient_scope, mailing_list_id, created_at, updated_at, sent_at
-- status : DRAFT, SENT, FAILED
-- recipient_scope : ALL_ENABLED_USERS, ALL_STUDENTS, MAILING_LIST
-- ============================================================
SET @ml3_id = (SELECT id FROM mailing_list WHERE name = 'Club Informatique' LIMIT 1);
SET @ml5_id = (SELECT id FROM mailing_list WHERE name = 'Newsletter Générale' LIMIT 1);

INSERT INTO email_campaign (subject, html_body, from_email, status, recipient_scope, mailing_list_id, created_at, updated_at, sent_at)
VALUES
-- Campagne 1 : Bienvenue (envoyée)
(
  'Bienvenue sur EspritConnect - Votre plateforme étudiante',
  '<html><body style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;background:#f5f5f5;padding:20px"><div style="background:white;border-radius:12px;padding:30px;box-shadow:0 2px 8px rgba(0,0,0,0.1)"><h1 style="color:#667eea;margin-bottom:16px">🎓 Bienvenue sur EspritConnect !</h1><p style="color:#555;line-height:1.6">Nous sommes ravis de vous accueillir sur <strong>EspritConnect</strong>, la plateforme dédiée aux étudiants et alumni d''Esprit.</p><ul style="color:#555;line-height:1.8"><li>Accédez aux offres de stage et d''emploi</li><li>Rejoignez des forums de discussion</li><li>Suivez les événements campus</li><li>Connectez avec des alumni</li></ul><div style="text-align:center;margin:30px 0"><a href="http://localhost:4200" style="background:linear-gradient(135deg,#667eea,#764ba2);color:white;text-decoration:none;padding:14px 32px;border-radius:8px;font-weight:bold;display:inline-block">Découvrir la plateforme</a></div></div></body></html>',
  'admin@esprit.tn', 'SENT', 'ALL_ENABLED_USERS', NULL,
  NOW() - INTERVAL 25 DAY, NOW() - INTERVAL 25 DAY, NOW() - INTERVAL 25 DAY
),
-- Campagne 2 : Portes Ouvertes (envoyée)
(
  '📢 Rappel : Journée Portes Ouvertes Esprit - 15 Juin 2025',
  '<html><body style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;background:#f5f5f5;padding:20px"><div style="background:white;border-radius:12px;padding:30px"><div style="background:linear-gradient(135deg,#f093fb,#f5576c);border-radius:8px;padding:20px;text-align:center"><h1 style="color:white;margin:0">📅 Journée Portes Ouvertes</h1><p style="color:rgba(255,255,255,0.9)">15 Juin 2025 — Campus Esprit, Lac 2</p></div><p style="color:#555;margin-top:20px">Ne manquez pas cet événement ! Au programme : présentation des filières, démos de projets, rencontres recruteurs.</p></div></body></html>',
  'admin@esprit.tn', 'SENT', 'ALL_ENABLED_USERS', NULL,
  NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 10 DAY
),
-- Campagne 3 : Opportunités stage Club Info (envoyée, ciblée)
(
  '🚀 Opportunités de stage été 2025 - Club Informatique',
  '<html><body style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;background:#f5f5f5;padding:20px"><div style="background:white;border-radius:12px;padding:30px"><h1 style="color:#667eea">Nouvelles opportunités de stage</h1><p style="color:#555">Bonjour membres du Club Informatique,</p><div style="background:#f8f9fa;border-radius:8px;padding:16px;margin:12px 0"><strong>TechInnov</strong> — Développeur Full-Stack<br/><em>2 mois | Juillet 2025</em></div><div style="background:#f8f9fa;border-radius:8px;padding:16px;margin:12px 0"><strong>DigitalTN</strong> — Ingénieur DevOps Junior<br/><em>3 mois | Juin 2025</em></div><div style="text-align:center;margin:24px 0"><a href="http://localhost:4200/dashboard/jobs" style="background:#667eea;color:white;text-decoration:none;padding:12px 28px;border-radius:8px;display:inline-block">Voir toutes les offres</a></div></div></body></html>',
  'admin@esprit.tn', 'SENT', 'MAILING_LIST', @ml3_id,
  NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY
),
-- Campagne 4 : Newsletter (brouillon)
(
  '📝 Newsletter EspritConnect — Juin 2025',
  '<html><body style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;background:#f5f5f5;padding:20px"><div style="background:white;border-radius:12px;padding:30px"><div style="text-align:center;margin-bottom:24px"><h1 style="color:#667eea;font-size:32px;margin:0">EspritConnect</h1><p style="color:#888;margin:4px 0">Newsletter — Juin 2025</p></div><h2 style="color:#333;border-bottom:2px solid #667eea;padding-bottom:8px">🆕 Nouveautés</h2><p style="color:#555">Le module <strong>Forum Communautaire</strong> est maintenant disponible ! Rejoignez les discussions et connectez-vous avec toute la communauté Esprit.</p></div></body></html>',
  'admin@esprit.tn', 'DRAFT', 'MAILING_LIST', @ml5_id,
  NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY, NULL
);


-- ============================================================
-- 7. EMAIL HISTORY (table: email_history)
-- Colonnes : id, type, campaign_id, to_email, subject,
--            delivery_status, error_message, sent_at
-- type : CAMPAIGN, EMAIL_VERIFICATION, ADMIN_NOTIFICATION, BIRTHDAY, DIGEST
-- delivery_status : SUCCESS, FAILED
-- ============================================================
SET @camp1 = (SELECT id FROM email_campaign WHERE subject LIKE '%Bienvenue%' LIMIT 1);
SET @camp2 = (SELECT id FROM email_campaign WHERE subject LIKE '%Portes Ouvertes%' LIMIT 1);
SET @camp3 = (SELECT id FROM email_campaign WHERE subject LIKE '%Club Informatique%' LIMIT 1);

INSERT INTO email_history (type, campaign_id, to_email, subject, delivery_status, error_message, sent_at)
VALUES
-- Campagne 1 - Bienvenue
('CAMPAIGN', @camp1, 'safa.bennasr@esprit.tn',   'Bienvenue sur EspritConnect - Votre plateforme étudiante', 'SUCCESS', NULL, NOW() - INTERVAL 25 DAY),
('CAMPAIGN', @camp1, 'rahma.ayari@esprit.tn',    'Bienvenue sur EspritConnect - Votre plateforme étudiante', 'SUCCESS', NULL, NOW() - INTERVAL 25 DAY),
('CAMPAIGN', @camp1, 'ahmed.triki@esprit.tn',    'Bienvenue sur EspritConnect - Votre plateforme étudiante', 'SUCCESS', NULL, NOW() - INTERVAL 25 DAY),
('CAMPAIGN', @camp1, 'mohamed.slim@esprit.tn',   'Bienvenue sur EspritConnect - Votre plateforme étudiante', 'SUCCESS', NULL, NOW() - INTERVAL 25 DAY),
('CAMPAIGN', @camp1, 'nour.belhaj@esprit.tn',    'Bienvenue sur EspritConnect - Votre plateforme étudiante', 'FAILED',  'SMTP timeout after 30s', NOW() - INTERVAL 25 DAY),
('CAMPAIGN', @camp1, 'oussema.gharbi@esprit.tn', 'Bienvenue sur EspritConnect - Votre plateforme étudiante', 'SUCCESS', NULL, NOW() - INTERVAL 25 DAY),
('CAMPAIGN', @camp1, 'ines.maaref@esprit.tn',    'Bienvenue sur EspritConnect - Votre plateforme étudiante', 'SUCCESS', NULL, NOW() - INTERVAL 25 DAY),
('CAMPAIGN', @camp1, 'bilel.chaabane@esprit.tn', 'Bienvenue sur EspritConnect - Votre plateforme étudiante', 'SUCCESS', NULL, NOW() - INTERVAL 25 DAY),
-- Campagne 2 - Portes Ouvertes
('CAMPAIGN', @camp2, 'safa.bennasr@esprit.tn',   '📢 Rappel : Journée Portes Ouvertes Esprit - 15 Juin 2025', 'SUCCESS', NULL, NOW() - INTERVAL 10 DAY),
('CAMPAIGN', @camp2, 'rahma.ayari@esprit.tn',    '📢 Rappel : Journée Portes Ouvertes Esprit - 15 Juin 2025', 'SUCCESS', NULL, NOW() - INTERVAL 10 DAY),
('CAMPAIGN', @camp2, 'admin@esprit.tn',           '📢 Rappel : Journée Portes Ouvertes Esprit - 15 Juin 2025', 'SUCCESS', NULL, NOW() - INTERVAL 10 DAY),
('CAMPAIGN', @camp2, 'karim.oueslati@gmail.com',  '📢 Rappel : Journée Portes Ouvertes Esprit - 15 Juin 2025', 'FAILED',  'Invalid email address', NOW() - INTERVAL 10 DAY),
-- Campagne 3 - Club Info
('CAMPAIGN', @camp3, 'safa.bennasr@esprit.tn',   '🚀 Opportunités de stage été 2025 - Club Informatique', 'SUCCESS', NULL, NOW() - INTERVAL 5 DAY),
('CAMPAIGN', @camp3, 'ahmed.triki@esprit.tn',    '🚀 Opportunités de stage été 2025 - Club Informatique', 'SUCCESS', NULL, NOW() - INTERVAL 5 DAY),
('CAMPAIGN', @camp3, 'bilel.chaabane@esprit.tn', '🚀 Opportunités de stage été 2025 - Club Informatique', 'SUCCESS', NULL, NOW() - INTERVAL 5 DAY),
('CAMPAIGN', @camp3, 'rami.benali@esprit.tn',    '🚀 Opportunités de stage été 2025 - Club Informatique', 'FAILED',  'Connection refused by recipient server', NOW() - INTERVAL 5 DAY),
-- Emails système
('EMAIL_VERIFICATION', NULL, 'safa.bennasr@esprit.tn',   'Vérification de votre adresse email EspritConnect', 'SUCCESS', NULL, NOW() - INTERVAL 30 DAY),
('EMAIL_VERIFICATION', NULL, 'rahma.ayari@esprit.tn',    'Vérification de votre adresse email EspritConnect', 'SUCCESS', NULL, NOW() - INTERVAL 28 DAY),
('EMAIL_VERIFICATION', NULL, 'ahmed.triki@esprit.tn',    'Vérification de votre adresse email EspritConnect', 'SUCCESS', NULL, NOW() - INTERVAL 27 DAY),
('ADMIN_NOTIFICATION', NULL, 'admin@esprit.tn',           'Nouvelle inscription en attente d''approbation',     'SUCCESS', NULL, NOW() - INTERVAL 20 DAY),
('ADMIN_NOTIFICATION', NULL, 'admin@esprit.tn',           'Nouvelle inscription en attente d''approbation',     'SUCCESS', NULL, NOW() - INTERVAL 15 DAY),
('BIRTHDAY',           NULL, 'safa.bennasr@esprit.tn',   'Joyeux anniversaire Safa ! 🎂',                     'SUCCESS', NULL, NOW() - INTERVAL 3 DAY);


-- ============================================================
-- VÉRIFICATION FINALE
-- ============================================================
SELECT '=== RÉSUMÉ DES DONNÉES INSÉRÉES ===' AS info;
SELECT 'forum_category'       AS `Table`, COUNT(*) AS `Lignes insérées` FROM forum_category
UNION ALL SELECT 'forum_post',            COUNT(*) FROM forum_post
UNION ALL SELECT 'forum_reply',           COUNT(*) FROM forum_reply
UNION ALL SELECT 'mailing_list',          COUNT(*) FROM mailing_list
UNION ALL SELECT 'mailing_list_member',   COUNT(*) FROM mailing_list_member
UNION ALL SELECT 'email_campaign',        COUNT(*) FROM email_campaign
UNION ALL SELECT 'email_history',         COUNT(*) FROM email_history;
