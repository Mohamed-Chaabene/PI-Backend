package t.esprit.arctic.jobmatch.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

import t.esprit.arctic.jobmatch.entity.ChatbotHistory;
import t.esprit.arctic.jobmatch.repository.ChatbotHistoryRepository;

import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

@RestController
@RequestMapping("/api/chatbot")
@CrossOrigin(
        origins = "http://localhost:4200",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
                RequestMethod.DELETE, RequestMethod.OPTIONS}
)
public class ChatbotController {

    private static final Logger logger = LoggerFactory.getLogger(ChatbotController.class);

    static final String KEY_FILE_DATA     = "fileData";
    static final String KEY_FILE_TEXT     = "fileText";
    static final String KEY_FILE_NAME     = "fileName";
    static final String KEY_FILE_EXCERPT  = "fileExcerpt";
    static final String KEY_IMAGE_URL     = "imageUrl";
    static final String KEY_IMAGE_URL_OBJ = "image_url";
    static final String TYPE_IMAGE_URL    = "image_url";
    static final String KEY_SESSION_ID    = "sessionId";
    static final String KEY_SESSION_TITLE = "sessionTitle";
    static final String KEY_CANDIDAT_ID   = "candidatId";
    static final String KEY_FORMATION_ID  = "formationId";
    static final String KEY_MESSAGES      = "messages";
    static final String KEY_HISTORY       = "history";
    static final String KEY_MESSAGE       = "message";
    static final String KEY_CONTENT       = "content";
    static final String KEY_ERROR         = "error";
    static final String KEY_RESPONSE      = "response";
    static final String DATA_PREFIX       = "data:";
    static final String BASE64_SEPARATOR  = ";base64,";

    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL_VISION = "meta-llama/llama-4-scout-17b-16e-instruct";
    private static final String MODEL_TEXT   = "llama-3.1-8b-instant";

    private final ChatbotHistoryRepository chatHistoryRepo;
    private final HttpClient               http;
    private final ObjectMapper             mapper;

    @Value("${gemini.api.key:}")
    private String groqApiKey;

    public ChatbotController(ChatbotHistoryRepository chatHistoryRepo) {
        this.chatHistoryRepo = chatHistoryRepo;
        this.http   = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
    }

    @GetMapping("/history")
    public ResponseEntity<List<Map<String, Object>>> getHistory(
            @RequestParam Long candidatId,
            @RequestParam Long formationId) {

        List<ChatbotHistory> histories = chatHistoryRepo
                .findAllByCandidatIdAndFormationIdOrderByCreatedAtDesc(candidatId, formationId);

        List<Map<String, Object>> result = new ArrayList<>();
        for (ChatbotHistory h : histories) {
            result.add(toSessionMap(h));
        }
        return ResponseEntity.ok(result);
    }

    private Map<String, Object> toSessionMap(ChatbotHistory h) {
        Map<String, Object> sessionData = new HashMap<>();
        sessionData.put("id",              h.getId());
        sessionData.put(KEY_SESSION_ID,    h.getSessionId());
        sessionData.put(KEY_SESSION_TITLE, h.getSessionTitle());
        sessionData.put("createdAt",       h.getCreatedAt() != null ? h.getCreatedAt().toString() : null);
        sessionData.put(KEY_MESSAGES,      parseMessages(h));
        return sessionData;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, String>> parseMessages(ChatbotHistory h) {
        if (h.getHistoriqueJson() == null || h.getHistoriqueJson().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return mapper.readValue(h.getHistoriqueJson(), List.class);
        } catch (IOException e) {
            logger.error("Failed to parse historique JSON for session {}: {}", h.getSessionId(), e.getMessage());
            return new ArrayList<>();
        }
    }

    @PutMapping("/session/{sessionId}")
    public ResponseEntity<Map<String, Object>> updateSession(
            @PathVariable String sessionId,
            @RequestBody Map<String, Object> body) {

        Optional<ChatbotHistory> histOpt = chatHistoryRepo.findBySessionId(sessionId);
        if (!histOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        ChatbotHistory hist = histOpt.get();

        if (body.containsKey(KEY_SESSION_TITLE)) {
            hist.setSessionTitle(body.get(KEY_SESSION_TITLE).toString());
        }

        if (body.containsKey(KEY_MESSAGES)) {
            try {
                hist.setHistoriqueJson(mapper.writeValueAsString(body.get(KEY_MESSAGES)));
            } catch (IOException e) {
                logger.error("Failed to serialize messages for session {}: {}", sessionId, e.getMessage());
                Map<String, Object> err = new HashMap<>();
                err.put(KEY_ERROR, "Invalid messages format");
                return ResponseEntity.badRequest().body(err);
            }
        }

        chatHistoryRepo.save(hist);
        Map<String, Object> ok = new HashMap<>();
        ok.put(KEY_MESSAGE, "Session updated successfully");
        return ResponseEntity.ok(ok);
    }

    @Transactional
    @DeleteMapping("/session/{sessionId}")
    public ResponseEntity<Map<String, Object>> deleteSession(@PathVariable String sessionId) {
        try {
            Optional<ChatbotHistory> histOpt = chatHistoryRepo.findBySessionId(sessionId);
            if (histOpt.isPresent()) {
                chatHistoryRepo.deleteById(histOpt.get().getId());
                Map<String, Object> ok = new HashMap<>();
                ok.put(KEY_MESSAGE, "Session deleted successfully");
                return ResponseEntity.ok(ok);
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Error deleting session {}: {}", sessionId, e.getMessage(), e);
            Map<String, Object> err = new HashMap<>();
            err.put(KEY_ERROR, e.getMessage() != null ? e.getMessage() : e.toString());
            err.put("trace", Arrays.toString(e.getStackTrace()));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }
    }

    @PostMapping("/formation")
    public ResponseEntity<Map<String, String>> chat(
            @RequestBody Map<String, Object> body) throws IOException, InterruptedException {

        ChatRequest req = ChatRequest.from(body);

        if (req.message.isEmpty() && !req.hasImage && !req.hasFile) {
            Map<String, String> err = new HashMap<>();
            err.put(KEY_ERROR, "Message vide");
            return ResponseEntity.badRequest().body(err);
        }

        SessionContext            ctx        = resolveSession(req);
        List<Map<String, Object>> apiMessages = buildApiMessages(req, ctx);
        String reply = callGroqApi(req, apiMessages);

        return persistAndRespond(req, ctx, reply);
    }

    private static class ChatRequest {

        String  message;
        String  imageUrl;
        String  fileData;
        String  fileName;
        String  fileText;
        String  titreFormation;
        String  categorie;
        String  niveau;
        String  sessionId;
        Long    candidatId;
        Long    formationId;
        boolean hasImage;
        boolean hasFile;
        List<Map<String, Object>> historyFromFrontend;

        @SuppressWarnings("unchecked")
        static ChatRequest from(Map<String, Object> body) {
            ChatRequest r         = new ChatRequest();
            r.message             = body.getOrDefault(KEY_MESSAGE, "").toString();
            r.imageUrl            = getStr(body, KEY_IMAGE_URL);
            r.fileData            = getStr(body, KEY_FILE_DATA);
            r.fileName            = getStr(body, KEY_FILE_NAME);
            r.fileText            = getStr(body, KEY_FILE_TEXT);
            r.titreFormation      = body.getOrDefault("titreFormation", "").toString();
            r.categorie           = body.getOrDefault("categorie", "").toString();
            r.niveau              = body.getOrDefault("niveau", "").toString();
            r.sessionId           = getStr(body, KEY_SESSION_ID);
            r.candidatId          = getLng(body, KEY_CANDIDAT_ID);
            r.formationId         = getLng(body, KEY_FORMATION_ID);
            r.historyFromFrontend = body.containsKey(KEY_HISTORY) && body.get(KEY_HISTORY) != null
                    ? (List<Map<String, Object>>) body.get(KEY_HISTORY) : new ArrayList<>();
            r.hasImage = r.imageUrl != null && !r.imageUrl.trim().isEmpty();
            r.hasFile  = (r.fileData != null && !r.fileData.trim().isEmpty())
                    || (r.fileText != null && !r.fileText.trim().isEmpty());
            return r;
        }

        private static String getStr(Map<String, Object> b, String key) {
            return b.containsKey(key) && b.get(key) != null ? b.get(key).toString() : null;
        }

        private static Long getLng(Map<String, Object> b, String key) {
            return b.containsKey(key) && b.get(key) != null
                    ? Long.parseLong(b.get(key).toString()) : null;
        }
    }

    private static class SessionContext {
        ChatbotHistory            histDb;
        List<Map<String, Object>> dbHistoryList = new ArrayList<>();
    }

    private SessionContext resolveSession(ChatRequest req) {
        SessionContext ctx = new SessionContext();
        if (req.candidatId == null || req.formationId == null) {
            return ctx;
        }
        if (req.sessionId != null && !req.sessionId.isEmpty()) {
            ctx.histDb = chatHistoryRepo.findBySessionId(req.sessionId).orElse(null);
        }
        if (ctx.histDb == null) {
            ctx.histDb = createNewSession(req);
        } else {
            ctx.dbHistoryList = loadDbHistory(ctx.histDb);
        }
        return ctx;
    }

    private ChatbotHistory createNewSession(ChatRequest req) {
        ChatbotHistory h = new ChatbotHistory();
        h.setCandidatId(req.candidatId);
        h.setFormationId(req.formationId);
        h.setSessionId(UUID.randomUUID().toString());
        h.setCreatedAt(java.time.LocalDateTime.now());

        String titleSource;
        if (!req.message.isEmpty()) {
            titleSource = req.message;
        } else if (req.fileName != null) {
            titleSource = req.fileName;
        } else {
            titleSource = "Document";
        }

        String shortTitle = titleSource.length() > 30
                ? titleSource.substring(0, 30) + "..." : titleSource;
        h.setSessionTitle(shortTitle);
        return h;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> loadDbHistory(ChatbotHistory h) {
        if (h.getHistoriqueJson() == null || h.getHistoriqueJson().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return mapper.readValue(h.getHistoriqueJson(), List.class);
        } catch (IOException e) {
            logger.error("Failed to parse historique JSON: {}", e.getMessage());
            return new ArrayList<>();
        }
    }


    private List<Map<String, Object>> buildApiMessages(ChatRequest req, SessionContext ctx) {
        req.fileText = extractFileTextIfNeeded(req);
        appendUserMsgToHistory(req, ctx);

        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(buildSystemMessage(req));

        appendHistoryWindow(req, ctx, messages);
        appendCurrentUserTurn(req, messages);
        return messages;
    }

    private Map<String, Object> buildSystemMessage(ChatRequest req) {
        Map<String, Object> sysMsg = new HashMap<>();
        sysMsg.put("role", "system");
        sysMsg.put(KEY_CONTENT, buildSystemPrompt(req.titreFormation, req.categorie, req.niveau));
        return sysMsg;
    }


    private String extractFileTextIfNeeded(ChatRequest req) {
        boolean needsExtraction = (req.fileText == null || req.fileText.trim().isEmpty())
                && req.fileData != null && req.fileData.contains(BASE64_SEPARATOR);
        if (!needsExtraction) {
            return req.fileText;
        }
        try {
            String base64Data   = req.fileData.split(BASE64_SEPARATOR)[1];
            byte[] decodedBytes = Base64.getDecoder().decode(base64Data);
            return extractTextFromBytes(decodedBytes, req.fileName);
        } catch (IOException e) {
            logger.error("Backend document text extraction failed: {}", e.getMessage());
            return req.fileText;
        }
    }

    private String extractTextFromBytes(byte[] bytes, String fileName) throws IOException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes)) {
            if (fileName != null && fileName.toLowerCase().endsWith(".docx")) {
                try (XWPFDocument doc = new XWPFDocument(bis);
                     XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
                    return extractor.getText();
                }
            }
            if (fileName != null && fileName.toLowerCase().endsWith(".pdf")) {
                try (PDDocument doc = PDDocument.load(bis)) {
                    return new PDFTextStripper().getText(doc);
                }
            }
        }
        return null;
    }


    private void appendUserMsgToHistory(ChatRequest req, SessionContext ctx) {
        Map<String, Object> userMsg = new HashMap<>();
        userMsg.put("role",      "user");
        userMsg.put(KEY_CONTENT, req.message);
        if (req.hasImage) {
            userMsg.put(KEY_IMAGE_URL, sanitizeImageUrlForDb(req.imageUrl));
        }
        if (req.hasFile) {
            userMsg.put(KEY_FILE_NAME, req.fileName);
            if (req.fileText != null && !req.fileText.isEmpty()) {
                String excerpt = req.fileText.length() > 500
                        ? req.fileText.substring(0, 500) + "..." : req.fileText;
                userMsg.put(KEY_FILE_EXCERPT, excerpt);
            } else {
                userMsg.put(KEY_FILE_EXCERPT, sanitizeFileDataForDb(req.fileData, req.fileName));
            }
        }
        ctx.dbHistoryList.add(userMsg);
    }


    private void appendHistoryWindow(ChatRequest req, SessionContext ctx,
                                     List<Map<String, Object>> messages) {
        List<Map<String, Object>> source = (req.candidatId != null && req.formationId != null)
                ? ctx.dbHistoryList : req.historyFromFrontend;
        int total = (req.candidatId != null && req.formationId != null)
                ? source.size() - 1 : source.size();
        int start = Math.max(0, total - 8);
        for (int i = start; i < total; i++) {
            appendHistoryEntry(source.get(i), messages);
        }
    }

    private void appendHistoryEntry(Map<String, Object> h, List<Map<String, Object>> messages) {
        String role        = h.getOrDefault("role", "user").toString();
        String contentText = h.getOrDefault(KEY_CONTENT, "").toString();
        String imgUrl      = getStringOrNull(h, KEY_IMAGE_URL);

        boolean isPlaceholder = imgUrl != null && imgUrl.startsWith("[image:");
        boolean hasRealImg    = imgUrl != null && !imgUrl.trim().isEmpty() && !isPlaceholder;

        String fileExcerptHist = getStringOrNull(h, KEY_FILE_EXCERPT);
        String fileNameHist    = getStringOrNull(h, KEY_FILE_NAME);
        String enrichedContent = enrichWithFileExcerpt(contentText, fileExcerptHist, fileNameHist);

        Map<String, Object> entry = new HashMap<>();
        if (hasRealImg) {
            entry.put("role", role);
            List<Map<String, Object>> parts = new ArrayList<>();
            Map<String, Object> textPart = new HashMap<>();
            textPart.put("type", "text");
            textPart.put("text", enrichedContent);
            parts.add(textPart);
            parts.add(buildImageContent(imgUrl));
            entry.put(KEY_CONTENT, parts);
        } else {
            entry.put("role", role);
            entry.put(KEY_CONTENT, enrichedContent);
        }
        messages.add(entry);
    }

    private String enrichWithFileExcerpt(String contentText, String excerpt, String fileNameHist) {
        if (excerpt != null && !excerpt.startsWith("[file:")) {
            return contentText + "\n\n[Contenu du document '" + fileNameHist + "':\n" + excerpt + "]";
        }
        return contentText;
    }


    private void appendCurrentUserTurn(ChatRequest req, List<Map<String, Object>> messages) {
        String userText = buildUserMessageText(req);
        Map<String, Object> turn = new HashMap<>();
        if (req.hasImage) {
            String txtToSend = userText.isEmpty() ? "Analyse cette image." : userText;
            List<Map<String, Object>> parts = new ArrayList<>();
            Map<String, Object> textPart = new HashMap<>();
            textPart.put("type", "text");
            textPart.put("text", txtToSend);
            parts.add(textPart);
            parts.add(buildImageContent(req.imageUrl));
            turn.put("role", "user");
            turn.put(KEY_CONTENT, parts);
        } else {
            turn.put("role", "user");
            turn.put(KEY_CONTENT, userText.isEmpty() ? "." : userText);
        }
        messages.add(turn);
    }

    private String buildUserMessageText(ChatRequest req) {
        if (req.hasFile && req.fileText != null && !req.fileText.trim().isEmpty()) {
            String truncated = req.fileText.length() > 6000
                    ? req.fileText.substring(0, 6000) + "\n...[document tronqué]" : req.fileText;
            String prompt = req.message.isEmpty() ? "Analyse ce document." : req.message;
            return prompt
                    + "\n\n[CONTENU DU DOCUMENT JOINT '" + req.fileName + "']:\n" + truncated
                    + "\n\n[INSTRUCTION OBLIGATOIRE : Avant toute analyse, vérifie si ce document"
                    + " est lié à la formation. Si le document ne porte pas principalement sur les"
                    + " concepts, outils ou techniques de la formation, applique EXACTEMENT et"
                    + " UNIQUEMENT la règle TOPIC RELEVANCE RULE définie dans le system prompt."
                    + " N'analyse, ne résume et ne décris aucune partie d'un document hors-sujet.]";
        }
        if (req.hasFile && req.fileData != null && !req.fileData.trim().isEmpty()) {
            String prompt = req.message.isEmpty()
                    ? "J'ai joint le document: " + req.fileName : req.message;
            return prompt + "\n\n[Document joint: " + req.fileName
                    + " - le texte n'a pas pu être extrait]";
        }
        return req.message;
    }

    private String callGroqApi(ChatRequest req, List<Map<String, Object>> messages)
            throws IOException, InterruptedException {

        boolean usesVision = req.hasImage || historyContainsImage(messages);
        String modelToUse  = usesVision ? MODEL_VISION : MODEL_TEXT;

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("model",       modelToUse);
        requestMap.put(KEY_MESSAGES,  messages);
        requestMap.put("temperature", 0.1);
        requestMap.put("max_tokens",  1024);

        String requestBody = mapper.writeValueAsString(requestMap);

        logger.info("=== GROQ REQUEST === Model: {} | hasImage: {} | hasFile: {}",
                modelToUse, req.hasImage, req.hasFile);

        HttpResponse<String> httpResp = http.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(GROQ_API_URL))
                        .header("Content-Type",  "application/json")
                        .header("Authorization", "Bearer " + groqApiKey)
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        logger.info("=== GROQ RESPONSE STATUS: {} ===", httpResp.statusCode());

        JsonNode root = mapper.readTree(httpResp.body());
        if (root.has(KEY_ERROR)) {
            String errMsg = root.path(KEY_ERROR).path(KEY_MESSAGE).asText();
            logger.error("GROQ error: {}", errMsg);
            return "Désolé, je rencontre un problème technique : " + errMsg;
        }

        return root.path("choices").get(0)
                .path(KEY_MESSAGE).path(KEY_CONTENT).asText("Pas de réponse.");
    }

    private boolean historyContainsImage(List<Map<String, Object>> messages) {
        for (Map<String, Object> m : messages) {
            if (m.get(KEY_CONTENT) instanceof List) {
                return true;
            }
        }
        return false;
    }


    private ResponseEntity<Map<String, String>> persistAndRespond(
            ChatRequest req, SessionContext ctx, String reply) throws IOException {

        if (ctx.histDb == null || req.candidatId == null || req.formationId == null) {
            Map<String, String> res = new HashMap<>();
            res.put(KEY_RESPONSE, reply);
            return ResponseEntity.ok(res);
        }

        Map<String, Object> aiMsg = new HashMap<>();
        aiMsg.put("role",      "assistant");
        aiMsg.put(KEY_CONTENT, reply);
        ctx.dbHistoryList.add(aiMsg);
        ctx.histDb.setHistoriqueJson(mapper.writeValueAsString(ctx.dbHistoryList));
        chatHistoryRepo.save(ctx.histDb);

        Map<String, String> result = new HashMap<>();
        result.put(KEY_RESPONSE,      reply);
        result.put(KEY_SESSION_ID,    ctx.histDb.getSessionId());
        result.put(KEY_SESSION_TITLE, ctx.histDb.getSessionTitle());
        return ResponseEntity.ok(result);
    }


    private static String getStringOrNull(Map<String, Object> map, String key) {
        return map.containsKey(key) && map.get(key) != null ? map.get(key).toString() : null;
    }

    private Map<String, Object> buildImageContent(String imageUrl) {
        Map<String, Object> imgContent = new HashMap<>();
        imgContent.put("type", TYPE_IMAGE_URL);
        Map<String, Object> urlObj = new HashMap<>();

        if (imageUrl.startsWith("data:image")) {
            String mediaType  = "image/jpeg";
            String base64Data = imageUrl;
            if (imageUrl.contains(BASE64_SEPARATOR)) {
                String[] parts = imageUrl.split(BASE64_SEPARATOR);
                mediaType  = parts[0].replace(DATA_PREFIX, "");
                base64Data = parts[1];
            }
            urlObj.put("url", DATA_PREFIX + mediaType + BASE64_SEPARATOR + base64Data);
        } else {
            urlObj.put("url", imageUrl);
        }

        imgContent.put(KEY_IMAGE_URL_OBJ, urlObj);
        return imgContent;
    }

    private String sanitizeImageUrlForDb(String imageUrl) {
        if (imageUrl != null && imageUrl.startsWith("data:image")) {
            String mediaType = "image/jpeg";
            if (imageUrl.contains(BASE64_SEPARATOR)) {
                mediaType = imageUrl.split(BASE64_SEPARATOR)[0].replace(DATA_PREFIX, "");
            }
            return "[image:" + mediaType + "]";
        }
        return imageUrl;
    }

    private String sanitizeFileDataForDb(String fileData, String fileName) {
        if (fileData != null && fileData.startsWith(DATA_PREFIX)) {
            String name = (fileName != null && !fileName.isEmpty()) ? fileName : "document";
            return "[file:" + name + "]";
        }
        return fileData;
    }


    private String buildSystemPrompt(String titre, String categorie, String niveau) {
        return String.format("""
            You are a pedagogical AI assistant.

            Training: "%s"
            Category: %s
            Level: %s

            === TOPIC RELEVANCE RULE (HIGHEST PRIORITY) ===
            You must assist the user ONLY with topics directly related to the training "%s".
            This rule applies to ALL inputs: questions, images, and documents.

            HOW TO DETERMINE RELEVANCE:
            - Accept slight variations in names (e.g., "Power BI" is the same as "Power BIiii").
            - FOR QUESTIONS: Refuse any question that is not about "%s" concepts, tools, or techniques.
            - FOR IMAGES:
                * Check if the image shows content directly related to "%s".
                * If the image shows unrelated content (nature, people, generic objects, etc.): REFUSE.
            - FOR DOCUMENTS (STRICT):
                * Your FIRST action is to verify: does this document primarily discuss "%s"?
                * Read the beginning, middle, and any section titles to assess relevance.
                * If the document is about a DIFFERENT tool, language, framework, or domain: REFUSE.
                * If the document is a CV, cover letter, or generic text not about "%s": REFUSE.
                * If the document is a general or unrelated subject: REFUSE.
                * WHEN IN DOUBT → REFUSE. Only accept if the link to "%s" is explicit and clear.
                * Do NOT summarize, analyze, describe, or quote ANY part of an off-topic document.

            IF THE CONTENT IS OFF-TOPIC:
            - You MUST output ONLY the exact refusal sentence below, in the user's language.
            - Do NOT add any analysis, notes, partial summary, or suggestions.
            - Do NOT mention any content from the document or image.

            EXACT REFUSAL SENTENCES (use the one matching the user's language):
            - FRENCH:  "Cette image/question/document ne semble pas être liée à la formation **%s**. Je suis ici pour vous aider uniquement sur les thèmes liés à **%s**. Posez-moi une question sur ce sujet !"
            - ENGLISH: "This image/question/document does not seem to be related to the training **%s**. I am here to help you only with topics related to **%s**. Ask me a question about this subject!"
            - ARABIC:  "هذه الصورة/السؤال/المستند لا يبدو مرتبطًا بالتكوين **%s**. أنا هنا لمساعدتك فقط في المواضيع المتعلقة بـ **%s**. اطرح سؤالاً حول هذا الموضوع!"

            === CRITICAL LANGUAGE RULE ===
            Reply in the SAME language as the user's message.

            === FORMAT ===
            - Be concise and pedagogical.
            - Use bullet points if helpful.
            - Stay strictly within the context of the training "%s".
            """,
                // Line 1-3 : titre, categorie, niveau
                titre, categorie, niveau,
                // TOPIC RELEVANCE : 7 occurrences de titre
                titre, titre, titre, titre, titre, titre, titre,
                // IF OFF-TOPIC : 1 occurrence
                titre,
                // FRENCH refusal : 2 occurrences
                titre, titre,
                // ENGLISH refusal : 2 occurrences
                titre, titre,
                // ARABIC refusal : 2 occurrences
                titre, titre,
                // FORMAT : 1 occurrence
                titre
        );
    }
}