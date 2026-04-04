package t.esprit.arctic.jobmatch.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/proxy")
public class ProxyController {

    @GetMapping("/w3schools")
    public ResponseEntity<String> proxyW3Schools(@RequestParam("path") String path) {
        String targetUrl = "https://www.w3schools.com/" + path;
        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.getForEntity(targetUrl, String.class);
            
            String html = response.getBody();
            if (html != null && !html.isEmpty()) {
                // Injeter la balise <base> pour que les CSS, JS et images se chargent avec l'URL de base correcte
                if (html.toLowerCase().contains("<head>")) {
                    html = html.replaceFirst("(?i)<head>", "<head><base href=\"https://www.w3schools.com/\">");
                } else {
                    html = "<head><base href=\"https://www.w3schools.com/\"></head>" + html;
                }
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_HTML);
            
            return new ResponseEntity<>(html, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching content: " + e.getMessage());
        }
    }

    @GetMapping("/fetch")
    public ResponseEntity<String> proxyAnyUrl(@RequestParam("url") String url) {
        try {
            java.net.URL target = new java.net.URL(url);
            String baseUrl = target.getProtocol() + "://" + target.getHost();
            if (target.getPort() != -1) {
                baseUrl += ":" + target.getPort();
            }
            baseUrl += "/";

            RestTemplate restTemplate = new RestTemplate();
            
            // Simuler un navigateur pour éviter les blocages Anti-Bot
            org.springframework.http.HttpHeaders reqHeaders = new org.springframework.http.HttpHeaders();
            reqHeaders.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36");
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(reqHeaders);
            
            ResponseEntity<String> response = restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, entity, String.class);
            
            String html = response.getBody();
            if (html != null && !html.isEmpty()) {
                if (html.toLowerCase().contains("<head>")) {
                    html = html.replaceFirst("(?i)<head>", "<head><base href=\"" + baseUrl + "\">");
                } else {
                    html = "<head><base href=\"" + baseUrl + "\"></head>" + html;
                }
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_HTML);
            
            return new ResponseEntity<>(html, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Proxy Fetch Error: " + e.getMessage());
        }
    }
}
