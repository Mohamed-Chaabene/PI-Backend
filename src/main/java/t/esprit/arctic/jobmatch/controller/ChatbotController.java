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

<<<<<<< HEAD
=======
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import java.io.ByteArrayInputStream;
import java.util.Base64;

>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
@RestController
@RequestMapping("/api/chatbot")
@CrossOrigin(origins = "http://localhost:4200", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class ChatbotController {

    @Autowired
    private ChatbotHistoryRepository chatHistoryRepo;

    @Value("${gemini.api.key:}")
    private String groqApiKey;

    private final HttpClient   http   = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

<<<<<<< HEAD
    // ── Helper : construit le bloc image_url selon base64 ou URL ────────
=======
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
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

<<<<<<< HEAD
    // ── Helper : remplace le base64 par un placeholder avant sauvegarde ──
    // Les images base64 font plusieurs Mo → trop grand pour TEXT en MySQL
=======
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
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

<<<<<<< HEAD
=======
    private String sanitizeFileDataForDb(String fileData, String fileName) {
        if (fileData != null && fileData.startsWith("data:")) {
            String name = (fileName != null && !fileName.isEmpty()) ? fileName : "document";
            return "[file:" + name + "]";
        }
        return fileData;
    }

>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
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

        String message   = body.getOrDefault("message", "").toString();
        String imageUrl  = body.containsKey("imageUrl") && body.get("imageUrl") != null
                ? body.get("imageUrl").toString() : null;

<<<<<<< HEAD
        boolean hasImage = imageUrl != null && !imageUrl.trim().isEmpty();
=======
        String fileData  = body.containsKey("fileData") && body.get("fileData") != null
                ? body.get("fileData").toString() : null;
        String fileName  = body.containsKey("fileName") && body.get("fileName") != null
                ? body.get("fileName").toString() : null;
        String fileText  = body.containsKey("fileText") && body.get("fileText") != null
                ? body.get("fileText").toString() : null;

        if ((fileText == null || fileText.trim().isEmpty()) && fileData != null && fileData.contains(";base64,")) {
            try {
                String base64Data = fileData.split(";base64,")[1];
                byte[] decodedBytes = Base64.getDecoder().decode(base64Data);
                try (ByteArrayInputStream bis = new ByteArrayInputStream(decodedBytes)) {
                    if (fileName != null && fileName.toLowerCase().endsWith(".docx")) {
                        try (XWPFDocument document = new XWPFDocument(bis);
                             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
                            fileText = extractor.getText();
                        }
                    } else if (fileName != null && fileName.toLowerCase().endsWith(".pdf")) {
                        try (PDDocument document = PDDocument.load(bis)) {
                            PDFTextStripper pdfStripper = new PDFTextStripper();
                            fileText = pdfStripper.getText(document);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Backend document text extraction failed: " + e.getMessage());
            }
        }

        boolean hasImage = imageUrl != null && !imageUrl.trim().isEmpty();
        boolean hasFile  = (fileData != null && !fileData.trim().isEmpty())
                || (fileText != null && !fileText.trim().isEmpty());
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0

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

<<<<<<< HEAD
        if (message.isEmpty() && !hasImage) {
            return ResponseEntity.badRequest().body(Map.of("error", "Message vide"));
        }

        // ── Gestion Historique BDD ──────────────────────────────────────
=======
        if (message.isEmpty() && !hasImage && !hasFile) {
            return ResponseEntity.badRequest().body(Map.of("error", "Message vide"));
        }

>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
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
<<<<<<< HEAD
                String shortTitle = message.length() > 30 ? message.substring(0, 30) + "..." : message;
=======
                String titleSource = !message.isEmpty() ? message : (fileName != null ? fileName : "Document");
                String shortTitle  = titleSource.length() > 30 ? titleSource.substring(0, 30) + "..." : titleSource;
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
                chatHistDb.setSessionTitle(shortTitle);
            } else {
                if (chatHistDb.getHistoriqueJson() != null && !chatHistDb.getHistoriqueJson().isEmpty()) {
                    try {
                        dbHistoryList = mapper.readValue(chatHistDb.getHistoriqueJson(), List.class);
                    } catch (Exception e) {}
                }
            }
        }

<<<<<<< HEAD
        // ── Ajouter le message user en BDD (base64 → placeholder) ──────
=======
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
        Map<String, Object> userMsgForDb = new HashMap<>();
        userMsgForDb.put("role", "user");
        userMsgForDb.put("content", message);
        if (hasImage) {
<<<<<<< HEAD
            // JAMAIS stocker le base64 brut → remplacer par un placeholder court
            userMsgForDb.put("imageUrl", sanitizeImageUrlForDb(imageUrl));
        }
        dbHistoryList.add(userMsgForDb);

        // ── Construire les messages pour GROQ (vrais base64/URL) ────────
=======
            userMsgForDb.put("imageUrl", sanitizeImageUrlForDb(imageUrl));
        }
        if (hasFile) {
            // Stocker le nom du fichier + le texte extrait (pas le base64 brut)
            userMsgForDb.put("fileName", fileName);
            if (fileText != null && !fileText.isEmpty()) {
                // Stocker un extrait du texte (max 500 chars pour ne pas saturer la BDD)
                String excerpt = fileText.length() > 500 ? fileText.substring(0, 500) + "..." : fileText;
                userMsgForDb.put("fileExcerpt", excerpt);
            } else {
                userMsgForDb.put("fileExcerpt", sanitizeFileDataForDb(fileData, fileName));
            }
        }
        dbHistoryList.add(userMsgForDb);

>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
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

<<<<<<< HEAD
            // Les placeholders "[image:...]" issus de la BDD ne peuvent pas être envoyés à Groq
            boolean isPlaceholder = imgUrl != null && imgUrl.startsWith("[image:");
            boolean hasRealImg    = imgUrl != null && !imgUrl.trim().isEmpty() && !isPlaceholder;

=======
            boolean isPlaceholder = imgUrl != null && imgUrl.startsWith("[image:");
            boolean hasRealImg    = imgUrl != null && !imgUrl.trim().isEmpty() && !isPlaceholder;

            String fileExcerptHist = h.containsKey("fileExcerpt") && h.get("fileExcerpt") != null
                    ? h.get("fileExcerpt").toString() : null;
            String fileNameHist    = h.containsKey("fileName") && h.get("fileName") != null
                    ? h.get("fileName").toString() : null;

            String enrichedContent = contentText;
            if (fileExcerptHist != null && !fileExcerptHist.startsWith("[file:")) {
                enrichedContent += "\n\n[Contenu du document '" + fileNameHist + "':\n" + fileExcerptHist + "]";
            }

>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
            if (hasRealImg) {
                historyHasImage = true;
                messages.add(Map.of(
                        "role", role,
                        "content", List.of(
<<<<<<< HEAD
                                Map.of("type", "text", "text", contentText),
=======
                                Map.of("type", "text", "text", enrichedContent),
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
                                buildImageContent(imgUrl)
                        )
                ));
            } else {
<<<<<<< HEAD
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
=======
                messages.add(Map.of("role", role, "content", enrichedContent));
            }
        }

        String userMessageFull = message;

        if (hasFile && fileText != null && !fileText.trim().isEmpty()) {
            // Tronquer à 6000 chars pour éviter la limite de tokens Groq (6000 TPM)
            String truncated = fileText.length() > 6000
                    ? fileText.substring(0, 6000) + "\n...[document tronqué]"
                    : fileText;
            String prompt = message.isEmpty()
                    ? "Analyse et résume ce document. ATTENTION: Si ce document n'a AUCUN rapport avec la formation, tu DOIS obligatoirement refuser l'analyse détaillée en expliquant que c'est hors-sujet."
                    : message + "\n\n(IMPORTANT: Avant de répondre, vérifie si ce document est hors-sujet par rapport à la formation. Si oui, refuse de répondre et signale-le obligatoirement)";
            userMessageFull = prompt + "\n\n[Document joint: " + fileName + "]\n" + truncated;
        } else if (hasFile && (fileData != null && !fileData.trim().isEmpty())) {
            String prompt = message.isEmpty()
                    ? "J'ai joint le document: " + fileName + ". ATTENTION: S'il te semble sans rapport avec la formation, refuse de l'analyser."
                    : message + "\n\n(IMPORTANT: L'analyse de contenu est soumise à la règle de hors-sujet)";
            userMessageFull = prompt + "\n\n[Document joint: " + fileName + " - le texte n'a pas pu être extrait]";
        }

        if (hasImage) {
            historyHasImage = true;
            String fallbackImgPrompt = "Analyse cette image. ATTENTION: Si cette image n'a AUCUN rapport avec la formation, tu DOIS obligatoirement refuser l'analyse détaillée en expliquant que c'est hors-sujet.";
            String txtToSend = userMessageFull.isEmpty() ? fallbackImgPrompt : userMessageFull + "\n\n(IMPORTANT: Avant d'analyser, vérifie si l'image est hors-sujet par rapport à la formation. Si oui, refuse de répondre et signale-le obligatoirement)";

            List<Map<String, Object>> contentParts = new ArrayList<>();
            contentParts.add(Map.of("type", "text", "text", txtToSend));
            contentParts.add(buildImageContent(imageUrl));
            messages.add(Map.of("role", "user", "content", contentParts));
        } else {
            messages.add(Map.of("role", "user", "content",
                    userMessageFull.isEmpty() ? "." : userMessageFull));
        }

>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
        String modelToUse = (hasImage || historyHasImage)
                ? "meta-llama/llama-4-scout-17b-16e-instruct"
                : "llama-3.1-8b-instant";

<<<<<<< HEAD
        // ── Appel GROQ API ───────────────────────────────────────────────
=======
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
        String requestBody = mapper.writeValueAsString(Map.of(
                "model",       modelToUse,
                "messages",    messages,
                "temperature", 0.7,
                "max_tokens",  1024
        ));

        System.out.println("=== GROQ REQUEST ===");
        System.out.println("Model: " + modelToUse);
<<<<<<< HEAD
        System.out.println("Has image: " + hasImage);
=======
        System.out.println("Has image: " + hasImage + " | Has file: " + hasFile);
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0

        HttpResponse<String> response = http.send(
                HttpRequest.newBuilder()
                        .uri(URI.create("https://api.groq.com/openai/v1/chat/completions"))
                        .header("Content-Type",  "application/json")
                        .header("Authorization", "Bearer " + groqApiKey)
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        System.out.println("=== GROQ RESPONSE STATUS: " + response.statusCode() + " ===");

        JsonNode root = mapper.readTree(response.body());

        if (root.has("error")) {
            String errMsg = root.path("error").path("message").asText();
            System.err.println("GROQ error: " + errMsg);
            return ResponseEntity.ok(Map.of(
                    "response", "Désolé, je rencontre un problème technique : " + errMsg
            ));
        }

        String reply = root.path("choices").get(0)
                .path("message").path("content").asText("Pas de réponse.");

<<<<<<< HEAD
        // ── Sauvegarder la réponse assistant en BDD ─────────────────────
=======
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
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

<<<<<<< HEAD
    // ✅ SEULE MODIFICATION : buildSystemPrompt mis à jour pour le multilangue
=======
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
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
<<<<<<< HEAD
=======
            - Analyze images shared by the learner IF they are related to the training
            - Analyze and summarize documents (PDF, Word) shared by the learner IF they are related to the training
            
            === CRITICAL OFF-TOPIC RULE ===
            You MUST strictly detect and refuse off-topic content:
            
            1. OFF-TOPIC QUESTIONS: If the user asks a question that has NO relation to the training
               "%s" (category: %s), politely refuse and redirect.
               - Example refusal (French): "Cette question est hors du sujet de la formation. Je suis ici pour vous aider uniquement sur les thèmes liés à **%s**. Posez-moi une question sur ce sujet !"
               - Example refusal (English): "This question is off-topic. I'm here to help you only with topics related to **%s**. Ask me something about the training!"
               - Example refusal (Arabic): "هذا السؤال خارج نطاق التكوين. أنا هنا فقط للمساعدة في مواضيع **%s**. اطرح سؤالاً يتعلق بالتكوين!"
            
            2. OFF-TOPIC IMAGES: If the user shares an image that has NO relation to the training
               (e.g., personal photos, random memes, unrelated screenshots), refuse politely:
               - Example: "L'image que vous avez partagée ne semble pas être liée à la formation **%s**. Partagez des captures d'écran ou images en lien avec les concepts du cours."
            
            3. OFF-TOPIC DOCUMENTS: If the user shares a document (PDF, Word, etc.) that has NO
               relation to the training (e.g., personal CV, unrelated articles), refuse politely:
               - Example: "Le document partagé ne semble pas être lié à la formation **%s**. Partagez des documents en rapport avec le cours pour que je puisse vous aider."
            
            IMPORTANT: Be SMART about relevance — if there is any reasonable connection to the
            training topic, answer helpfully. Only refuse clearly unrelated content.
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
            
            === CRITICAL LANGUAGE RULE ===
            You MUST detect the language of the user's message and reply
            in EXACTLY the same language.
            - If the user writes in French  → reply entirely in French
            - If the user writes in English → reply entirely in English
            - If the user writes in Arabic  → reply entirely in Arabic
            - Never mix languages in the same response
            - Never explain this rule to the user
            
<<<<<<< HEAD
=======
            === DOCUMENT HANDLING ===
            When a document is shared:
            - Summarize its key points if asked
            - Check if its content relates to the training topic
            - Answer questions about the document content in relation to the training
            - If the document is off-topic, refuse using the OFF-TOPIC DOCUMENTS rule above
            
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
            === FORMAT ===
            - Be concise and precise (max 300 words unless asked otherwise)
            - Use bullet points for clarity when appropriate
            - Bold (**) important terms
            - Use code blocks for code samples
            - Stay in the context of the training "%s"
<<<<<<< HEAD
            - If the question is off-topic, politely redirect to the training
            """,
                contextDesc, titre, categorie, niveau, titre
=======
            """,
                contextDesc, titre, categorie, niveau,
                titre, categorie, titre, titre, titre, titre, titre,
                titre
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
        );
    }
}