package t.esprit.arctic.jobmatch.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import t.esprit.arctic.jobmatch.entity.Formation;
import t.esprit.arctic.jobmatch.entity.Candidat;
import t.esprit.arctic.jobmatch.entity.Competence;
import t.esprit.arctic.jobmatch.entity.InscriptionFormation;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
public class RecommendationMLClient {

    private final String ML_SERVICE_URL = "http://localhost:5000/recommend";
    private final RestTemplate restTemplate;

    public RecommendationMLClient() {
        this.restTemplate = new RestTemplate();
    }

    public List<Map<String, Object>> getRecommendations(Candidat candidat, List<Formation> formationsDisponibles) {
        
        // 1. Préparer les compétences du candidat
        List<String> candidatCompetences = new ArrayList<>();
        if (candidat.getCompetences() != null) {
            candidatCompetences = candidat.getCompetences().stream()
                .map(Competence::getNom)
                .collect(Collectors.toList());
        }

        // 2. Préparer les formations déjà terminées
        List<Long> formationsTermineesIds = new ArrayList<>();
        if (candidat.getInscriptions() != null) {
            formationsTermineesIds = candidat.getInscriptions().stream()
                .filter(i -> "Terminé".equals(i.getStatut()) && i.getFormation() != null)
                .map(i -> i.getFormation().getId())
                .collect(Collectors.toList());
        }

        // 3. Préparer les formations disponibles à envoyer au ML
        List<Map<String, Object>> formationsData = formationsDisponibles.stream().map(f -> {
            List<String> compNames = new ArrayList<>();
            if (f.getCompetences() != null) {
                compNames = f.getCompetences().stream().map(Competence::getNom).collect(Collectors.toList());
            }
            return Map.of(
                "id", f.getId(),
                "titre", f.getTitre() != null ? f.getTitre() : "",
                "description", f.getDescription() != null ? f.getDescription() : "",
                "categorie", f.getCategorie() != null ? f.getCategorie() : "",
                "niveau", f.getNiveau() != null ? f.getNiveau() : "",
                "competences", compNames
            );
        }).collect(Collectors.toList());

        // 4. Construire le payload de la requête
        Map<String, Object> requestBody = Map.of(
            "candidat_competences", candidatCompetences,
            "candidat_niveau", candidat.getNiveauEtude() != null ? candidat.getNiveauEtude() : "",
            "formations_terminees_ids", formationsTermineesIds,
            "formations_disponibles", formationsData
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            // 5. Appel au service Python ML
            List response = restTemplate.postForObject(ML_SERVICE_URL, entity, List.class);
            return (List<Map<String, Object>>) response;
        } catch (Exception e) {
            System.err.println("Erreur de connexion au service ML Python : " + e.getMessage());
            return new ArrayList<>(); // Retourner liste vide si le service Python est éteint
        }
    }
}
