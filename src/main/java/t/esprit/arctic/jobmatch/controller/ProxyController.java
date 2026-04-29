package t.esprit.arctic.jobmatch.controller;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@RestController
@RequestMapping("/api/proxy")
@CrossOrigin(origins = "http://localhost:4200")
public class ProxyController {

    @GetMapping("/fetch")
    public ResponseEntity<String> proxyAnyUrl(@RequestParam("url") String url) {
        if (url == null || (!url.startsWith("https://") && !url.startsWith("http://"))) {
            return ResponseEntity.badRequest().body("URL invalide");
        }

        try {
            URI target = new URI(url);
            String baseUrl = target.getScheme() + "://" + target.getHost() + "/";

            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders reqHeaders = new HttpHeaders();
            reqHeaders.set("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                            "AppleWebKit/537.36 (KHTML, like Gecko) " +
                            "Chrome/120.0.0.0 Safari/537.36");
            reqHeaders.set("Accept",
                    "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            reqHeaders.set("Accept-Language", "fr,en;q=0.5");

            HttpEntity<String> entity = new HttpEntity<>(reqHeaders);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, String.class
            );

            String html = response.getBody();
            if (html == null) {
                return ResponseEntity.noContent().build();
            }

            if (html.toLowerCase().contains("<head>")) {
                html = html.replaceFirst("(?i)<head>",
                        "<head><base href=\"" + baseUrl + "\">");
            } else {
                html = "<head><base href=\"" + baseUrl + "\"></head>" + html;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_HTML);

            return new ResponseEntity<>(html, headers, HttpStatus.OK);

        } catch (Exception e) {
            String safeUrl = url.replaceAll("[<>\"']", "");

            String fallbackHtml =
                    "<div style='padding:30px;text-align:center;font-family:sans-serif'>" +
                            "<p style='color:#6b7280'>Ce contenu ne peut pas être affiché directement.</p>" +
                            "<a href='" + safeUrl + "' target='_blank' " +
                            "style='color:#0965A4;font-weight:600'>" +
                            "Ouvrir dans un nouvel onglet &#x2192;</a></div>";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_HTML);

            return new ResponseEntity<>(fallbackHtml, headers, HttpStatus.BAD_GATEWAY);
        }
    }
}