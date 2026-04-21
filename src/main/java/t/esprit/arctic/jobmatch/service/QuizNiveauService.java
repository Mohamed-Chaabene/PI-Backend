package t.esprit.arctic.jobmatch.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import t.esprit.arctic.jobmatch.dto.*;
import t.esprit.arctic.jobmatch.entity.*;
import t.esprit.arctic.jobmatch.repository.InscriptionParcoursRepository;
import t.esprit.arctic.jobmatch.repository.QuizNiveauRepository;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class QuizNiveauService {

    private final QuizNiveauRepository quizRepository;
    private final InscriptionParcoursRepository inscriptionParcoursRepository;
    private final CertificatService certificatService;
    private final InscriptionFormationService inscriptionFormationService;

    @Value("${gemini.api.key:}")
    private String groqApiKey;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * ÉTAPE 1 : Génère les questions via Groq
     */
    @Transactional
    public QuizNiveau genererQuiz(QuizGenerationRequest req) {
        // 1. Vérifier que l'inscription existe
        InscriptionParcours inscription = inscriptionParcoursRepository.findById(req.getInscriptionParcoursId())
                .orElseThrow(() -> new RuntimeException("Inscription parcours non trouvée"));

        NiveauOrdre niveau = req.getNiveau() != null ? req.getNiveau() : inscription.getNiveauActuel();

        // 2. Vérifier que le candidat n'a pas déjà réussi ce niveau
        if (quizRepository.existsByInscriptionParcoursIdAndNiveauAndReussiTrue(
                inscription.getId(), niveau)) {
            throw new RuntimeException("Ce niveau a déjà été réussi");
        }

        int nombreQuestions = req.getNombreQuestions() > 0 ? req.getNombreQuestions() : 10;
        String titreFormation = req.getTitreFormation() != null ? req.getTitreFormation() : "Formation";

        // 3. Construire le prompt et appeler Groq
        String prompt = buildQuizPrompt(titreFormation, niveau,
                inscription.getParcours().getCategorie(), nombreQuestions);

        String questionsJson = null;
        Exception lastEx = null;
        for (int i = 0; i < 2; i++) {
            try {
                questionsJson = callGroqForQuiz(prompt);
                break;
            } catch (Exception e) {
                lastEx = e;
                System.err.println("Tentative " + (i+1) + " échouée : " + e.getMessage());
            }
        }

        if (questionsJson == null) {
            throw new RuntimeException("Erreur lors de la génération du quiz par l'IA : " + 
                (lastEx != null ? lastEx.getMessage() : "Inconnue"));
        }

        // 4. Calculer le numéro de tentative
        int tentative = quizRepository.countByInscriptionParcoursIdAndNiveau(
                inscription.getId(), niveau) + 1;

        // 5. Sauvegarder le QuizNiveau
        QuizNiveau quiz = new QuizNiveau();
        quiz.setInscriptionParcours(inscription);
        quiz.setNiveau(niveau);
        quiz.setQuestionsJson(questionsJson);
        quiz.setScore(0);
        quiz.setReussi(false);
        quiz.setTentative(tentative);
        quiz.setDateTentative(LocalDateTime.now());

        return quizRepository.save(quiz);
    }

    /**
     * Récupère un quiz par ID (les bonnes réponses sont masquées côté contrôleur).
     */
    public QuizNiveau getById(Long id) {
        return quizRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quiz non trouvé : " + id));
    }

    /**
     * Historique des quiz d'une inscription parcours.
     */
    public List<QuizNiveau> getHistorique(Long inscriptionParcoursId) {
        return quizRepository.findByInscriptionParcoursIdOrderByDateTentativeDesc(inscriptionParcoursId);
    }

    /**
     * ÉTAPE 2 : Corrige le quiz soumis
     */
    @Transactional
    public QuizResultatDTO soumettre(QuizSoumissionDTO dto) {
        // 1. Récupérer le QuizNiveau
        QuizNiveau quiz = quizRepository.findById(dto.getQuizNiveauId())
                .orElseThrow(() -> new RuntimeException("Quiz non trouvé"));

        if (quiz.getReponsesCandidat() != null && !quiz.getReponsesCandidat().isEmpty()) {
            throw new RuntimeException("Ce quiz a déjà été soumis");
        }

        // 2. Parser questionsJson pour obtenir les bonnes réponses
        List<Map<String, Object>> questions;
        try {
            JsonNode root = mapper.readTree(quiz.getQuestionsJson());
            JsonNode questionsNode = root.has("questions") ? root.get("questions") : root;
            questions = mapper.convertValue(questionsNode,
                    new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Erreur parsing des questions : " + e.getMessage());
        }

        // 3. Comparer avec les réponses du candidat
        Map<Integer, String> reponses = dto.getReponses();
        int nbBonnes = 0;
        int total = questions.size();
        List<CorrectionQuestionDTO> corrections = new ArrayList<>();

        for (int i = 0; i < total; i++) {
            Map<String, Object> q = questions.get(i);
            String bonneReponse = Objects.toString(q.get("bonneReponse"), "").trim();
            String reponseCandidat = reponses != null && reponses.containsKey(i + 1)
                    ? Objects.toString(reponses.get(i + 1), "").trim() : "";

            boolean correct = bonneReponse.equalsIgnoreCase(reponseCandidat);
            if (correct) nbBonnes++;

            corrections.add(CorrectionQuestionDTO.builder()
                    .questionId(i + 1)
                    .enonce(Objects.toString(q.get("enonce"), ""))
                    .reponseCandidat(reponseCandidat)
                    .bonneReponse(bonneReponse)
                    .correct(correct)
                    .explication(Objects.toString(q.get("explication"), ""))
                    .build());
        }

        // 4. Calculer le score
        int score = total > 0 ? (int) Math.round((double) nbBonnes / total * 100) : 0;

        // 5. Récupérer le seuil dynamique
        NiveauOrdre niveau = quiz.getNiveau();
        int seuil = niveau.seuilReussite();
        boolean reussi = score >= seuil;

        // 6. Sauvegarder les réponses et le score
        try {
            quiz.setReponsesCandidat(mapper.writeValueAsString(reponses));
        } catch (Exception e) {
            quiz.setReponsesCandidat("{}");
        }
        quiz.setScore(score);
        quiz.setReussi(reussi);
        quizRepository.save(quiz);

        // 7. Déterminer le besoin en feedback et mettre à jour l'inscription si réussi
        InscriptionParcours inscription = quiz.getInscriptionParcours();
        FeedbackType needsFeedback = FeedbackType.NONE;
        String nextNiveauLabel = null;
        String message;

        if (reussi) {
            NiveauOrdre suivant = niveau.suivant();
            boolean isLastLevel = (suivant == null) || 
                                 (inscription.getParcours().getFormationParNiveau(suivant) == null);

            if (isLastLevel) {
                needsFeedback = FeedbackType.MACRO;
                // Quiz Final réussi — parcours marqué comme terminé
                inscription.setStatut("TERMINE");
                inscription.setEvaluationParcoursRequise(true);
                inscriptionParcoursRepository.save(inscription);

                // Marquer la formation du niveau actuel comme terminée
                Formation lastFormation = inscription.getParcours().getFormationParNiveau(niveau);
                if (lastFormation != null) {
                    inscriptionFormationService.marquerCommeTerminee(inscription.getCandidat(), lastFormation);
                }

                message = String.format("Félicitations ! Vous avez complété le parcours avec un score de %d%% au niveau %s. " +
                        "Une dernière étape : obtenez votre certificat en partageant votre expérience !", 
                        score, niveau.toNiveauLabel());
            } else {
                // Micro-feedback pour les niveaux intermédiaires (pas le premier)
                if (!niveau.isFirst()) {
                    needsFeedback = FeedbackType.MICRO;
                }

                inscription.setNiveauActuel(suivant);
                inscriptionParcoursRepository.save(inscription);
                nextNiveauLabel = suivant.toNiveauLabel();

                // 1. Marquer la formation du niveau actuel comme terminée
                Formation currentLevelFormation = inscription.getParcours().getFormationParNiveau(niveau);
                if (currentLevelFormation != null) {
                    inscriptionFormationService.marquerCommeTerminee(inscription.getCandidat(), currentLevelFormation);
                }

                // 2. Inscription automatique à la Formation du niveau suivant
                Formation nextFormation = inscription.getParcours().getFormationParNiveau(suivant);
                if (nextFormation != null) {
                    inscriptionFormationService.inscrireAutomatiquement(inscription.getCandidat(), nextFormation);
                }

                message = String.format("Bravo ! Vous avez réussi le niveau %s avec %d%%. " +
                        "Le niveau %s est maintenant débloqué !",
                        niveau.toNiveauLabel(), score, suivant.toNiveauLabel());
            }
        } else {
            message = String.format("Score : %d%% (seuil requis : %d%%). " +
                    "Vous pouvez réessayer ce niveau.", score, seuil);
        }

        return QuizResultatDTO.builder()
                .score(score)
                .seuilRequis(seuil)
                .reussi(reussi)
                .needsFeedback(needsFeedback)
                .nextNiveauLabel(nextNiveauLabel)
                .niveauSuivantDebloque(reussi && !niveau.isLast() ? niveau.suivant() : null)
                .message(message)
                .corrections(corrections)
                .build();
    }

    // ── Appel Groq ──────────────────────────────────────────────

    private String callGroqForQuiz(String prompt) throws Exception {
        Map<String, Object> requestBody = Map.of(
                "model", "llama-3.1-8b-instant",
                "messages", List.of(
                        Map.of("role", "system", "content",
                                "Tu es un générateur de quiz pédagogique. " +
                                "Réponds UNIQUEMENT en JSON valide, sans texte avant ou après."),
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.7,
                "max_tokens", 4096
        );

        String jsonBody = mapper.writeValueAsString(requestBody);

        HttpResponse<String> response = httpClient.send(
                HttpRequest.newBuilder()
                        .uri(URI.create("https://api.groq.com/openai/v1/chat/completions"))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + groqApiKey)
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        JsonNode root = mapper.readTree(response.body());

        if (root.has("error")) {
            String errMsg = root.path("error").path("message").asText("Erreur Groq inconnue");
            throw new RuntimeException("Groq API error: " + errMsg);
        }

        String content = root.path("choices").get(0)
                .path("message").path("content").asText("").trim();

        // Hardened JSON Extraction: Trouve le premier '{' et le dernier '}'
        int start = content.indexOf('{');
        int end = content.lastIndexOf('}');
        if (start >= 0 && end > start) {
            content = content.substring(start, end + 1);
        }

        // Nettoyage supplémentaire des backticks habituels
        content = content.replaceAll("(?s)```json\\s*", "")
                         .replaceAll("(?s)```\\s*", "").trim();

        // Valider que c'est du JSON valide
        try {
            mapper.readTree(content);
        } catch (Exception e) {
            System.err.println("❌ Erreur parsing JSON AI: " + e.getMessage());
            System.err.println("Contenu brut: " + content);
            throw new RuntimeException("L'IA a généré un format invalide. Veuillez réessayer.");
        }

        return content;
    }

    private String buildQuizPrompt(String titreFormation, NiveauOrdre niveau,
                                    String categorie, int nombreQuestions) {
        String niveauLabel = niveau.toNiveauLabel();
        return String.format("""
            Tu es un générateur de quiz pédagogique.
            Formation : "%s"
            Niveau : %s
            Catégorie : %s
            
            Génère exactement %d questions QCM (4 choix chacune) adaptées au niveau %s.
            - Niveau Débutant : définitions, concepts de base, syntaxe simple
            - Niveau Intermédiaire : application pratique, patterns courants, cas d'usage
            - Niveau Avancé : optimisation, cas complexes, bonnes pratiques, design patterns
            - Niveau Expert : architecture, performance, sécurité, cas limites, scalabilité
            
            CONSIGNES GÉNÉRALES :
            - Les questions doivent être techniques et précises.
            - Utilise des guillemets simples (') à l'intérieur des chaînes JSON pour éviter de casser le format.
            - Ne tronque JAMAIS la réponse.
            
            RÈGLE CRITIQUE : Réponds UNIQUEMENT en JSON valide, sans aucun texte avant ou après.
            
            Format exact attendu :
            {
              "questions": [
                {
                  "id": 1,
                  "enonce": "...",
                  "choix": ["A. ...", "B. ...", "C. ...", "D. ..."],
                  "bonneReponse": "A",
                  "explication": "..."
                }
              ]
            }
            """, titreFormation, niveauLabel, categorie, nombreQuestions, niveauLabel);
    }
}
