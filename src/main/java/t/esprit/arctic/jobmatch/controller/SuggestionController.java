package t.esprit.arctic.jobmatch.controller;

import t.esprit.arctic.jobmatch.dto.FormationSuggestion;
import t.esprit.arctic.jobmatch.dto.DocSuggestion;
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

    @Value("${github.token:}")
    private String githubToken;

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    // ══════════════════════════════════════════════════════════
    // 1. SUGGESTIONS PLAYLISTS YOUTUBE
    // ══════════════════════════════════════════════════════════
    @GetMapping("/formations")
    public ResponseEntity<List<FormationSuggestion>> suggest(
            @RequestParam String titre) throws Exception {

        String query = URLEncoder.encode(titre + " cours complet tutoriel", StandardCharsets.UTF_8);

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
            String thumbnail   = item.path("snippet").path("thumbnails").path("medium").path("url").asText();
            String channelName = item.path("snippet").path("channelTitle").asText();

            if (playlistId.isEmpty() || playlistId.equals("null")) continue;

            int nbVideos = getPlaylistVideoCount(playlistId);
            if (nbVideos < 3) continue;

            suggestions.add(new FormationSuggestion(
                    playlistId, videoTitle, thumbnail, channelName,
                    "", // writtenUrl vide — l'admin choisit la source doc séparément
                    detectCategorie(titre), detectNiveau(titre), nbVideos
            ));
            if (suggestions.size() >= 3) break;
        }
        return ResponseEntity.ok(suggestions);
    }

    // ══════════════════════════════════════════════════════════
    // 2. SUGGESTIONS DOCUMENTATION — 3 sources fonctionnelles
    // ══════════════════════════════════════════════════════════

    // Source 1 : DevDocs.io API — retourne JSON, rendu dans l'app
    @GetMapping("/docs/devdocs")
    public ResponseEntity<List<DocSuggestion>> searchDevDocs(
            @RequestParam String titre) throws Exception {

        // DevDocs liste toutes les entrées disponibles
        // On filtre selon le titre pour trouver la doc la plus pertinente
        String url = "https://devdocs.io/docs.json";
        HttpResponse<String> response = http.send(
                HttpRequest.newBuilder().uri(URI.create(url)).build(),
                HttpResponse.BodyHandlers.ofString()
        );

        JsonNode docs = mapper.readTree(response.body());
        List<DocSuggestion> results = new ArrayList<>();
        String titreLower = titre.toLowerCase();

        for (JsonNode doc : docs) {
            String name = doc.path("name").asText().toLowerCase();
            String slug = doc.path("slug").asText();
            if (name.contains(titreLower) || titreLower.contains(name)) {
                results.add(new DocSuggestion(
                        slug,
                        doc.path("name").asText(),
                        "DevDocs.io",
                        "devdocs",
                        "https://devdocs.io/" + slug
                ));
                if (results.size() >= 3) break;
            }
        }

        // Fallback : chercher par catégorie
        if (results.isEmpty()) {
            String cat = detectCategorie(titre).toLowerCase();
            for (JsonNode doc : docs) {
                String name = doc.path("name").asText().toLowerCase();
                String slug = doc.path("slug").asText();
                if (name.contains(cat) || cat.contains(name.split(" ")[0])) {
                    results.add(new DocSuggestion(
                            slug,
                            doc.path("name").asText(),
                            "DevDocs.io",
                            "devdocs",
                            "https://devdocs.io/" + slug
                    ));
                    if (results.size() >= 3) break;
                }
            }
        }
        return ResponseEntity.ok(results);
    }

    // Source 2 : dev.to API — articles tech gratuits
    @GetMapping("/docs/devto")
    public ResponseEntity<List<DocSuggestion>> searchDevTo(
            @RequestParam String titre) throws Exception {

        String query = URLEncoder.encode(titre, StandardCharsets.UTF_8);
        String url = "https://dev.to/api/articles?per_page=5&tag=" + query + "&top=1";

        HttpResponse<String> response = http.send(
                HttpRequest.newBuilder().uri(URI.create(url))
                        .header("Accept", "application/json").build(),
                HttpResponse.BodyHandlers.ofString()
        );

        JsonNode articles = mapper.readTree(response.body());
        List<DocSuggestion> results = new ArrayList<>();

        if (articles.isArray()) {
            for (JsonNode article : articles) {
                String articleId = article.path("id").asText();
                String title     = article.path("title").asText();
                String articleUrl= article.path("url").asText();
                String cover     = article.path("cover_image").asText("");

                results.add(new DocSuggestion(
                        articleId, title, "dev.to", "devto", articleUrl
                ));
                if (results.size() >= 3) break;
            }
        }

        // Si pas de résultats avec tag, chercher avec search
        if (results.isEmpty()) {
            String searchUrl = "https://dev.to/api/articles?per_page=5&"
                    + "search=" + URLEncoder.encode(titre + " tutorial", StandardCharsets.UTF_8);
            HttpResponse<String> r2 = http.send(
                    HttpRequest.newBuilder().uri(URI.create(searchUrl))
                            .header("Accept", "application/json").build(),
                    HttpResponse.BodyHandlers.ofString()
            );
            JsonNode a2 = mapper.readTree(r2.body());
            if (a2.isArray()) {
                for (JsonNode article : a2) {
                    results.add(new DocSuggestion(
                            article.path("id").asText(),
                            article.path("title").asText(),
                            "dev.to", "devto",
                            article.path("url").asText()
                    ));
                    if (results.size() >= 3) break;
                }
            }
        }
        return ResponseEntity.ok(results);
    }

    // Source 3 : GitHub — cours open source (freeCodeCamp, The Odin Project...)
    @GetMapping("/docs/github")
    public ResponseEntity<List<DocSuggestion>> searchGithub(
            @RequestParam String titre) throws Exception {

        String query = URLEncoder.encode(titre + " course tutorial", StandardCharsets.UTF_8);
        String url = "https://api.github.com/search/repositories"
                + "?q=" + query + "+topic:tutorial&sort=stars&per_page=5";

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/vnd.github.v3+json");

        if (!githubToken.isEmpty()) {
            builder.header("Authorization", "Bearer " + githubToken);
        }

        HttpResponse<String> response = http.send(
                builder.build(), HttpResponse.BodyHandlers.ofString()
        );

        JsonNode root = mapper.readTree(response.body());
        List<DocSuggestion> results = new ArrayList<>();

        for (JsonNode repo : root.path("items")) {
            String repoFullName = repo.path("full_name").asText();
            String repoName     = repo.path("name").asText();
            String description  = repo.path("description").asText("Cours open source");
            int stars           = repo.path("stargazers_count").asInt();

            // Seulement les repos populaires avec plus de 1000 étoiles
            if (stars < 1000) continue;

            results.add(new DocSuggestion(
                    repoFullName,
                    repoName + " (" + stars / 1000 + "k ★)",
                    "GitHub",
                    "github",
                    "https://github.com/" + repoFullName
            ));
            if (results.size() >= 3) break;
        }
        return ResponseEntity.ok(results);
    }

    // ══════════════════════════════════════════════════════════
    // 3. CONTENU DE DOCUMENTATION — rendu dans l'app
    // ══════════════════════════════════════════════════════════

// Dans SuggestionController.java — remplacer getDevDocsContent

    @GetMapping("/docs/devdocs/content")
    public ResponseEntity<String> getDevDocsContent(
            @RequestParam String slug) {
        try {
            // DevDocs index.json peut être très lourd — on récupère juste les entrées
            String url = "https://devdocs.io/docs/" + slug + "/index.json";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .header("User-Agent", "Mozilla/5.0")
                    .build();

            HttpResponse<String> response = http.send(
                    request, HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) {
                // Retourner un JSON minimal si DevDocs refuse
                return ResponseEntity.ok(
                        "{\"entries\":[{\"name\":\"" + slug + "\",\"type\":\"guide\"}]}"
                );
            }

            // Limiter la taille — prendre seulement les 50 premières entrées
            JsonNode root = mapper.readTree(response.body());
            JsonNode entries = root.path("entries");

            com.fasterxml.jackson.databind.node.ObjectNode result = mapper.createObjectNode();
            com.fasterxml.jackson.databind.node.ArrayNode limited = mapper.createArrayNode();

            int count = 0;
            if (entries.isArray()) {
                for (JsonNode entry : entries) {
                    limited.add(entry);
                    if (++count >= 50) break;
                }
            }
            result.set("entries", limited);
            result.put("slug", slug);

            return ResponseEntity.ok()
                    .header("Content-Type", "application/json")
                    .body(mapper.writeValueAsString(result));

        } catch (Exception e) {
            System.err.println("DevDocs error: " + e.getMessage());
            return ResponseEntity.ok(
                    "{\"entries\":[{\"name\":\"Documentation\",\"type\":\"guide\"}],\"slug\":\"" + slug + "\"}"
            );
        }
    }

    // Récupérer un article dev.to complet en HTML
    @GetMapping("/docs/devto/content")
    public ResponseEntity<String> getDevToContent(
            @RequestParam String articleId) throws Exception {

        String url = "https://dev.to/api/articles/" + articleId;
        HttpResponse<String> response = http.send(
                HttpRequest.newBuilder().uri(URI.create(url))
                        .header("Accept", "application/json").build(),
                HttpResponse.BodyHandlers.ofString()
        );

        JsonNode article = mapper.readTree(response.body());
        // body_html contient le HTML de l'article complet
        String bodyHtml = article.path("body_html").asText("");
        String title    = article.path("title").asText("");
        String cover    = article.path("cover_image").asText("");

        // Construire un HTML propre
        String html = "<h1>" + title + "</h1>"
                + (cover.isEmpty() ? "" : "<img src='" + cover + "' style='width:100%;border-radius:8px;margin-bottom:1rem'>")
                + bodyHtml;

        return ResponseEntity.ok()
                .header("Content-Type", "text/html; charset=UTF-8")
                .body(html);
    }

    // Récupérer le README d'un repo GitHub en HTML
    @GetMapping("/docs/github/content")
    public ResponseEntity<String> getGithubContent(
            @RequestParam String repo) throws Exception {

        String url = "https://api.github.com/repos/" + repo + "/readme";
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/vnd.github.v3.html"); // GitHub retourne HTML directement

        if (!githubToken.isEmpty()) {
            builder.header("Authorization", "Bearer " + githubToken);
        }

        HttpResponse<String> response = http.send(
                builder.build(), HttpResponse.BodyHandlers.ofString()
        );
        return ResponseEntity.ok()
                .header("Content-Type", "text/html; charset=UTF-8")
                .body(response.body());
    }

    // ══════════════════════════════════════════════════════════
    // 4. VIDEOS D'UNE PLAYLIST YOUTUBE
    // ══════════════════════════════════════════════════════════
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
            String videoId  = item.path("snippet").path("resourceId").path("videoId").asText();
            String title    = item.path("snippet").path("title").asText();
            if ("Private video".equals(title) || "Deleted video".equals(title)) continue;

            String thumbnail = item.path("snippet").path("thumbnails").path("medium").path("url").asText("");
            if (thumbnail.isEmpty())
                thumbnail = item.path("snippet").path("thumbnails").path("default").path("url").asText("");

            com.fasterxml.jackson.databind.node.ObjectNode v = mapper.createObjectNode();
            v.put("videoId", videoId);
            v.put("title", title);
            v.put("thumbnail", thumbnail);
            v.put("position", position++);
            videos.add(v);
        }
        return ResponseEntity.ok(videos);
    }

    // ══════════════════════════════════════════════════════════
    // UTILITAIRES PRIVÉS
    // ══════════════════════════════════════════════════════════
    private int getPlaylistVideoCount(String playlistId) {
        try {
            String url = "https://www.googleapis.com/youtube/v3/playlists"
                    + "?part=contentDetails&id=" + playlistId + "&key=" + youtubeApiKey;
            HttpResponse<String> resp = http.send(
                    HttpRequest.newBuilder().uri(URI.create(url)).build(),
                    HttpResponse.BodyHandlers.ofString()
            );
            JsonNode root = mapper.readTree(resp.body());
            JsonNode items = root.path("items");
            if (items.isArray() && items.size() > 0)
                return items.get(0).path("contentDetails").path("itemCount").asInt(0);
        } catch (Exception e) { return 10; }
        return 0;
    }

    private List<FormationSuggestion> fallbackSuggestion(String titre) {
        List<FormationSuggestion> fallback = new ArrayList<>();
        fallback.add(new FormationSuggestion(
                "", "Quota YouTube dépassé — saisir manuellement",
                "https://cdn-icons-png.flaticon.com/512/376/376048.png",
                "Système", "", detectCategorie(titre), detectNiveau(titre), 0
        ));
        return fallback;
    }

    private String detectCategorie(String titre) {
        String t = titre.toLowerCase();
        if (t.contains("react") || t.contains("angular") || t.contains("vue") ||
                t.contains("html") || t.contains("css") || t.contains("javascript"))  return "Frontend";
        if (t.contains("spring") || t.contains("node") || t.contains("django") ||
                t.contains("laravel") || t.contains("php") || t.contains("java"))     return "Backend";
        if (t.contains("docker") || t.contains("kubernetes") ||
                t.contains("aws") || t.contains("devops"))                            return "DevOps";
        if (t.contains("machine learning") || t.contains("deep learning") ||
                t.contains("tensorflow") || t.contains("ia"))                         return "IA";
        if (t.contains("pandas") || t.contains("sql") || t.contains("data"))     return "Data";
        if (t.contains("figma") || t.contains("ux") || t.contains("design"))     return "Design";
        if (t.contains("flutter") || t.contains("android") || t.contains("ios")) return "Mobile";
        return "Développement";
    }

    private String detectNiveau(String titre) {
        String t = titre.toLowerCase();
        if (t.contains("débutant") || t.contains("initiation") ||
                t.contains("introduction") || t.contains("bases") ||
                t.contains("beginner"))                             return "Débutant";
        if (t.contains("avancé") || t.contains("expert") ||
                t.contains("master") || t.contains("advanced"))    return "Avancé";
        return "Intermédiaire";
    }
}