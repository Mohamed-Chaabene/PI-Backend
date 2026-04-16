package t.esprit.arctic.jobmatch.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
<<<<<<< HEAD
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
=======
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
import t.esprit.arctic.jobmatch.entity.Candidat;
import t.esprit.arctic.jobmatch.entity.Document;
import t.esprit.arctic.jobmatch.entity.Utilisateur;
import t.esprit.arctic.jobmatch.repository.CandidatRepository;
import t.esprit.arctic.jobmatch.repository.DocumentRepository;
import t.esprit.arctic.jobmatch.repository.UtilisateurRepository;

import java.util.List;
<<<<<<< HEAD
=======
import java.util.Base64;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0

@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentRepository documentRepository;
    private final CandidatRepository candidatRepository;
    private final UtilisateurRepository utilisateurRepository;

<<<<<<< HEAD
    // ✅ Récupérer le candidat connecté par son ID
=======
    private final String ML_API_URL = "http://localhost:8000";
    private final RestTemplate restTemplate = new RestTemplate();

>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
    private Candidat getCandidatConnecte() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Non authentifié");
        }

<<<<<<< HEAD
        // auth.getName() = email (mis par ton JwtFilter)
        String email = auth.getName();

        // Trouver l'utilisateur par email → récupérer son ID
        Utilisateur user = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé : " + email));

        System.out.println("✅ Utilisateur connecté - ID: " + user.getId() + " | Email: " + email);

        // Trouver le candidat par ID
=======
        String email = auth.getName();

        Utilisateur user = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé : " + email));

        System.out.println("Utilisateur connecté - ID: " + user.getId() + " | Email: " + email);

>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
        return candidatRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé pour ID: " + user.getId()));
    }

    // CREATE
    @PostMapping
    public ResponseEntity<Document> create(@RequestBody Document document) {
        try {
            Candidat candidat = getCandidatConnecte();
            document.setCandidat(candidat);
<<<<<<< HEAD
            System.out.println("✅ Document lié au candidat ID: " + candidat.getId());
        } catch (Exception e) {
            System.out.println("⚠️ Liaison candidat échouée: " + e.getMessage());
=======
            System.out.println(" Document lié au candidat ID: " + candidat.getId());
        } catch (Exception e) {
            System.out.println(" Liaison candidat échouée: " + e.getMessage());
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
        }
        Document saved = documentRepository.save(document);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

<<<<<<< HEAD
    // READ ALL — uniquement les documents du candidat connecté
=======
    // READ ALL
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
    @GetMapping
    public ResponseEntity<List<Document>> getAll() {
        try {
            Candidat candidat = getCandidatConnecte();
<<<<<<< HEAD
            System.out.println("📄 Documents pour candidat ID: " + candidat.getId());
            List<Document> docs = documentRepository.findByCandidatId(candidat.getId());
            return ResponseEntity.ok(docs);
        } catch (Exception e) {
            System.out.println("⚠️ Fallback getAll: " + e.getMessage());
=======
            System.out.println(" Documents pour candidat ID: " + candidat.getId());
            List<Document> docs = documentRepository.findByCandidatId(candidat.getId());
            return ResponseEntity.ok(docs);
        } catch (Exception e) {
            System.out.println(" Fallback getAll: " + e.getMessage());
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
            return ResponseEntity.ok(documentRepository.findAll());
        }
    }

    // READ ONE
    @GetMapping("/{id}")
    public ResponseEntity<Document> getById(@PathVariable Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document non trouvé"));
        try {
            Candidat candidat = getCandidatConnecte();
            if (document.getCandidat() != null &&
                    !document.getCandidat().getId().equals(candidat.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } catch (Exception ignored) {}
        return ResponseEntity.ok(document);
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<Document> update(@PathVariable Long id,
                                           @RequestBody Document document) {
        Document existing = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document non trouvé"));
        try {
            Candidat candidat = getCandidatConnecte();
            if (existing.getCandidat() != null &&
                    !existing.getCandidat().getId().equals(candidat.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } catch (Exception ignored) {}

        existing.setNom(document.getNom());
        existing.setType(document.getType());
        existing.setContenu(document.getContenu());
        existing.setTemplate(document.getTemplate());
        existing.setCompatibleATS(document.getCompatibleATS());
        existing.setAjouterPhoto(document.getAjouterPhoto());
        return ResponseEntity.ok(documentRepository.save(existing));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Document existing = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document non trouvé"));
        try {
            Candidat candidat = getCandidatConnecte();
            if (existing.getCandidat() != null &&
                    !existing.getCandidat().getId().equals(candidat.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } catch (Exception ignored) {}

        documentRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
<<<<<<< HEAD
=======

    // ============ SPRING DATA JPA KEYWORDS ============

    @GetMapping("/keywords/jpa/by-nom")
    public ResponseEntity<List<Document>> findByNomContainingIgnoreCase(@RequestParam String nom) {
        try {
            Candidat candidatConnecte = getCandidatConnecte();
            List<Document> documents = documentRepository.findByNomContainingIgnoreCase(nom);
            documents = documents.stream()
                    .filter(d -> d.getCandidat() != null && d.getCandidat().getId().equals(candidatConnecte.getId()))
                    .toList();
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            System.out.println("Erreur findByNomContainingIgnoreCase: " + e.getMessage());
            return ResponseEntity.ok(documentRepository.findByNomContainingIgnoreCase(nom));
        }
    }

    @GetMapping("/keywords/jpa/by-type-and-nom")
    public ResponseEntity<List<Document>> findByTypeAndNomContainingIgnoreCase(
            @RequestParam String type,
            @RequestParam String nom) {
        try {
            Candidat candidatConnecte = getCandidatConnecte();
            List<Document> documents = documentRepository.findByTypeAndNomContainingIgnoreCase(type, nom);
            documents = documents.stream()
                    .filter(d -> d.getCandidat() != null && d.getCandidat().getId().equals(candidatConnecte.getId()))
                    .toList();
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            System.out.println("Erreur findByTypeAndNomContainingIgnoreCase: " + e.getMessage());
            return ResponseEntity.ok(documentRepository.findByTypeAndNomContainingIgnoreCase(type, nom));
        }
    }

    @GetMapping("/keywords/jpa/exists-by-candidat")
    public ResponseEntity<Boolean> existsByCandidatIdAndType(
            @RequestParam Long candidatId,
            @RequestParam String type) {
        try {
            Candidat candidatConnecte = getCandidatConnecte();
            if (!candidatConnecte.getId().equals(candidatId)) {
                return ResponseEntity.ok(false);
            }
            boolean exists = documentRepository.existsByCandidatIdAndType(candidatId, type);
            return ResponseEntity.ok(exists);
        } catch (Exception e) {
            System.out.println("Erreur existsByCandidatIdAndType: " + e.getMessage());
            return ResponseEntity.ok(false);
        }
    }

    @GetMapping("/keywords/jpa/count-by-candidat")
    public ResponseEntity<Long> countByCandidatId(@RequestParam Long candidatId) {
        try {
            Candidat candidatConnecte = getCandidatConnecte();
            if (!candidatConnecte.getId().equals(candidatId)) {
                return ResponseEntity.ok(0L);
            }
            long count = documentRepository.countByCandidatId(candidatId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            System.out.println("Erreur countByCandidatId: " + e.getMessage());
            return ResponseEntity.ok(0L);
        }
    }

    @GetMapping("/keywords/jpa/top5-recents")
    public ResponseEntity<List<Document>> findTop5ByCandidatIdOrderByCreatedAtDesc(
            @RequestParam Long candidatId,
            @RequestParam(required = false) String mot) {
        try {
            Candidat candidatConnecte = getCandidatConnecte();
            if (!candidatConnecte.getId().equals(candidatId)) {
                return ResponseEntity.ok(new ArrayList<>());
            }

            List<Document> documents;
            if (mot != null && !mot.trim().isEmpty()) {
                documents = documentRepository.findTop5ByCandidatIdAndNomContainingIgnoreCaseOrderByCreatedAtDesc(candidatId, mot);
            } else {
                documents = documentRepository.findTop5ByCandidatIdOrderByCreatedAtDesc(candidatId);
            }
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            System.out.println("Erreur findTop5ByCandidatIdOrderByCreatedAtDesc: " + e.getMessage());
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    // ============ ENDPOINTS POUR LES STATISTIQUES ============

    @GetMapping("/jpql/mes-documents-avec-infos")
    public ResponseEntity<List<Map<String, Object>>> getMesDocumentsAvecInfos() {
        try {
            Candidat candidatConnecte = getCandidatConnecte();
            Long candidatId = candidatConnecte.getId();
            System.out.println(" [MES DOCS] Candidat connecté ID: " + candidatId);

            List<Document> documents = documentRepository.findByCandidatId(candidatId);
            List<Map<String, Object>> resultats = new ArrayList<>();

            for (Document doc : documents) {
                Map<String, Object> map = new HashMap<>();
                map.put("documentId", doc.getId());
                map.put("documentNom", doc.getNom());
                // Correction : convertir l'enum en String
                map.put("documentType", doc.getType().name());
                map.put("candidatId", doc.getCandidat().getId());
                map.put("candidatPrenom", doc.getCandidat().getPrenom());
                map.put("candidatNiveauEtude", doc.getCandidat().getNiveauEtude());
                map.put("candidatDescription", doc.getCandidat().getDescription());
                resultats.add(map);
            }

            return ResponseEntity.ok(resultats);
        } catch (Exception e) {
            System.out.println(" Erreur getMesDocumentsAvecInfos: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @GetMapping("/jpql/mes-cvs-candidatures")
    public ResponseEntity<List<Map<String, Object>>> getMesCVsAvecCandidatures() {
        try {
            Candidat candidatConnecte = getCandidatConnecte();
            Long candidatId = candidatConnecte.getId();
            System.out.println(" [MES CVS] Candidat connecté ID: " + candidatId);

            List<Document> cvs = documentRepository.findByCandidatIdAndType(candidatId, "CV");
            List<Map<String, Object>> resultats = new ArrayList<>();

            for (Document cv : cvs) {
                Map<String, Object> map = new HashMap<>();
                map.put("documentId", cv.getId());
                map.put("cvNom", cv.getNom());
                map.put("candidatId", cv.getCandidat().getId());
                map.put("candidatPrenom", cv.getCandidat().getPrenom());
                map.put("nombreCandidatures", 0); // À calculer si besoin
                resultats.add(map);
            }

            return ResponseEntity.ok(resultats);
        } catch (Exception e) {
            System.out.println(" Erreur getMesCVsAvecCandidatures: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/jpql/statistiques-niveau")
    public ResponseEntity<List<Map<String, Object>>> getStatistiquesParNiveau() {
        // Cette méthode nécessite @Query car elle fait des agrégations
        return ResponseEntity.ok(new ArrayList<>());
    }

    @GetMapping("/jpql/mes-statistiques")
    public ResponseEntity<List<Map<String, Object>>> getMesStatistiques() {
        try {
            Candidat candidatConnecte = getCandidatConnecte();
            Long candidatId = candidatConnecte.getId();
            System.out.println(" Candidat connecté ID: " + candidatId);

            List<Document> documents = documentRepository.findByCandidatId(candidatId);
            Map<String, Integer> stats = new HashMap<>();

            for (Document doc : documents) {
                // CORRECTION ICI : utilisez getType().name() ou getType().toString()
                String type = doc.getType().name();  // ← Au lieu de doc.getString()
                stats.put(type, stats.getOrDefault(type, 0) + 1);
            }

            List<Map<String, Object>> resultats = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : stats.entrySet()) {
                Map<String, Object> map = new HashMap<>();
                map.put("typeDocument", entry.getKey());
                map.put("nombreDocuments", entry.getValue());
                resultats.add(map);
            }

            return ResponseEntity.ok(resultats);
        } catch (Exception e) {
            System.out.println(" Erreur: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ============ RECHERCHE ============

    @GetMapping("/keywords/recherche")
    public ResponseEntity<List<Map<String, Object>>> rechercher(@RequestParam String mot) {
        try {
            Candidat candidatConnecte = getCandidatConnecte();
            List<Document> documents = documentRepository.findByNomContainingIgnoreCase(mot);

            List<Map<String, Object>> resultats = new ArrayList<>();
            for (Document doc : documents) {
                if (doc.getCandidat() != null && doc.getCandidat().getId().equals(candidatConnecte.getId())) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", doc.getId());
                    map.put("documentNom", doc.getNom());
                    // Correction : convertir l'enum en String
                    map.put("type", doc.getType().name());
                    map.put("candidatId", doc.getCandidat().getId());
                    map.put("candidatPrenom", doc.getCandidat().getPrenom());
                    resultats.add(map);
                }
            }
            return ResponseEntity.ok(resultats);
        } catch (Exception e) {
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    @PostMapping("/keywords/multi-recherche")
    public ResponseEntity<List<Map<String, Object>>> rechercherMulti(@RequestBody List<String> mots) {
        if (mots == null || mots.size() < 3) {
            return ResponseEntity.badRequest().build();
        }

        try {
            Candidat candidatConnecte = getCandidatConnecte();
            List<Document> documents = documentRepository.findByNomContainingIgnoreCase(mots.get(0));

            List<Map<String, Object>> resultats = new ArrayList<>();
            for (Document doc : documents) {
                if (doc.getCandidat() != null && doc.getCandidat().getId().equals(candidatConnecte.getId())) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", doc.getId());
                    map.put("documentNom", doc.getNom());
                    map.put("type", doc.getType());
                    map.put("candidatId", doc.getCandidat().getId());
                    map.put("candidatPrenom", doc.getCandidat().getPrenom());
                    resultats.add(map);
                }
            }
            return ResponseEntity.ok(resultats);
        } catch (Exception e) {
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    // ==================== ANALYSE CV (Appel au modèle ML) ====================
    @PostMapping("/{id}/analyser")
    public ResponseEntity<Map<String, Object>> analyserCV(@PathVariable Long id) {
        try {
            Document document = documentRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Document non trouvé"));

            if (!"CV".equals(document.getType())) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "L'analyse est disponible uniquement pour les CVs");
                return ResponseEntity.badRequest().body(error);
            }

            String mlUrl = ML_API_URL + "/analyze";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("cv_content", document.getContenu());

            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(mlUrl, request, Map.class);

            return ResponseEntity.ok(response.getBody());

        } catch (Exception e) {
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("error", "Service ML indisponible: " + e.getMessage());
            fallback.put("scoreGlobal", 50);
            fallback.put("profilDetecte", "Non détecté");
            fallback.put("competencesTrouvees", new ArrayList<>());
            fallback.put("pointsForts", List.of("Service d'analyse temporairement indisponible"));
            fallback.put("pointsAmeliorer", List.of("Réessayez plus tard"));
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(fallback);
        }
    }

    // ==================== GÉNÉRATION DE DOCUMENT ====================
    @PostMapping("/generer")
    public ResponseEntity<Map<String, Object>> genererDocument(@RequestBody Map<String, Object> request) {
        String type = (String) request.get("type");
        Map<String, Object> cvData = (Map<String, Object>) request.get("cvData");
        Map<String, Object> lettreData = (Map<String, Object>) request.get("lettreData");
        Map<String, Object> portfolioData = (Map<String, Object>) request.get("portfolioData");
        Map<String, Object> autreData = (Map<String, Object>) request.get("autreData");

        Map<String, Object> result = new HashMap<>();
        String contenu = "";
        String nom = "";

        switch (type) {
            case "CV":
                contenu = genererCV(cvData);
                nom = genererNomCV(cvData);
                break;
            case "LETTRE_DE_MOTIVATION":
                contenu = genererLettre(lettreData);
                nom = genererNomLettre(lettreData);
                break;
            case "PORTFOLIO":
                contenu = genererPortfolio(portfolioData);
                nom = genererNomPortfolio(portfolioData);
                break;
            default:
                contenu = (String) autreData.get("contenu");
                nom = (String) autreData.get("titre");
        }

        result.put("contenu", contenu);
        result.put("nom", nom);
        result.put("type", type);

        return ResponseEntity.ok(result);
    }

    private String genererCV(Map<String, Object> cvData) {
        String prenom = (String) cvData.getOrDefault("prenom", "");
        String nom = (String) cvData.getOrDefault("nom", "");
        String fullName = (prenom + " " + nom).trim();
        if (fullName.isEmpty()) fullName = "Votre Nom";
        return "<html>...</html>";
    }

    private String genererNomCV(Map<String, Object> cvData) {
        String prenom = (String) cvData.getOrDefault("prenom", "");
        String nom = (String) cvData.getOrDefault("nom", "");
        String fullName = (prenom + "_" + nom).trim();
        return fullName.isEmpty() ? "Mon_CV" : fullName + "_CV";
    }

    private String genererLettre(Map<String, Object> lettreData) {
        return "<html>...</html>";
    }

    private String genererNomLettre(Map<String, Object> lettreData) {
        String entreprise = (String) lettreData.getOrDefault("entreprise", "");
        return entreprise.isEmpty() ? "Lettre_motivation" : "Lettre_" + entreprise;
    }

    private String genererPortfolio(Map<String, Object> portfolioData) {
        return "<html>...</html>";
    }

    private String genererNomPortfolio(Map<String, Object> portfolioData) {
        String titre = (String) portfolioData.getOrDefault("titre", "");
        return titre.isEmpty() ? "Mon_Portfolio" : titre;
    }

    // ==================== OPTIMISATION CV ====================
    @PostMapping("/{id}/optimiser")
    public ResponseEntity<Map<String, Object>> optimiserCV(@PathVariable Long id,
                                                           @RequestBody Map<String, String> request) {
        try {
            Document document = documentRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Document non trouvé"));

            String offreEmploi = request.get("offreEmploi");

            if (offreEmploi == null || offreEmploi.trim().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Offre d'emploi requise");
                return ResponseEntity.badRequest().body(error);
            }

            String mlUrl = ML_API_URL + "/optimize";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("cv_content", document.getContenu());
            requestBody.put("job_offer", offreEmploi);

            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(mlUrl, requestEntity, Map.class);

            return ResponseEntity.ok(response.getBody());

        } catch (Exception e) {
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("error", "Service ML indisponible: " + e.getMessage());
            fallback.put("scoreCompatibilite", 50);
            fallback.put("probabiliteEntretien", 40);
            fallback.put("suggestionsOptimisation", List.of("Service d'optimisation temporairement indisponible"));
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(fallback);
        }
    }

    // ==================== CHATBOT ====================
    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, String> request) {
        try {
            String message = request.get("message");
            String cvContent = request.getOrDefault("cv_content", "");

            if (message == null || message.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("response", "Veuillez poser une question");
                return ResponseEntity.badRequest().body(error);
            }

            String mlUrl = ML_API_URL + "/chat/ml";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("message", message);
            requestBody.put("cv_content", cvContent);

            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(mlUrl, requestEntity, Map.class);

            Map<String, String> result = new HashMap<>();
            result.put("response", (String) response.getBody().get("response"));
            result.put("intention", (String) response.getBody().getOrDefault("intention", "general"));

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, String> fallback = new HashMap<>();
            fallback.put("response", "Désolé, je rencontre une difficulté technique. Veuillez réessayer plus tard.");
            fallback.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(fallback);
        }
    }

    // ==================== PRÉDICTION IA ====================
    @SuppressWarnings("unchecked")
    @PostMapping("/prediction/succes")
    public ResponseEntity<Map<String, Object>> predictionSucces(@RequestBody Map<String, Object> request) {
        try {
            String cvContent = (String) request.getOrDefault("cv_content", "");
            List<Map<String, Object>> historique = (List<Map<String, Object>>) request.getOrDefault("historique_candidatures", new ArrayList<>());

            String mlUrl = ML_API_URL + "/prediction/succes";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("cv_content", cvContent);
            requestBody.put("historique_candidatures", historique);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(mlUrl, requestEntity, Map.class);

            return ResponseEntity.ok(response.getBody());

        } catch (Exception e) {
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("error", "Service ML indisponible: " + e.getMessage());
            fallback.put("probabilite", 50);
            fallback.put("meilleurMoment", "Service temporairement indisponible");
            fallback.put("pointsForts", new ArrayList<>());
            fallback.put("pointsAmeliorer", new ArrayList<>());
            fallback.put("conseilsSpecifiques", new ArrayList<>());
            fallback.put("couleur", "#f59e0b");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(fallback);
        }
    }

    // ==================== TRAITEMENT PHOTO ====================
    @PostMapping("/traiter-photo")
    public ResponseEntity<Map<String, String>> traiterPhoto(@RequestParam("photo") MultipartFile photo) {
        try {
            if (photo == null || photo.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Photo requise");
                return ResponseEntity.badRequest().body(error);
            }

            if (photo.getSize() > 5 * 1024 * 1024) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Photo trop volumineuse (max 5 Mo)");
                return ResponseEntity.badRequest().body(error);
            }

            try {
                String mlUrl = ML_API_URL + "/photo/professionalize";

                String base64Image = Base64.getEncoder().encodeToString(photo.getBytes());

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                Map<String, String> requestBody = new HashMap<>();
                requestBody.put("image_base64", "data:" + photo.getContentType() + ";base64," + base64Image);

                HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

                ResponseEntity<Map> response = restTemplate.postForEntity(mlUrl, requestEntity, Map.class);

                if (response.getBody() != null && response.getBody().containsKey("image_professionnelle")) {
                    Map<String, String> result = new HashMap<>();
                    result.put("photoUrl", (String) response.getBody().get("image_professionnelle"));
                    result.put("photoName", photo.getOriginalFilename());
                    return ResponseEntity.ok(result);
                }
            } catch (Exception mlError) {
                System.out.println("⚠ Service ML photo indisponible: " + mlError.getMessage());
            }

            String base64Image = Base64.getEncoder().encodeToString(photo.getBytes());
            String photoUrl = "data:" + photo.getContentType() + ";base64," + base64Image;

            Map<String, String> result = new HashMap<>();
            result.put("photoUrl", photoUrl);
            result.put("photoName", photo.getOriginalFilename());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur traitement photo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ==================== HEALTH CHECK ML ====================
    @GetMapping("/ml/health")
    public ResponseEntity<Map<String, Object>> checkMLHealth() {
        try {
            String mlUrl = ML_API_URL + "/health";
            ResponseEntity<Map> response = restTemplate.getForEntity(mlUrl, Map.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "unavailable");
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
        }
    }
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
}