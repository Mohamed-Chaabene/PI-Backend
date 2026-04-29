package t.esprit.arctic.jobmatch.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import t.esprit.arctic.jobmatch.entity.Candidat;
import t.esprit.arctic.jobmatch.entity.Competence;
import t.esprit.arctic.jobmatch.entity.Evenement;
import t.esprit.arctic.jobmatch.repository.EvenementRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class RecommendationEventService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private EvenementRepository evenementRepository;

    @Value("${reco.api.url:http://localhost:5001}")
    private String recoApiUrl;


    public List<Evenement> getRecommendedEvents(Long candidateId,
                                                Candidat candidat) {

        String domaine   = normaliseDomaine(candidat.getBackgroundExpertise());
        String ville     = normaliseVille(
                candidat.getLocalisation() != null
                        ? candidat.getLocalisation().getVille() : null);
        String skills    = extraireSkills(candidat.getCompetences());
        String education = normaliseEducation(candidat.getNiveauEtude());


        System.out.println("=== FLASK REQUEST ===");
        System.out.println("candidate_id  : " + candidateId);
        System.out.println("domaine       : " + domaine);
        System.out.println("ville         : " + ville);
        System.out.println("skills        : " + skills);
        System.out.println("education     : " + education);


        Map<String, Object> body = new HashMap<>();
        body.put("candidate_id",         candidateId);
        body.put("domaine_candidat",     domaine);
        body.put("ville_candidat",       ville);
        body.put("candidate_skills",     skills);
        body.put("candidate_education",  education);
        body.put("candidate_experience", 0);
        body.put("top_n", 5);

        try {
            ResponseEntity<Map> response =
                    restTemplate.postForEntity(
                            recoApiUrl + "/recommend", body, Map.class);

            List<Map<String, Object>> events =
                    (List<Map<String, Object>>)
                            response.getBody().get("evenements");


            List<Long> ids = events.stream()
                    .map(e -> Long.valueOf(
                            e.get("evenement_id").toString()))
                    .collect(Collectors.toList());

            return evenementRepository.findAllById(ids);

        } catch (Exception e) {
            System.err.println("Flask indisponible : "
                    + e.getMessage());
            return evenementRepository
                    .findTop5ByOrderByDateHeureDesc();
        }
    }


    private String normaliseDomaine(String raw) {
        if (raw == null || raw.isBlank()) return "IT";
        String r = raw.toLowerCase();

        if (r.contains("it")       || r.contains("info")    ||
                r.contains("dev")      || r.contains("tech")    ||
                r.contains("logiciel") || r.contains("software")||
                r.contains("web")      || r.contains("cloud")   ||
                r.contains("data")     || r.contains("réseau")  ||
                r.contains("réseau")   || r.contains("cyber"))
            return "IT";

        if (r.contains("finance")  || r.contains("compta")  ||
                r.contains("banque")   || r.contains("audit")   ||
                r.contains("fiscal")   || r.contains("économi") ||
                r.contains("gestion")  || r.contains("invest"))
            return "Finance";

        if (r.contains("market")   || r.contains("comm")    ||
                r.contains("vente")    || r.contains("commerce")||
                r.contains("digital")  || r.contains("publicité")||
                r.contains("seo")      || r.contains("contenu"))
            return "Marketing";

        return "IT";
    }


    private String normaliseVille(String raw) {
        if (raw == null || raw.isBlank()) return "Tunis";
        String r = raw.toLowerCase().trim();

        if (r.contains("tunis")    || r.contains("ariana")  ||
                r.contains("manouba")  || r.contains("ben arous")||
                r.contains("la marsa") || r.contains("carthage"))
            return "Tunis";

        if (r.contains("sfax"))
            return "Sfax";

        if (r.contains("sousse")   || r.contains("monastir")||
                r.contains("mahdia")   || r.contains("hammamet")||
                r.contains("nabeul"))
            return "Sousse";

        return "Tunis";
    }


    private String normaliseEducation(String raw) {
        if (raw == null || raw.isBlank()) return "Licence";
        String r = raw.toLowerCase();

        if (r.contains("master")      || r.contains("bac+5") ||
                r.contains("bac +5")      || r.contains("ingénieur")||
                r.contains("ingenieur")   || r.contains("bac+4") ||
                r.contains("doctorat")    || r.contains("phd"))
            return "Master";

        if (r.contains("licence")     || r.contains("bac+3") ||
                r.contains("bac +3")      || r.contains("l3")    ||
                r.contains("bts")         || r.contains("bac+2"))
            return "Licence";

        if (r.contains("bac") && !r.contains("+"))
            return "Bac";

        return "Licence";
    }


    private String extraireSkills(List<Competence> competences) {
        if (competences == null || competences.isEmpty())
            return "unknown";
        return competences.stream()
                .map(c -> c.getNom().toLowerCase()
                        .replace(" ", "_")
                        .replace("-", "_"))
                .collect(Collectors.joining(" "));
    }
}