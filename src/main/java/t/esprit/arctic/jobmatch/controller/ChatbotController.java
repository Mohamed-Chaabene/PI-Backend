package t.esprit.arctic.jobmatch.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.UUID;

import t.esprit.arctic.jobmatch.entity.ChatbotHistory;
import t.esprit.arctic.jobmatch.repository.ChatbotHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/chatbot")
@CrossOrigin(origins = "http://localhost:4200", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class ChatbotController {

    @Autowired
    private ChatbotHistoryRepository chatHistoryRepo;

    @Value("${groq.api.key:}")
    private String groqApiKey;

    @Value("${groq.api.url:https://api.groq.com/openai/v1/chat/completions}")
    private String groqApiUrl;

    @Value("${groq.model:mixtral-8x7b-32768}")
    private String groqModel;

    private final HttpClient   http   = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    // ── Helper : construit le bloc image_url selon base64 ou URL ────────
    private Map<String, Object> buildImageContent(String imageUrl) {
        if (imageUrl.startsWith("data:image")) {
            String mediaType  = "image/jpeg";
            String base64Data = imageUrl;
            if (imageUrl.contains(";base64,")) {
                String[] parts = imageUrl.split(";base64,");
                mediaType  = parts[0].replace("data:", "");
                base64Data = parts[1];
            }
            return Map.of(
                    "type", "image_url",
                    "image_url", Map.of("url", "data:" + mediaType + ";base64," + base64Data)
            );
        } else {
            return Map.of(
                    "type", "image_url",
                    "image_url", Map.of("url", imageUrl)
            );
        }
    }

    // ── Helper : remplace le base64 par un placeholder avant sauvegarde ──
    // Les images base64 font plusieurs Mo → trop grand pour TEXT en MySQL
    private String sanitizeImageUrlForDb(String imageUrl) {
        if (imageUrl != null && imageUrl.startsWith("data:image")) {
            String mediaType = "image/jpeg";
            if (imageUrl.contains(";base64,")) {
                mediaType = imageUrl.split(";base64,")[0].replace("data:", "");
            }
            return "[image:" + mediaType + "]";
        }
        return imageUrl;
    }

    @GetMapping("/history")
    public ResponseEntity<List<Map<String, Object>>> getHistory(
            @RequestParam Long candidatId,
            @RequestParam Long formationId) {

        List<ChatbotHistory> histories = chatHistoryRepo
                .findAllByCandidatIdAndFormationIdOrderByCreatedAtDesc(candidatId, formationId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (ChatbotHistory h : histories) {
            Map<String, Object> sessionData = new HashMap<>();
            sessionData.put("id", h.getId());
            sessionData.put("sessionId", h.getSessionId());
            sessionData.put("sessionTitle", h.getSessionTitle());
            sessionData.put("createdAt", h.getCreatedAt() != null ? h.getCreatedAt().toString() : null);

            List<Map<String, String>> messages = new ArrayList<>();
            if (h.getHistoriqueJson() != null && !h.getHistoriqueJson().isEmpty()) {
                try {
                    messages = mapper.readValue(h.getHistoriqueJson(), List.class);
                } catch (Exception e) {}
            }
            sessionData.put("messages", messages);
            result.add(sessionData);
        }

        return ResponseEntity.ok(result);
    }

    @PutMapping("/session/{sessionId}")
    public ResponseEntity<?> updateSession(
            @PathVariable String sessionId,
            @RequestBody Map<String, Object> body) {

        Optional<ChatbotHistory> histOpt = chatHistoryRepo.findBySessionId(sessionId);
        if (!histOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        ChatbotHistory hist = histOpt.get();

        if (body.containsKey("sessionTitle")) {
            hist.setSessionTitle(body.get("sessionTitle").toString());
        }

        if (body.containsKey("messages")) {
            try {
                String json = mapper.writeValueAsString(body.get("messages"));
                hist.setHistoriqueJson(json);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid messages format"));
            }
        }

        chatHistoryRepo.save(hist);
        return ResponseEntity.ok(Map.of("message", "Session updated successfully"));
    }

    @Transactional
    @DeleteMapping("/session/{sessionId}")
    public ResponseEntity<?> deleteSession(@PathVariable String sessionId) {
        try {
            Optional<ChatbotHistory> histOpt = chatHistoryRepo.findBySessionId(sessionId);
            if (histOpt.isPresent()) {
                chatHistoryRepo.deleteById(histOpt.get().getId());
                return ResponseEntity.ok(Map.of("message", "Session deleted successfully"));
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "error", e.getMessage() != null ? e.getMessage() : e.toString(),
                    "trace", Arrays.toString(e.getStackTrace())
            ));
        }
    }

    @PostMapping("/formation")
    public ResponseEntity<Map<String, String>> chat(
            @RequestBody Map<String, Object> body) throws Exception {

        // ── Vérifier la clé GROQ API ────────────────────────────────────
        if (groqApiKey == null || groqApiKey.trim().isEmpty()) {
            System.err.println("CRITICAL ERROR: groq.api.key is not configured in application.properties");
            return ResponseEntity.ok(Map.of(
                "response", "Erreur: Cle API GROQ non configuree. Veuillez verifier application.properties"
            ));
        }

        String message   = body.getOrDefault("message", "").toString();
        String imageUrl  = body.containsKey("imageUrl") && body.get("imageUrl") != null
                ? body.get("imageUrl").toString() : null;

        boolean hasImage = imageUrl != null && !imageUrl.trim().isEmpty();

        String titreFormation = body.getOrDefault("titreFormation", "").toString();
        String categorie      = body.getOrDefault("categorie", "").toString();
        String niveau         = body.getOrDefault("niveau", "").toString();
        String context        = body.getOrDefault("context", "video").toString();
        String sessionId      = body.containsKey("sessionId") && body.get("sessionId") != null
                ? body.get("sessionId").toString() : null;

        Long candidatId  = body.containsKey("candidatId")  && body.get("candidatId")  != null
                ? Long.parseLong(body.get("candidatId").toString())  : null;
        Long formationId = body.containsKey("formationId") && body.get("formationId") != null
                ? Long.parseLong(body.get("formationId").toString()) : null;

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> historyFromFrontend =
                body.containsKey("history") && body.get("history") != null
                        ? (List<Map<String, Object>>) body.get("history") : new ArrayList<>();

        if (message.isEmpty() && !hasImage) {
            return ResponseEntity.badRequest().body(Map.of("error", "Message vide"));
        }

        // ── Gestion Historique BDD ──────────────────────────────────────
        List<Map<String, Object>> dbHistoryList = new ArrayList<>();
        ChatbotHistory chatHistDb = null;

        if (candidatId != null && formationId != null) {
            if (sessionId != null && !sessionId.isEmpty()) {
                chatHistDb = chatHistoryRepo.findBySessionId(sessionId).orElse(null);
            }

            if (chatHistDb == null) {
                chatHistDb = new ChatbotHistory();
                chatHistDb.setCandidatId(candidatId);
                chatHistDb.setFormationId(formationId);
                chatHistDb.setSessionId(UUID.randomUUID().toString());
                chatHistDb.setCreatedAt(java.time.LocalDateTime.now());
                String shortTitle = message.length() > 30 ? message.substring(0, 30) + "..." : message;
                chatHistDb.setSessionTitle(shortTitle);
            } else {
                if (chatHistDb.getHistoriqueJson() != null && !chatHistDb.getHistoriqueJson().isEmpty()) {
                    try {
                        dbHistoryList = mapper.readValue(chatHistDb.getHistoriqueJson(), List.class);
                    } catch (Exception e) {}
                }
            }
        }

        // ── Ajouter le message user en BDD (base64 → placeholder) ──────
        Map<String, Object> userMsgForDb = new HashMap<>();
        userMsgForDb.put("role", "user");
        userMsgForDb.put("content", message);
        if (hasImage) {
            // JAMAIS stocker le base64 brut → remplacer par un placeholder court
            userMsgForDb.put("imageUrl", sanitizeImageUrlForDb(imageUrl));
        }
        dbHistoryList.add(userMsgForDb);

        // ── Construire les messages pour GROQ (vrais base64/URL) ────────
        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content",
                buildSystemPrompt(titreFormation, categorie, niveau, context)));

        List<Map<String, Object>> sourceHistory = (candidatId != null && formationId != null)
                ? dbHistoryList : historyFromFrontend;
        int historySizeToSend = (candidatId != null && formationId != null)
                ? sourceHistory.size() - 1 : sourceHistory.size();

        int start = Math.max(0, historySizeToSend - 8);
        boolean historyHasImage = false;

        for (int i = start; i < historySizeToSend; i++) {
            Map<String, Object> h = sourceHistory.get(i);
            String role           = h.getOrDefault("role", "user").toString();
            String contentText    = h.getOrDefault("content", "").toString();
            String imgUrl         = h.containsKey("imageUrl") && h.get("imageUrl") != null
                    ? h.get("imageUrl").toString() : null;

            // Les placeholders "[image:...]" issus de la BDD ne peuvent pas être envoyés à Groq
            boolean isPlaceholder = imgUrl != null && imgUrl.startsWith("[image:");
            boolean hasRealImg    = imgUrl != null && !imgUrl.trim().isEmpty() && !isPlaceholder;

            if (hasRealImg) {
                historyHasImage = true;
                messages.add(Map.of(
                        "role", role,
                        "content", List.of(
                                Map.of("type", "text", "text", contentText),
                                buildImageContent(imgUrl)
                        )
                ));
            } else {
                messages.add(Map.of("role", role, "content", contentText));
            }
        }

        // ── Message actuel avec la vraie image ──────────────────────────
        if (hasImage) {
            historyHasImage = true;
            messages.add(Map.of(
                    "role", "user",
                    "content", List.of(
                            Map.of("type", "text", "text", message),
                            buildImageContent(imageUrl)
                    )
            ));
        } else {
            messages.add(Map.of("role", "user", "content", message));
        }

        // ── Choix du modèle ──────────────────────────────────────────────
        String modelToUse = (hasImage || historyHasImage)
            ? "meta-llama/llama-4-scout-17b-16e-instruct"
                : groqModel;

        // ── Appel GROQ API ───────────────────────────────────────────────
        String requestBody = mapper.writeValueAsString(Map.of(
                "model",       modelToUse,
                "messages",    messages,
                "temperature", 0.7,
                "max_tokens",  1024
        ));

        System.out.println("=== GROQ REQUEST ===");
        System.out.println("Model: " + modelToUse);
        System.out.println("Has image: " + hasImage);
        System.out.println("API Key present: " + (groqApiKey != null && !groqApiKey.isEmpty()));

        HttpResponse<String> response = http.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(groqApiUrl))
                        .header("Content-Type",  "application/json")
                        .header("Authorization", "Bearer " + groqApiKey)
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        System.out.println("=== GROQ RESPONSE STATUS: " + response.statusCode() + " ===");
        System.out.println("GROQ Response body: " + response.body());

        JsonNode root = mapper.readTree(response.body());

        if (response.statusCode() != 200) {
            System.err.println("WARNING: GROQ API returned status " + response.statusCode());
            if (root.has("error")) {
                String errMsg = root.path("error").path("message").asText("Unknown error");
                System.err.println("GROQ error message: " + errMsg);
                return ResponseEntity.ok(Map.of(
                        "response", "Désolé, je rencontre un problème technique : " + errMsg
                ));
            }
        }

        if (root.has("error")) {
            String errMsg = root.path("error").path("message").asText();
            System.err.println("GROQ error: " + errMsg);
            return ResponseEntity.ok(Map.of(
                    "response", "Désolé, je rencontre un problème technique : " + errMsg
            ));
        }

        String reply = root.path("choices").get(0)
                .path("message").path("content").asText("Pas de réponse.");

        // ── Sauvegarder la réponse assistant en BDD ─────────────────────
        if (chatHistDb != null && candidatId != null && formationId != null) {
            Map<String, Object> aiMsgMap = new HashMap<>();
            aiMsgMap.put("role", "assistant");
            aiMsgMap.put("content", reply);
            dbHistoryList.add(aiMsgMap);
            chatHistDb.setHistoriqueJson(mapper.writeValueAsString(dbHistoryList));
            chatHistoryRepo.save(chatHistDb);

            Map<String, String> result = new HashMap<>();
            result.put("response", reply);
            result.put("sessionId", chatHistDb.getSessionId());
            result.put("sessionTitle", chatHistDb.getSessionTitle());
            return ResponseEntity.ok(result);
        }

        return ResponseEntity.ok(Map.of("response", reply));
    }

    // ✅ SEULE MODIFICATION : buildSystemPrompt mis à jour pour le multilangue
    private String buildSystemPrompt(String titre, String categorie, String niveau, String context) {

        String contextDesc = context.equals("video")
                ? "a video training course / une formation vidéo"
                : "technical documentation / une documentation technique";

        return String.format("""
            You are a pedagogical AI assistant embedded in %s.
            
            Training: "%s"
            Category: %s
            Level: %s
            
            Your role:
            - Answer learners' questions about this training
            - Explain technical concepts clearly and pedagogically
            - Provide code examples when useful
            - Summarize chapters or concepts
            - Suggest practical exercises
            - Encourage and motivate the learner
            
            === CRITICAL LANGUAGE RULE ===
            You MUST detect the language of the user's message and reply
            in EXACTLY the same language.
            - If the user writes in French  → reply entirely in French
            - If the user writes in English → reply entirely in English
            - If the user writes in Arabic  → reply entirely in Arabic
            - Never mix languages in the same response
            - Never explain this rule to the user
            
            === FORMAT ===
            - Be concise and precise (max 300 words unless asked otherwise)
            - Use bullet points for clarity when appropriate
            - Bold (**) important terms
            - Use code blocks for code samples
            - Stay in the context of the training "%s"
            - If the question is off-topic, politely redirect to the training
            """,
                contextDesc, titre, categorie, niveau, titre
        );
    }
}