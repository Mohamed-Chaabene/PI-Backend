package t.esprit.arctic.jobmatch.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OffreSalairePredictionService {
    @Value("${flask.ml.url}")
    private String flaskUrl;

    public String predictSalaire(String titre, String description, String entreprise, String location, String typeContrat, List<String> competences) {
        Map<String, Object> body = new HashMap<>();
        body.put("titre", titre);
        body.put("description", description);
        body.put("entreprise", entreprise);
        body.put("location", location);
        body.put("typeContrat", typeContrat);
        body.put("competences", competences);

        RestTemplate rest = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> req = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = rest.postForEntity(
                flaskUrl + "/predict-salary", req, Map.class
        );
        Map<String, Object> resBody = response.getBody();
        if (resBody != null && resBody.containsKey("predicted_salary")) {
            return resBody.get("predicted_salary").toString();
        }
        return null;
    }
}
