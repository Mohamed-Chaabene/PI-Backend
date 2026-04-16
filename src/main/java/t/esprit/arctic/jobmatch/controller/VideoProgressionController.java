package t.esprit.arctic.jobmatch.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import t.esprit.arctic.jobmatch.entity.VideoProgression;
import t.esprit.arctic.jobmatch.repository.InscriptionFormationRepository;
import t.esprit.arctic.jobmatch.repository.VideoProgressionRepository;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/video-progression")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class VideoProgressionController {

    private final VideoProgressionRepository     videoProgressionRepo;
    private final InscriptionFormationRepository inscriptionRepo;

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient   http   = HttpClient.newHttpClient();

    @Value("${youtube.api.key}")
    private String youtubeApiKey;

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

<<<<<<< HEAD
    // ══════════════════════════════════════════════════════════════
    // 1. Marquer une vidéo comme vue
    // ══════════════════════════════════════════════════════════════
=======
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
    @PostMapping("/video-vue")
    public ResponseEntity<Map<String, Object>> marquerVideoVue(
            @RequestBody Map<String, Object> body) {

        Long   inscriptionId = Long.valueOf(body.get("inscriptionId").toString());
        Long   candidatId    = Long.valueOf(body.get("candidatId").toString());
        Long   formationId   = Long.valueOf(body.get("formationId").toString());
        String videoId       = body.get("videoId").toString();
        int    totalVideos   = Integer.parseInt(
                body.getOrDefault("totalVideos", "1").toString());

        Optional<VideoProgression> existing =
                videoProgressionRepo.findByInscriptionIdAndVideoId(
                        inscriptionId, videoId);

        VideoProgression vp = existing.orElse(new VideoProgression());
        vp.setInscriptionId(inscriptionId);
        vp.setCandidatId(candidatId);
        vp.setFormationId(formationId);
        vp.setVideoId(videoId);
        vp.setVuComplete(true);
        vp.setQuizReussi(true);
        vp.setScoreQuiz(100);
        vp.setDateVue(LocalDateTime.now());
        videoProgressionRepo.save(vp);

        int progression = calculerProgression(inscriptionId, totalVideos);
        mettreAJourInscription(inscriptionId, progression);

        Map<String, Object> result = new HashMap<>();
        result.put("progression",      progression);
        result.put("videoId",          videoId);
        result.put("formationTerminee", progression >= 100);
        result.put("message",
                "Vidéo terminée ✅ Progression : " + progression + "%");
        return ResponseEntity.ok(result);
    }

<<<<<<< HEAD
    // ══════════════════════════════════════════════════════════════
    // 2. Récupérer la progression
    // ══════════════════════════════════════════════════════════════
=======
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
    @GetMapping("/inscription/{inscriptionId}")
    public ResponseEntity<Map<String, Object>> getProgression(
            @PathVariable Long inscriptionId,
            @RequestParam(defaultValue = "1") int totalVideos) {

        List<VideoProgression> vps =
                videoProgressionRepo.findByInscriptionId(inscriptionId);

        int progression = calculerProgression(inscriptionId, totalVideos);

        Map<String, Object> result = new HashMap<>();
        result.put("videosVues",  vps.stream()
                .filter(VideoProgression::isVuComplete).count());
        result.put("totalVideos", totalVideos);
        result.put("progression", progression);
        result.put("details",     vps);
        return ResponseEntity.ok(result);
    }

<<<<<<< HEAD
    // ══════════════════════════════════════════════════════════════
    // 3. Générer le quiz FINAL de la formation entière
    // ══════════════════════════════════════════════════════════════
=======
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
    @PostMapping("/quiz-final/generer")
    public ResponseEntity<Map<String, Object>> genererQuizFinal(
            @RequestBody Map<String, Object> body) throws Exception {

        Long   inscriptionId = Long.valueOf(body.get("inscriptionId").toString());
        String titreFomation = body.getOrDefault("titreFormation", "").toString();
        String categorie     = body.getOrDefault("categorie", "").toString();
        String playlistId    = body.getOrDefault("playlistId", "").toString();

<<<<<<< HEAD
        // Vérifier que la formation est bien terminée à 100%
        // (récupérer totalVideos depuis body)
=======
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
        int totalVideos = Integer.parseInt(
                body.getOrDefault("totalVideos", "1").toString());
        int progression = calculerProgression(inscriptionId, totalVideos);

        if (progression < 100) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "Formation non terminée");
            err.put("progression", progression);
            return ResponseEntity.badRequest().body(err);
        }

<<<<<<< HEAD
        // Récupérer les titres des vidéos vues
=======
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
        List<VideoProgression> vps =
                videoProgressionRepo.findByInscriptionId(inscriptionId);
        List<String> videoIds = vps.stream()
                .filter(VideoProgression::isVuComplete)
                .map(VideoProgression::getVideoId)
                .limit(10) // max 10 vidéos pour le contexte
                .toList();

<<<<<<< HEAD
        // Récupérer les infos YouTube des vidéos
        List<String> videoTitles = getVideoTitles(videoIds);

        // Générer le quiz final avec Claude
=======
        List<String> videoTitles = getVideoTitles(videoIds);

>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
        List<Map<String, Object>> questions =
                genererQuizFinal(titreFomation, categorie, videoTitles);

        Map<String, Object> result = new HashMap<>();
        result.put("questions",    questions);
        result.put("inscriptionId", inscriptionId);
        result.put("scoreMinimum", 70); // 70% pour obtenir le certificat
        return ResponseEntity.ok(result);
    }

<<<<<<< HEAD
    // ══════════════════════════════════════════════════════════════
    // 4. Soumettre le quiz final et générer le certificat si réussi
    // ══════════════════════════════════════════════════════════════
=======

>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
    @PostMapping("/quiz-final/soumettre")
    public ResponseEntity<Map<String, Object>> soumettreQuizFinal(
            @RequestBody Map<String, Object> body) {

        Long inscriptionId = Long.valueOf(body.get("inscriptionId").toString());
        int  score         = Integer.parseInt(body.get("score").toString());
        boolean reussi     = score >= 70;

        Map<String, Object> result = new HashMap<>();
        result.put("score",  score);
        result.put("reussi", reussi);

        if (reussi) {
<<<<<<< HEAD
            // Générer le certificat via le service
            try {
                inscriptionRepo.findById(inscriptionId).ifPresent(ins -> {
                    // Marquer comme certifié
=======
            try {
                inscriptionRepo.findById(inscriptionId).ifPresent(ins -> {
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
                    ins.setStatut("Terminé");
                    inscriptionRepo.save(ins);
                });
                result.put("certificatGenere", true);
                result.put("message",
                        "Félicitations ! Vous avez obtenu " + score
                                + "% — Votre certificat est disponible !");
            } catch (Exception e) {
                result.put("certificatGenere", false);
                result.put("message", "Score validé mais erreur certificat.");
            }
        } else {
            result.put("certificatGenere", false);
            result.put("message",
                    "Score insuffisant (" + score
                            + "%). Il faut 70% minimum. Vous pouvez réessayer !");
        }
        return ResponseEntity.ok(result);
    }

<<<<<<< HEAD
    // ══════════════════════════════════════════════════════════════
    // GÉNÉRATION QUIZ FINAL — Claude AI
    // ══════════════════════════════════════════════════════════════
=======

>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> genererQuizFinal(
            String titreFormation,
            String categorie,
            List<String> videoTitles) {

        if (geminiApiKey == null || geminiApiKey.isEmpty()) {
            return getQuizFinalFallback(titreFormation);
        }

        try {
            String listeVideos = videoTitles.isEmpty()
                    ? "Formation complète sur " + titreFormation
                    : String.join("\n- ", videoTitles);

            String prompt = """
                Tu es un formateur expert en %s.
                
                Le candidat vient de terminer la formation complète :
                "%s"
                
                Voici les chapitres/vidéos qu'il a regardés :
                - %s
                
                Génère exactement 10 questions QCM d'évaluation finale
                qui couvrent l'ensemble des concepts importants de cette formation.
                
                Règles :
                - Questions variées couvrant différents aspects de la formation
                - Difficulté progressive (3 faciles, 4 moyennes, 3 difficiles)
                - 4 options par question (A, B, C, D)
                - Une seule bonne réponse
                - Questions techniques et concrètes, PAS génériques
                - Inclure une explication pour la bonne réponse
                
                Réponds UNIQUEMENT en JSON valide sans markdown ni backticks :
                [
                  {
                    "question": "Question technique précise ?",
                    "options": ["Option A", "Option B", "Option C", "Option D"],
                    "correctIndex": 0,
                    "explication": "Explication de la bonne réponse",
                    "difficulte": "facile"
                  }
                ]
                """.formatted(categorie, titreFormation, listeVideos);

<<<<<<< HEAD
            // Structure OpenAI-compatible pour l'API gratuite GROQ (Llama 3.1)
=======
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
            String requestBody = mapper.writeValueAsString(Map.of(
                    "model", "llama-3.1-8b-instant", // Modèle courant gratuit et ultra-rapide
                    "messages", List.of(Map.of(
                            "role", "user",
                            "content", prompt
                    )),
                    "temperature", 0.5
            ));

            String url = "https://api.groq.com/openai/v1/chat/completions";

            HttpResponse<String> response = http.send(
                    HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .header("Content-Type", "application/json")
                            .header("Authorization", "Bearer " + geminiApiKey) // on a gardé l'ancien nom de variable
                            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                            .build(),
                    HttpResponse.BodyHandlers.ofString()
            );

            JsonNode root = mapper.readTree(response.body());

            if (root.has("error")) {
                System.err.println("Groq API error: "
                        + root.path("error").path("message").asText());
                return getQuizFinalFallback(titreFormation);
            }

<<<<<<< HEAD
            // Path OpenAI/Groq: choices[0].message.content
=======
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
            String text = root.path("choices").get(0)
                    .path("message").path("content").asText("[]").trim()
                    .replaceAll("(?s)```json\\s*", "")
                    .replaceAll("(?s)```\\s*", "").trim();

            int start = text.indexOf('[');
            int end   = text.lastIndexOf(']');
            if (start >= 0 && end > start) {
                text = text.substring(start, end + 1);
            }

            List<Map<String, Object>> questions =
                    mapper.readValue(text, List.class);

            System.out.println("✅ Quiz final généré par Gemini: "
                    + questions.size() + " questions pour " + titreFormation);
            return questions;

        } catch (Exception e) {
            System.err.println("Erreur quiz final Gemini: " + e.getMessage());
            return getQuizFinalFallback(titreFormation);
        }
    }

<<<<<<< HEAD
    // ── Récupérer les titres des vidéos YouTube ───────────────────
=======
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
    private List<String> getVideoTitles(List<String> videoIds) {
        if (videoIds.isEmpty()) return new ArrayList<>();
        try {
            String ids = String.join(",", videoIds);
            String url = "https://www.googleapis.com/youtube/v3/videos"
                    + "?part=snippet&id=" + ids
                    + "&key=" + youtubeApiKey;

            HttpResponse<String> response = http.send(
                    HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .header("Accept", "application/json")
                            .build(),
                    HttpResponse.BodyHandlers.ofString()
            );

            JsonNode root  = mapper.readTree(response.body());
            JsonNode items = root.path("items");

            List<String> titles = new ArrayList<>();
            for (JsonNode item : items) {
                String title = item.path("snippet").path("title").asText("");
                if (!title.isEmpty()) titles.add(title);
            }
            return titles;

        } catch (Exception e) {
            System.err.println("Erreur récup titres: " + e.getMessage());
            return new ArrayList<>();
        }
    }

<<<<<<< HEAD
    // ── Quiz fallback si Claude indisponible ──────────────────────
=======
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
    private List<Map<String, Object>> getQuizFinalFallback(
            String titreFormation) {
        List<Map<String, Object>> questions = new ArrayList<>();

        Object[][] data = {
                {"Quel est l'objectif principal de la formation \"" + titreFormation + "\" ?",
                        new String[]{"Maîtriser les concepts fondamentaux",
                                "Obtenir un certificat uniquement",
                                "Regarder des vidéos",
                                "Aucune réponse"},
                        0, "La formation vise à maîtriser les concepts fondamentaux."},
                {"Quelle est la meilleure pratique après avoir terminé une formation ?",
                        new String[]{"Pratiquer immédiatement sur des projets réels",
                                "Attendre d'avoir tout mémorisé",
                                "Regarder d'autres formations",
                                "Ne rien faire"},
                        0, "La pratique immédiate consolide les apprentissages."},
                {"Comment évaluez-vous votre niveau après cette formation ?",
                        new String[]{"Je peux créer des projets de base",
                                "Je suis expert",
                                "Je n'ai rien appris",
                                "Je dois recommencer depuis zéro"},
                        0, "Une formation donne les bases pour créer des projets simples."}
        };

        for (Object[] d : data) {
            Map<String, Object> q = new HashMap<>();
            q.put("question",    d[0]);
            q.put("options",     Arrays.asList((String[]) d[1]));
            q.put("correctIndex", d[2]);
            q.put("explication", d[3]);
            q.put("difficulte",  "facile");
            questions.add(q);
        }
        return questions;
    }

<<<<<<< HEAD
    // ══════════════════════════════════════════════════════════════
    // UTILITAIRES
    // ══════════════════════════════════════════════════════════════
=======
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
    private int calculerProgression(Long inscriptionId, int totalVideos) {
        if (totalVideos == 0) return 0;
        long videosVues = videoProgressionRepo
                .countByInscriptionIdAndVuCompleteTrue(inscriptionId);
        return (int) Math.min(100,
                Math.round((double) videosVues / totalVideos * 100));
    }

    private void mettreAJourInscription(Long inscriptionId, int progression) {
        inscriptionRepo.findById(inscriptionId).ifPresent(ins -> {
            ins.setProgression((double) progression);
            if (progression > 0 && progression < 100) {
                ins.setStatut("EnCours");
            }
<<<<<<< HEAD
            // Ne pas mettre "Terminé" automatiquement —
            // seulement après quiz final réussi
=======
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
            inscriptionRepo.save(ins);
        });
    }
}