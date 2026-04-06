package t.esprit.arctic.jobmatch.controller;

import t.esprit.arctic.jobmatch.dto.FormationSuggestion;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/api/suggestions")
@CrossOrigin(origins = "http://localhost:4200")
public class SuggestionController {

    @Value("${youtube.api.key}")
    private String youtubeApiKey;

    @Value("${google.search.api.key}")
    private String googleApiKey;

    @Value("${google.search.cx}")
    private String googleCx;

    private final HttpClient  http   = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    // ══════════════════════════════════════════════════════════════════
    // 1. PLAYLISTS YOUTUBE
    // ══════════════════════════════════════════════════════════════════
    @GetMapping("/formations")
    public ResponseEntity<List<FormationSuggestion>> suggest(
            @RequestParam String titre) throws Exception {

        String query = URLEncoder.encode(
                titre + " cours complet tutoriel", StandardCharsets.UTF_8);

        String ytUrl = "https://www.googleapis.com/youtube/v3/search"
                + "?part=snippet&type=playlist&relevanceLanguage=fr"
                + "&maxResults=5&order=relevance&q=" + query
                + "&key=" + youtubeApiKey;

        HttpResponse<String> response = http.send(
                HttpRequest.newBuilder().uri(URI.create(ytUrl))
                        .header("Accept", "application/json").build(),
                HttpResponse.BodyHandlers.ofString()
        );

        JsonNode root = mapper.readTree(response.body());

        if (root.has("error")) {
            return ResponseEntity.ok(fallbackSuggestion(titre));
        }

        List<FormationSuggestion> suggestions = new ArrayList<>();
        for (JsonNode item : root.path("items")) {
            String playlistId  = item.path("id").path("playlistId").asText();
            String videoTitle  = item.path("snippet").path("title").asText();
            String thumbnail   = item.path("snippet")
                    .path("thumbnails").path("medium").path("url").asText();
            String channelName = item.path("snippet").path("channelTitle").asText();

            if (playlistId.isEmpty() || playlistId.equals("null")) continue;

            int nbVideos = getPlaylistVideoCount(playlistId);
            if (nbVideos < 3) continue;

            suggestions.add(new FormationSuggestion(
                    playlistId, videoTitle, thumbnail, channelName,
                    "", detectCategorie(titre), detectNiveau(titre), nbVideos
            ));
            if (suggestions.size() >= 3) break;
        }
        return ResponseEntity.ok(suggestions);
    }

    // ══════════════════════════════════════════════════════════════════
    // 2. VIDEOS D'UNE PLAYLIST
    // ══════════════════════════════════════════════════════════════════
    @GetMapping("/playlist-videos/{playlistId}")
    public ResponseEntity<List<JsonNode>> getPlaylistVideos(
            @PathVariable String playlistId) throws Exception {

        String ytUrl = "https://www.googleapis.com/youtube/v3/playlistItems"
                + "?part=snippet&maxResults=50&playlistId=" + playlistId
                + "&key=" + youtubeApiKey;

        HttpResponse<String> response = http.send(
                HttpRequest.newBuilder().uri(URI.create(ytUrl))
                        .header("Accept", "application/json").build(),
                HttpResponse.BodyHandlers.ofString()
        );

        JsonNode root = mapper.readTree(response.body());
        if (root.has("error")) return ResponseEntity.ok(new ArrayList<>());

        List<JsonNode> videos = new ArrayList<>();
        int position = 1;
        for (JsonNode item : root.path("items")) {
            String videoId = item.path("snippet")
                    .path("resourceId").path("videoId").asText();
            String title   = item.path("snippet").path("title").asText();

            if ("Private video".equals(title) || "Deleted video".equals(title)) continue;

            String thumbnail = item.path("snippet")
                    .path("thumbnails").path("medium").path("url").asText("");
            if (thumbnail.isEmpty()) {
                thumbnail = item.path("snippet")
                        .path("thumbnails").path("default").path("url").asText("");
            }

            com.fasterxml.jackson.databind.node.ObjectNode v =
                    mapper.createObjectNode();
            v.put("videoId",   videoId);
            v.put("title",     title);
            v.put("thumbnail", thumbnail);
            v.put("position",  position++);
            videos.add(v);
        }
        return ResponseEntity.ok(videos);
    }

    // ══════════════════════════════════════════════════════════════════
    // 3. DOCUMENTATION AUTOMATIQUE — Google Search
    // ══════════════════════════════════════════════════════════════════
    @GetMapping("/docs/auto")
    public ResponseEntity<Map<String, Object>> findDocAuto(
            @RequestParam String titre) throws Exception {

        List<Map<String, String>> results = new ArrayList<>();

        try {
            // ── DuckDuckGo Instant Answer API — gratuit, sans clé ─────
            String query = URLEncoder.encode(
                titre + " documentation tutorial",
                StandardCharsets.UTF_8
            );

            String ddgUrl = "https://api.duckduckgo.com/?q=" + query
                + "&format=json&no_html=1&skip_disambig=1";

            HttpResponse<String> ddgResp = http.send(
                HttpRequest.newBuilder()
                    .uri(URI.create(ddgUrl))
                    .header("User-Agent", "Mozilla/5.0")
                    .build(),
                HttpResponse.BodyHandlers.ofString()
            );

            JsonNode ddg = mapper.readTree(ddgResp.body());

            // Résultats DuckDuckGo
            JsonNode relatedTopics = ddg.path("RelatedTopics");
            if (relatedTopics.isArray()) {
                for (JsonNode topic : relatedTopics) {
                    String url  = topic.path("FirstURL").asText("");
                    String text = topic.path("Text").asText("");
                    if (url.isEmpty() || text.isEmpty()) continue;

                    Map<String, String> r = new HashMap<>();
                    r.put("title",   text.length() > 80
                        ? text.substring(0, 80) + "..." : text);
                    r.put("url",     url);
                    r.put("snippet", text);
                    r.put("source",  extractDomain(url));
                    r.put("type",    "proxy");
                    results.add(r);
                    if (results.size() >= 3) break;
                }
            }

            // ── Fallback : URLs directes selon le titre ────────────────
            if (results.isEmpty()) {
                results.addAll(buildDirectUrls(titre));
            }

        } catch (Exception e) {
            System.err.println("Search error: " + e.getMessage());
            results.addAll(buildDirectUrls(titre));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("results", results);
        result.put("query",   titre);
        return ResponseEntity.ok(result);
    }

    // ── URLs directes selon mots-clés du titre ──────────────────────
    private List<Map<String, String>> buildDirectUrls(String titre) {
        List<Map<String, String>> results = new ArrayList<>();
        String t = titre.toLowerCase();

        // Mapping titre → URL documentation officielle directe
        String url  = null;
        String name = null;

        if (t.contains("angular")) {
            url = "https://angular.io/docs"; name = "angular.io"; }
        else if (t.contains("react")) {
            url = "https://react.dev/learn"; name = "react.dev"; }
        else if (t.contains("vue")) {
            url = "https://vuejs.org/guide/introduction.html"; name = "vuejs.org"; }
        else if (t.contains("python")) {
            url = "https://docs.python.org/fr/3/tutorial/index.html"; name = "docs.python.org"; }
        else if (t.contains("javascript") || t.contains("js")) {
            url = "https://developer.mozilla.org/fr/docs/Web/JavaScript/Guide"; name = "MDN"; }
        else if (t.contains("html")) {
            url = "https://developer.mozilla.org/fr/docs/Learn/HTML"; name = "MDN"; }
        else if (t.contains("css")) {
            url = "https://developer.mozilla.org/fr/docs/Learn/CSS"; name = "MDN"; }
        else if (t.contains("node")) {
            url = "https://nodejs.org/fr/docs"; name = "nodejs.org"; }
        else if (t.contains("spring")) {
            url = "https://spring.io/guides"; name = "spring.io"; }
        else if (t.contains("docker")) {
            url = "https://docs.docker.com/get-started/"; name = "docs.docker.com"; }
        else if (t.contains("kubernetes") || t.contains("k8s")) {
            url = "https://kubernetes.io/fr/docs/home/"; name = "kubernetes.io"; }
        else if (t.contains("php")) {
            url = "https://www.php.net/manual/fr/"; name = "php.net"; }
        else if (t.contains("laravel")) {
            url = "https://laravel.com/docs"; name = "laravel.com"; }
        else if (t.contains("django")) {
            url = "https://docs.djangoproject.com/fr/"; name = "djangoproject.com"; }
        else if (t.contains("flutter")) {
            url = "https://docs.flutter.dev/get-started/codelab"; name = "docs.flutter.dev"; }
        else if (t.contains("kotlin")) {
            url = "https://kotlinlang.org/docs/getting-started.html"; name = "kotlinlang.org"; }
        else if (t.contains("swift")) {
            url = "https://docs.swift.org/swift-book/"; name = "swift.org"; }
        else if (t.contains("rust")) {
            url = "https://doc.rust-lang.org/book/"; name = "rust-lang.org"; }
        else if (t.contains("go") || t.contains("golang")) {
            url = "https://go.dev/doc/"; name = "go.dev"; }
        else if (t.contains("c++") || t.contains("cpp")) {
            url = "https://www.cplusplus.com/doc/tutorial/"; name = "cplusplus.com"; }
        else if (t.contains("java") && !t.contains("javascript")) {
            url = "https://dev.java/learn/"; name = "dev.java"; }
        else if (t.contains("typescript")) {
            url = "https://www.typescriptlang.org/docs/"; name = "typescriptlang.org"; }
        else if (t.contains("sql") || t.contains("mysql")) {
            url = "https://dev.mysql.com/doc/refman/8.0/en/tutorial.html"; name = "mysql.com"; }
        else if (t.contains("mongodb")) {
            url = "https://www.mongodb.com/docs/manual/tutorial/"; name = "mongodb.com"; }
        else if (t.contains("tensorflow")) {
            url = "https://www.tensorflow.org/tutorials"; name = "tensorflow.org"; }
        else if (t.contains("pandas")) {
            url = "https://pandas.pydata.org/docs/getting_started/index.html"; name = "pandas.pydata.org"; }
        else if (t.contains("power bi") || t.contains("powerbi")) {
            url = "https://learn.microsoft.com/fr-fr/power-bi/"; name = "learn.microsoft.com"; }
        else if (t.contains("aws")) {
            url = "https://docs.aws.amazon.com/"; name = "docs.aws.amazon.com"; }
        else if (t.contains("git")) {
            url = "https://git-scm.com/doc"; name = "git-scm.com"; }
        else if (t.contains("linux") || t.contains("bash")) {
            url = "https://www.gnu.org/software/bash/manual/"; name = "gnu.org"; }
        else {
            // Fallback absolu : MDN
            url  = "https://developer.mozilla.org/fr/docs/Learn";
            name = "MDN Web Docs";
        }

        Map<String, String> r = new HashMap<>();
        r.put("title",   titre + " — Documentation officielle");
        r.put("url",     url);
        r.put("snippet", "Documentation officielle pour " + titre);
        r.put("source",  name);
        r.put("type",    "proxy");
        results.add(r);

        return results;
    }

    // ══════════════════════════════════════════════════════════════════
    // UTILITAIRES PRIVÉS
    // ══════════════════════════════════════════════════════════════════
    private int getPlaylistVideoCount(String playlistId) {
        try {
            String url = "https://www.googleapis.com/youtube/v3/playlists"
                    + "?part=contentDetails&id=" + playlistId
                    + "&key=" + youtubeApiKey;
            HttpResponse<String> resp = http.send(
                    HttpRequest.newBuilder().uri(URI.create(url)).build(),
                    HttpResponse.BodyHandlers.ofString()
            );
            JsonNode root  = mapper.readTree(resp.body());
            JsonNode items = root.path("items");
            if (items.isArray() && items.size() > 0)
                return items.get(0).path("contentDetails")
                        .path("itemCount").asInt(0);
        } catch (Exception e) { return 10; }
        return 0;
    }

    private String extractDomain(String url) {
        try {
            java.net.URL u = new java.net.URL(url);
            return u.getHost().replace("www.", "");
        } catch (Exception e) { return url; }
    }

    private List<FormationSuggestion> fallbackSuggestion(String titre) {
        List<FormationSuggestion> list = new ArrayList<>();
        list.add(new FormationSuggestion(
                "", "Quota YouTube dépassé — saisir manuellement",
                "https://cdn-icons-png.flaticon.com/512/376/376048.png",
                "Système", "", detectCategorie(titre), detectNiveau(titre), 0
        ));
        return list;
    }

    private String detectCategorie(String titre) {
        String t = titre.toLowerCase();
        if (t.contains("react") || t.contains("angular") || t.contains("vue") ||
                t.contains("html") || t.contains("css") || t.contains("javascript"))
            return "Frontend";
        if (t.contains("spring") || t.contains("node") || t.contains("django") ||
                t.contains("laravel") || t.contains("php") || t.contains("java"))
            return "Backend";
        if (t.contains("docker") || t.contains("kubernetes") ||
                t.contains("aws") || t.contains("devops") || t.contains("linux"))
            return "DevOps";
        if (t.contains("machine learning") || t.contains("deep learning") ||
                t.contains("tensorflow") || t.contains("ia") ||
                t.contains("intelligence"))
            return "IA";
        if (t.contains("pandas") || t.contains("sql") ||
                t.contains("data") || t.contains("power bi"))
            return "Data";
        if (t.contains("figma") || t.contains("ux") || t.contains("design"))
            return "Design";
        if (t.contains("flutter") || t.contains("android") ||
                t.contains("ios") || t.contains("swift") || t.contains("kotlin"))
            return "Mobile";
        return "Développement";
    }

    private String detectNiveau(String titre) {
        String t = titre.toLowerCase();
        if (t.contains("débutant") || t.contains("initiation") ||
                t.contains("introduction") || t.contains("bases") ||
                t.contains("beginner") || t.contains("zéro"))
            return "Débutant";
        if (t.contains("avancé") || t.contains("expert") ||
                t.contains("master") || t.contains("advanced"))
            return "Avancé";
        return "Intermédiaire";
    }
}