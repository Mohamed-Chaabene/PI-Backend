package t.esprit.arctic.jobmatch.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import t.esprit.arctic.jobmatch.dto.CandidatureDTO;
import t.esprit.arctic.jobmatch.entity.Candidat;
import t.esprit.arctic.jobmatch.entity.Candidature;
import t.esprit.arctic.jobmatch.entity.OffreEmploi;
import t.esprit.arctic.jobmatch.repository.CandidatRepository;
import t.esprit.arctic.jobmatch.repository.CandidatureRepository;
import t.esprit.arctic.jobmatch.repository.OffreEmploiRepository;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/candidatures")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class CandidatureController {

    private final CandidatureRepository candidatureRepository;
    private final CandidatRepository candidatRepository;
    private final OffreEmploiRepository offreEmploiRepository;


    @GetMapping("/admin/toutes")
    public ResponseEntity<List<CandidatureDTO>> getAllCandidaturesForAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthorized = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")
                        || authority.getAuthority().equals("ROLE_RECRUTEUR"));

        if (!isAuthorized) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<CandidatureDTO> candidatures = candidatureRepository.findAllByOrderByDateEnvoiDesc()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(candidatures);
    }




    // ==================== CREATE ====================
    @PostMapping
    public ResponseEntity<?> creerCandidature(@Valid @RequestBody CandidatureDTO dto, BindingResult result) {
        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            result.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(errors);
        }

        if (!dto.isAcceptRGPD()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Vous devez accepter les conditions RGPD"));
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Candidat candidat = candidatRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé"));

        Candidature candidature = new Candidature();
        candidature.setDateEnvoi(new Date());
        candidature.setStatut("EN_ATTENTE");
        candidature.setCandidat(candidat);

        if (dto.getOffreId() != null) {
            OffreEmploi offre = offreEmploiRepository.findById(dto.getOffreId())
                    .orElseThrow(() -> new RuntimeException("Offre non trouvée avec ID: " + dto.getOffreId()));
            candidature.setOffreEmploi(offre);
            if (offre.getTitre() != null) {
                dto.setOffreTitre(offre.getTitre());
            }
        }

        candidature.setNomComplet(dto.getNomComplet());
        candidature.setEmail(dto.getEmail());
        candidature.setTelephone(dto.getTelephone());
        candidature.setDescription(dto.getDescription());
        candidature.setFormation(dto.getFormation());
        candidature.setExperience(dto.getExperience());
        candidature.setCompetences(dto.getCompetences());
        candidature.setLettreMotivation(dto.getLettreMotivation());
        candidature.setDateDisponibilite(dto.getDateDisponibilite());
        candidature.setPreavis(dto.getPreavis());
        candidature.setAcceptContact(dto.getAcceptContact());
        candidature.setAcceptRGPD(dto.isAcceptRGPD());

        Candidature saved = candidatureRepository.save(candidature);
        return new ResponseEntity<>(convertToDTO(saved), HttpStatus.CREATED);
    }

    // ==================== READ - Mes candidatures ====================
    @GetMapping("/mes-candidatures")
    public ResponseEntity<List<CandidatureDTO>> getMesCandidatures() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Candidat candidat = candidatRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé"));

        List<CandidatureDTO> candidatures = candidatureRepository.findByCandidatId(candidat.getId())
                .stream().map(this::convertToDTO).collect(Collectors.toList());

        return ResponseEntity.ok(candidatures);
    }

    // ==================== READ - Statistiques ====================
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Candidat candidat = candidatRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé"));

        List<Candidature> candidatures = candidatureRepository.findByCandidatId(candidat.getId());

        Map<String, Long> stats = new HashMap<>();
        stats.put("total", (long) candidatures.size());
        stats.put("enAttente", candidatures.stream().filter(c -> "EN_ATTENTE".equals(c.getStatut())).count());
        stats.put("acceptees", candidatures.stream().filter(c -> "ACCEPTEE".equals(c.getStatut())).count());
        stats.put("refusees", candidatures.stream().filter(c -> "REFUSEE".equals(c.getStatut())).count());

        return ResponseEntity.ok(stats);
    }

    // ==================== READ - Candidatures par offre ====================
    @GetMapping("/offre/{offreId}")
    public ResponseEntity<List<CandidatureDTO>> getCandidaturesByOffre(@PathVariable Long offreId) {
        List<CandidatureDTO> candidatures = candidatureRepository.findByOffreEmploiId(offreId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(candidatures);
    }

    // ==================== READ - Filtrer par statut ====================
    @GetMapping("/filtre/statut/{statut}")
    public ResponseEntity<List<CandidatureDTO>> filtrerParStatut(@PathVariable String statut) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Candidat candidat = candidatRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé"));

        List<CandidatureDTO> resultats = candidatureRepository.findByCandidatId(candidat.getId())
                .stream().filter(c -> c.getStatut().equals(statut))
                .map(this::convertToDTO).collect(Collectors.toList());

        return ResponseEntity.ok(resultats);
    }

    // ==================== READ - Trier par date ====================
    @GetMapping("/tri/date")
    public ResponseEntity<List<CandidatureDTO>> trierParDate() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Candidat candidat = candidatRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé"));

        List<CandidatureDTO> resultats = candidatureRepository.findByCandidatId(candidat.getId())
                .stream().sorted((c1, c2) -> c2.getDateEnvoi().compareTo(c1.getDateEnvoi()))
                .map(this::convertToDTO).collect(Collectors.toList());

        return ResponseEntity.ok(resultats);
    }

    // ==================== READ - Recherche par entreprise ====================
    @GetMapping("/recherche")
    public ResponseEntity<List<CandidatureDTO>> rechercherParEntreprise(@RequestParam String entreprise) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Candidat candidat = candidatRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé"));

        List<CandidatureDTO> resultats = candidatureRepository.findByCandidatId(candidat.getId())
                .stream().map(this::convertToDTO).collect(Collectors.toList());

        return ResponseEntity.ok(resultats);
    }

    // ==================== READ - Par ID ====================
    @GetMapping("/{id}")
    public ResponseEntity<CandidatureDTO> getCandidatureById(@PathVariable Long id) {
        Candidature candidature = candidatureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Candidat candidat = candidatRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé"));

        if (!candidature.getCandidat().getId().equals(candidat.getId())) {
            throw new RuntimeException("Vous n'avez pas accès à cette candidature");
        }

        return ResponseEntity.ok(convertToDTO(candidature));
    }

    // ==================== UPDATE ====================
    @PutMapping("/{id}")
    public ResponseEntity<?> modifierCandidature(
            @PathVariable Long id,
            @Valid @RequestBody CandidatureDTO dto,
            BindingResult result) {

        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            result.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(errors);
        }

        Candidature candidature = candidatureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée avec ID: " + id));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Candidat candidat = candidatRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé"));

        if (!candidature.getCandidat().getId().equals(candidat.getId())) {
            throw new RuntimeException("Vous ne pouvez pas modifier cette candidature");
        }

        if (dto.getNomComplet() != null) candidature.setNomComplet(dto.getNomComplet());
        if (dto.getEmail() != null) candidature.setEmail(dto.getEmail());
        if (dto.getTelephone() != null) candidature.setTelephone(dto.getTelephone());
        if (dto.getDescription() != null) candidature.setDescription(dto.getDescription());
        if (dto.getFormation() != null) candidature.setFormation(dto.getFormation());
        if (dto.getExperience() != null) candidature.setExperience(dto.getExperience());
        if (dto.getCompetences() != null) candidature.setCompetences(dto.getCompetences());
        if (dto.getLettreMotivation() != null) candidature.setLettreMotivation(dto.getLettreMotivation());
        if (dto.getDateDisponibilite() != null) candidature.setDateDisponibilite(dto.getDateDisponibilite());
        if (dto.getPreavis() != null) candidature.setPreavis(dto.getPreavis());
        if (dto.getAcceptContact() != null) candidature.setAcceptContact(dto.getAcceptContact());
        candidature.setAcceptRGPD(dto.isAcceptRGPD());

        Candidature updated = candidatureRepository.save(candidature);
        return ResponseEntity.ok(convertToDTO(updated));
    }

    // ==================== RECRUTEUR - Modifier statut ====================
    @PutMapping("/{id}/statut")
    public ResponseEntity<CandidatureDTO> modifierStatutCandidature(
            @PathVariable Long id,
            @RequestParam String statut) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isRecruteur = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_RECRUTEUR")
                        || a.getAuthority().equals("ROLE_ADMIN"));

        if (!isRecruteur) {
            throw new RuntimeException("Seul un recruteur peut modifier le statut");
        }

        Candidature candidature = candidatureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée"));

        if (!statut.equals("ACCEPTEE") && !statut.equals("REFUSEE")) {
            throw new RuntimeException("Statut invalide. Valeurs acceptées: ACCEPTEE ou REFUSEE");
        }

        candidature.setStatut(statut);
        return ResponseEntity.ok(convertToDTO(candidatureRepository.save(candidature)));
    }

    // ==================== DELETE ====================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerCandidature(@PathVariable Long id) {
        Candidature candidature = candidatureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Candidat candidat = candidatRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé"));

        if (!candidature.getCandidat().getId().equals(candidat.getId())) {
            throw new RuntimeException("Vous ne pouvez pas supprimer cette candidature");
        }

        candidatureRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== QUICK APPLY ====================
    @PostMapping("/quick-apply")
    public ResponseEntity<?> quickApply(@RequestBody Map<String, Object> quickApplyData) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            Candidat candidat = candidatRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Candidat non trouvé"));

            Candidature candidature = new Candidature();
            candidature.setDateEnvoi(new Date());
            candidature.setStatut("EN_ATTENTE");
            candidature.setCandidat(candidat);
            candidature.setAcceptRGPD(true);

            OffreEmploi offre = null;
            if (quickApplyData.get("offreId") != null) {
                Long offreId = Long.valueOf(quickApplyData.get("offreId").toString());
                offre = offreEmploiRepository.findById(offreId)
                        .orElseThrow(() -> new RuntimeException("Offre non trouvée avec ID: " + offreId));
                candidature.setOffreEmploi(offre);
            }

            String nomComplet = candidat.getPrenom() != null
                    ? candidat.getPrenom() + " " + candidat.getNom()
                    : candidat.getNom() != null ? candidat.getNom() : "Candidat";

            candidature.setNomComplet(nomComplet);
            candidature.setEmail(candidat.getEmail());
            candidature.setTelephone("Non spécifié");

            if (offre != null) {
                String lettreGeneree = String.format(
                        "CANDIDATURE POUR L'OFFRE\n================================\n" +
                                "Poste : %s\nEntreprise : %s\nLocalisation : %s\n" +
                                "Type de contrat : %s\nSalaire : %s\n================================\n\n" +
                                "Description du poste :\n%s\n\nDate de candidature : %s\nStatut : En attente",
                        offre.getTitre() != null ? offre.getTitre() : "Non spécifié",
                        offre.getEntreprise() != null ? offre.getEntreprise() : "Non spécifiée",
                        offre.getLocation() != null ? offre.getLocation() : "Non spécifiée",
                        offre.getTypeContrat() != null ? offre.getTypeContrat() : "Non spécifié",
                        offre.getSalary() != null ? offre.getSalary() : "Non spécifié",
                        offre.getDescription() != null ? offre.getDescription() : "Aucune description",
                        new Date()
                );
                candidature.setLettreGeneree(lettreGeneree);
                candidature.setDescription("Candidature pour: " + offre.getTitre() + " chez " + offre.getEntreprise());
            } else {
                candidature.setLettreGeneree(quickApplyData.get("lettreGeneree").toString());
                candidature.setDescription("Candidature rapide");
            }

            Candidature saved = candidatureRepository.save(candidature);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Candidature envoyée avec succès");
            response.put("id", saved.getId());
            if (offre != null) {
                response.put("offreTitre", offre.getTitre());
                response.put("entreprise", offre.getEntreprise());
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage(), "success", false));
        }
    }





    // ==================== FONCTIONNALITÉS AVANCÉES ====================

    // 1 — Analyse taux de réussite
    @GetMapping("/taux-reussite")
    public ResponseEntity<Map<String, Object>> getTauxReussite() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Candidat candidat = candidatRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé"));

        List<Candidature> candidatures = candidatureRepository.findByCandidatId(candidat.getId());

        long total = candidatures.size();
        long acceptees = candidatures.stream().filter(c -> "ACCEPTEE".equals(c.getStatut())).count();
        long refusees = candidatures.stream().filter(c -> "REFUSEE".equals(c.getStatut())).count();
        long enAttente = candidatures.stream().filter(c -> "EN_ATTENTE".equals(c.getStatut())).count();

        double tauxReussite = total > 0 ? (double) acceptees / total * 100 : 0;
        double tauxRefus = total > 0 ? (double) refusees / total * 100 : 0;

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("acceptees", acceptees);
        result.put("refusees", refusees);
        result.put("enAttente", enAttente);
        result.put("tauxReussite", Math.round(tauxReussite));
        result.put("tauxRefus", Math.round(tauxRefus));

        return ResponseEntity.ok(result);
    }

    // 2 — Statistiques par mois
    @GetMapping("/stats-par-mois")
    public ResponseEntity<List<Map<String, Object>>> getStatsParMois() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Candidat candidat = candidatRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé"));

        List<Candidature> candidatures = candidatureRepository.findByCandidatId(candidat.getId());

        String[] mois = {"Jan","Fév","Mar","Avr","Mai","Jun","Jul","Aoû","Sep","Oct","Nov","Déc"};
        int[] compteur = new int[12];

        candidatures.forEach(c -> {
            if (c.getDateEnvoi() != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(c.getDateEnvoi());
                compteur[cal.get(Calendar.MONTH)]++;
            }
        });

        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            Map<String, Object> m = new HashMap<>();
            m.put("mois", mois[i]);
            m.put("count", compteur[i]);
            result.add(m);
        }

        return ResponseEntity.ok(result);
    }

    // 3 — Smart Match Score (compatibilité avec les offres)
    @GetMapping("/smart-match")
    public ResponseEntity<List<Map<String, Object>>> getSmartMatch() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Candidat candidat = candidatRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé"));

        // Récupérer toutes les compétences du candidat depuis ses candidatures
        List<Candidature> mesCandidatures = candidatureRepository.findByCandidatId(candidat.getId());
        Set<String> mesCompetences = new HashSet<>();
        mesCandidatures.forEach(c -> {
            if (c.getCompetences() != null) {
                Arrays.stream(c.getCompetences().split(","))
                        .map(String::trim)
                        .map(String::toLowerCase)
                        .forEach(mesCompetences::add);
            }
        });

        // Récupérer toutes les offres
        List<OffreEmploi> offres = offreEmploiRepository.findAll();

        List<Map<String, Object>> result = offres.stream().map(offre -> {
                    // Calculer le score de compatibilité
                    String descOffre = ((offre.getDescription() != null ? offre.getDescription() : "") + " " +
                            (offre.getTitre() != null ? offre.getTitre() : "")).toLowerCase();

                    long matches = mesCompetences.stream()
                            .filter(comp -> descOffre.contains(comp))
                            .count();

                    int score = mesCompetences.size() > 0
                            ? (int) Math.min((matches * 100) / mesCompetences.size(), 99)
                            : 30;

                    String label = score >= 70 ? "Excellent match" : score >= 40 ? "Bon match" : "Match partiel";

                    Map<String, Object> m = new HashMap<>();
                    m.put("offreId", offre.getId());
                    m.put("titrOffre", offre.getTitre());
                    m.put("entreprise", offre.getEntreprise());
                    m.put("localisation", offre.getLocation());
                    m.put("score", score);
                    m.put("label", label);
                    m.put("typeContrat", offre.getTypeContrat());
                    m.put("salary", offre.getSalary());
                    return m;
                })
                .sorted((a, b) -> (int) b.get("score") - (int) a.get("score"))
                .limit(10)
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // 4 — Radar compétences
    @GetMapping("/radar-competences")
    public ResponseEntity<Map<String, Object>> getRadarCompetences() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Candidat candidat = candidatRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé"));

        List<Candidature> candidatures = candidatureRepository.findByCandidatId(candidat.getId());

        // Collecter toutes les compétences
        Set<String> competences = new HashSet<>();
        candidatures.forEach(c -> {
            if (c.getCompetences() != null) {
                Arrays.stream(c.getCompetences().split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .forEach(competences::add);
            }
        });

        long total = candidatures.size();
        long acceptees = candidatures.stream().filter(c -> "ACCEPTEE".equals(c.getStatut())).count();
        long avecExperience = candidatures.stream().filter(c -> c.getExperience() != null && !c.getExperience().isEmpty()).count();

        // Calculer les scores radar
        List<Map<String, Object>> radarData = new ArrayList<>();

        Map<String, Object> r1 = new HashMap<>();
        r1.put("label", "Compétences techniques");
        r1.put("valeur", Math.min(competences.size() * 15, 100));
        radarData.add(r1);

        Map<String, Object> r2 = new HashMap<>();
        r2.put("label", "Expérience");
        r2.put("valeur", avecExperience > 0 ? 75 : 20);
        radarData.add(r2);

        Map<String, Object> r3 = new HashMap<>();
        r3.put("label", "Candidatures");
        r3.put("valeur", Math.min(total * 10, 100));
        radarData.add(r3);

        Map<String, Object> r4 = new HashMap<>();
        r4.put("label", "Taux de succès");
        r4.put("valeur", total > 0 ? (int)((double) acceptees / total * 100) : 0);
        radarData.add(r4);

        Map<String, Object> result = new HashMap<>();
        result.put("radarData", radarData);
        result.put("competences", new ArrayList<>(competences));
        result.put("totalCompetences", competences.size());

        return ResponseEntity.ok(result);
    }

    // 5 — Prédiction de succès
    @GetMapping("/prediction-succes")
    public ResponseEntity<Map<String, Object>> getPredictionSucces() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Candidat candidat = candidatRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé"));

        List<Candidature> candidatures = candidatureRepository.findByCandidatId(candidat.getId());

        long total = candidatures.size();
        long acceptees = candidatures.stream().filter(c -> "ACCEPTEE".equals(c.getStatut())).count();

        // Calcul intelligent de la probabilité
        int probabilite = 40; // base

        if (total > 0) probabilite += 10;
        if (total >= 5) probabilite += 10;
        if (acceptees > 0) probabilite += 15;

        // Vérifier si le candidat a des compétences renseignées
        boolean aDesCompetences = candidatures.stream()
                .anyMatch(c -> c.getCompetences() != null && !c.getCompetences().isEmpty());
        if (aDesCompetences) probabilite += 10;

        // Vérifier si le candidat a une lettre de motivation
        boolean aLettre = candidatures.stream()
                .anyMatch(c -> c.getLettreMotivation() != null && !c.getLettreMotivation().isEmpty());
        if (aLettre) probabilite += 10;

        probabilite = Math.min(probabilite, 95);

        // Points forts
        List<String> pointsForts = new ArrayList<>();
        if (aDesCompetences) pointsForts.add("Compétences bien renseignées");
        if (aLettre) pointsForts.add("Lettre de motivation présente");
        if (acceptees > 0) pointsForts.add("Historique de succès");
        if (total >= 5) pointsForts.add("Candidature régulière");

        // Points à améliorer
        List<String> pointsAmeliorer = new ArrayList<>();
        if (!aDesCompetences) pointsAmeliorer.add("Ajoutez vos compétences");
        if (!aLettre) pointsAmeliorer.add("Rédigez une lettre de motivation");
        if (total < 3) pointsAmeliorer.add("Envoyez plus de candidatures");
        pointsAmeliorer.add("Personnalisez chaque candidature");

        String[] moments = {"Mardi matin", "Mercredi matin", "Lundi après-midi", "Jeudi matin"};
        String meilleurMoment = moments[(int)(Math.random() * moments.length)];

        Map<String, Object> result = new HashMap<>();
        result.put("probabilite", probabilite);
        result.put("meilleurMoment", meilleurMoment);
        result.put("pointsForts", pointsForts);
        result.put("pointsAmeliorer", pointsAmeliorer);

        return ResponseEntity.ok(result);
    }

    // 6 — Relances intelligentes
    @GetMapping("/relances")
    public ResponseEntity<List<Map<String, Object>>> getRelances() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Candidat candidat = candidatRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé"));

        List<Candidature> candidatures = candidatureRepository
                .findByCandidatIdAndStatut(candidat.getId(), "EN_ATTENTE");

        List<Map<String, Object>> result = candidatures.stream().map(c -> {
                    long joursEcoules = 0;
                    if (c.getDateEnvoi() != null) {
                        joursEcoules = (new Date().getTime() - c.getDateEnvoi().getTime()) / (1000 * 60 * 60 * 24);
                    }

                    String urgence = joursEcoules > 14 ? "haute" : joursEcoules > 7 ? "moyenne" : "basse";

                    String messageRelance = String.format(
                            "Bonjour,\n\nJe me permets de vous relancer concernant ma candidature " +
                                    "pour le poste de %s envoyée le %s.\n\n" +
                                    "Je reste très motivé(e) par cette opportunité et disponible pour tout entretien.\n\n" +
                                    "Cordialement,\n%s",
                            c.getOffreEmploi() != null ? c.getOffreEmploi().getTitre() : "votre offre",
                            c.getDateEnvoi() != null ? new java.text.SimpleDateFormat("dd/MM/yyyy").format(c.getDateEnvoi()) : "récemment",
                            c.getNomComplet() != null ? c.getNomComplet() : "Le candidat"
                    );

                    Map<String, Object> m = new HashMap<>();
                    m.put("id", c.getId());
                    m.put("offreTitre", c.getOffreEmploi() != null ? c.getOffreEmploi().getTitre() : "Candidature spontanée");
                    m.put("dateEnvoi", c.getDateEnvoi());
                    m.put("joursEcoules", joursEcoules);
                    m.put("urgence", urgence);
                    m.put("messageRelance", messageRelance);
                    return m;
                })
                .sorted((a, b) -> Long.compare((long) b.get("joursEcoules"), (long) a.get("joursEcoules")))
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // 7 — Gamification
    @GetMapping("/gamification")
    public ResponseEntity<Map<String, Object>> getGamification() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Candidat candidat = candidatRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé"));

        List<Candidature> candidatures = candidatureRepository.findByCandidatId(candidat.getId());

        long total = candidatures.size();
        long acceptees = candidatures.stream().filter(c -> "ACCEPTEE".equals(c.getStatut())).count();
        long refusees = candidatures.stream().filter(c -> "REFUSEE".equals(c.getStatut())).count();
        long enAttente = candidatures.stream().filter(c -> "EN_ATTENTE".equals(c.getStatut())).count();

        // Stats mensuelles
        Calendar cal = Calendar.getInstance();
        int currentMonth = cal.get(Calendar.MONTH);
        int currentYear = cal.get(Calendar.YEAR);

        long candidaturesCeMois = candidatures.stream().filter(c -> {
            if (c.getDateEnvoi() == null) return false;
            Calendar c2 = Calendar.getInstance();
            c2.setTime(c.getDateEnvoi());
            return c2.get(Calendar.MONTH) == currentMonth && c2.get(Calendar.YEAR) == currentYear;
        }).count();

        long accepteesCeMois = candidatures.stream().filter(c -> {
            if (c.getDateEnvoi() == null) return false;
            Calendar c2 = Calendar.getInstance();
            c2.setTime(c.getDateEnvoi());
            return "ACCEPTEE".equals(c.getStatut()) &&
                    c2.get(Calendar.MONTH) == currentMonth &&
                    c2.get(Calendar.YEAR) == currentYear;
        }).count();

        // Analyse des compétences
        Set<String> competences = new HashSet<>();
        candidatures.forEach(c -> {
            if (c.getCompetences() != null) {
                Arrays.stream(c.getCompetences().split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .forEach(competences::add);
            }
        });

        // ===== NOUVEAU CALCUL DE POINTS PLUS COMPLET =====
        int points = 0;

        // 1. Volume de candidatures (max 200 pts)
        points += Math.min(total * 8, 200);

        // 2. Succès (max 300 pts)
        points += (int)(acceptees * 60);

        // 3. Bonus pour taux de réussite (max 100 pts)
        double tauxReussite = total > 0 ? (double) acceptees / total * 100 : 0;
        if (tauxReussite >= 50) points += 50;
        else if (tauxReussite >= 25) points += 25;
        else if (tauxReussite > 0) points += 10;

        // 4. Bonus régularité (max 50 pts)
        points += Math.min(candidaturesCeMois * 10, 50);

        // 5. Bonus acceptations récentes (max 100 pts)
        points += accepteesCeMois * 30;

        // 6. Bonus diversité des compétences (max 50 pts)
        points += Math.min(competences.size() * 5, 50);

        // 7. Bonus anti-abandon (points de persévérance)
        if (refusees > 0 && acceptees > 0) points += 20;
        if (total >= 20) points += 30;

        // 8. Bonus CV/complétude
        boolean aCV = candidatures.stream().anyMatch(c -> c.getDocument() != null);
        boolean aLettre = candidatures.stream().anyMatch(c -> c.getLettreMotivation() != null && !c.getLettreMotivation().isEmpty());
        if (aCV) points += 30;
        if (aLettre) points += 20;

        // ===== DÉTERMINATION DU NIVEAU =====
        String niveau;
        String niveauSuivant;
        int niveauProgress;
        int pointsPourNiveauSuivant;

        if (points < 100) {
            niveau = "🥉 Débutant";
            niveauSuivant = "📌 Apprenti";
            niveauProgress = (int)(points * 100 / 100);
            pointsPourNiveauSuivant = 100 - points;
        } else if (points < 250) {
            niveau = "📌 Apprenti";
            niveauSuivant = "⚡ Intermédiaire";
            niveauProgress = (int)((points - 100) * 100 / 150);
            pointsPourNiveauSuivant = 250 - points;
        } else if (points < 450) {
            niveau = "⚡ Intermédiaire";
            niveauSuivant = "🔥 Confirmé";
            niveauProgress = (int)((points - 250) * 100 / 200);
            pointsPourNiveauSuivant = 450 - points;
        } else if (points < 700) {
            niveau = "🔥 Confirmé";
            niveauSuivant = "🏆 Expert";
            niveauProgress = (int)((points - 450) * 100 / 250);
            pointsPourNiveauSuivant = 700 - points;
        } else if (points < 1000) {
            niveau = "🏆 Expert";
            niveauSuivant = "👑 Légende";
            niveauProgress = (int)((points - 700) * 100 / 300);
            pointsPourNiveauSuivant = 1000 - points;
        } else {
            niveau = "👑 Légende";
            niveauSuivant = "🏆 Maximum !";
            niveauProgress = 100;
            pointsPourNiveauSuivant = 0;
        }

        // ===== BADGES DYNAMIQUES =====
        List<Map<String, Object>> badges = new ArrayList<>();

        // Badges de volume
        addBadge(badges, "🎯", "Premier pas", "Première candidature", total >= 1);
        addBadge(badges, "📈", "Actif", "5 candidatures", total >= 5);
        addBadge(badges, "🚀", "En mission", "10 candidatures", total >= 10);
        addBadge(badges, "💪", "Persévérant", "20 candidatures", total >= 20);

        // Badges de succès
        addBadge(badges, "🏆", "Premier succès", "1ère acceptation", acceptees >= 1);
        addBadge(badges, "⭐", "En demande", "3 acceptations", acceptees >= 3);
        addBadge(badges, "👑", "Star", "5 acceptations", acceptees >= 5);

        // Badges de performance
        if (tauxReussite >= 50) {
            addBadge(badges, "🎯", "Précis", "Taux réussite > 50%", true);
        }
        if (tauxReussite >= 75) {
            addBadge(badges, "🎖️", "Elite", "Taux réussite > 75%", true);
        }

        // Badges de régularité
        addBadge(badges, "📅", "Régulier", "3 candidatures ce mois", candidaturesCeMois >= 3);
        addBadge(badges, "🔥", "En feu", "5 candidatures ce mois", candidaturesCeMois >= 5);

        // Badges de compétences
        if (competences.size() >= 5) {
            addBadge(badges, "🧠", "Polyvalent", "5+ compétences", true);
        }
        if (competences.size() >= 10) {
            addBadge(badges, "🎓", "Expert", "10+ compétences", true);
        }

        // Badges spéciaux
        if (aCV && aLettre) {
            addBadge(badges, "📄", "Prêt", "CV + Lettre", true);
        }
        if (accepteesCeMois >= 1) {
            addBadge(badges, "⚡", "En forme", "Acceptation ce mois", true);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("points", points);
        result.put("niveau", niveau);
        result.put("niveauSuivant", niveauSuivant);
        result.put("niveauProgress", niveauProgress);
        result.put("pointsPourNiveauSuivant", pointsPourNiveauSuivant);
        result.put("badges", badges);
        result.put("candidaturesCeMois", candidaturesCeMois);
        result.put("tauxReussite", Math.round(tauxReussite));
        result.put("competencesCount", competences.size());

        // Détail des points pour transparence
        Map<String, Integer> details = new HashMap<>();
        details.put("base", (int)(total * 8));
        details.put("succes", (int)(acceptees * 60));
        details.put("tauxReussiteBonus", tauxReussite >= 50 ? 50 : (tauxReussite >= 25 ? 25 : 0));
        details.put("regularite", (int)Math.min(candidaturesCeMois * 10, 50));
        details.put("competences", Math.min(competences.size() * 5, 50));
        details.put("cv", aCV ? 30 : 0);
        details.put("lettre", aLettre ? 20 : 0);
        result.put("detailsPoints", details);

        return ResponseEntity.ok(result);
    }

    private void addBadge(List<Map<String, Object>> badges, String icon, String nom, String desc, boolean obtenu) {
        Map<String, Object> badge = new HashMap<>();
        badge.put("icon", icon);
        badge.put("nom", nom);
        badge.put("desc", desc);
        badge.put("obtenu", obtenu);
        badges.add(badge);
    }

    // 8 — Career Timeline
    @GetMapping("/timeline")
    public ResponseEntity<List<Map<String, Object>>> getTimeline() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Candidat candidat = candidatRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé"));

        List<Candidature> candidatures = candidatureRepository.findByCandidatId(candidat.getId());

        List<Map<String, Object>> result = candidatures.stream()
                .sorted((a, b) -> b.getDateEnvoi().compareTo(a.getDateEnvoi()))
                .map(c -> {
                    String icon = "ACCEPTEE".equals(c.getStatut()) ? "🏆" :
                            "REFUSEE".equals(c.getStatut()) ? "❌" : "⏳";
                    String couleur = "ACCEPTEE".equals(c.getStatut()) ? "#10b981" :
                            "REFUSEE".equals(c.getStatut()) ? "#ef4444" : "#f59e0b";

                    Map<String, Object> m = new HashMap<>();
                    m.put("id", c.getId());
                    m.put("nomComplet", c.getNomComplet());
                    m.put("statut", c.getStatut());
                    m.put("dateEnvoi", c.getDateEnvoi());
                    m.put("offreTitre", c.getOffreEmploi() != null ? c.getOffreEmploi().getTitre() : "Candidature spontanée");
                    m.put("icon", icon);
                    m.put("couleur", couleur);
                    return m;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }







    // ==================== METHODE UTILITAIRE ====================
    private CandidatureDTO convertToDTO(Candidature c) {
        CandidatureDTO dto = new CandidatureDTO();
        dto.setId(c.getId());
        dto.setDateEnvoi(c.getDateEnvoi());
        dto.setStatut(c.getStatut());
        dto.setLettreGeneree(c.getLettreGeneree());
        dto.setNomComplet(c.getNomComplet());
        dto.setEmail(c.getEmail());
        dto.setTelephone(c.getTelephone());
        dto.setDescription(c.getDescription());
        dto.setFormation(c.getFormation());
        dto.setExperience(c.getExperience());
        dto.setCompetences(c.getCompetences());
        dto.setLettreMotivation(c.getLettreMotivation());
        dto.setDateDisponibilite(c.getDateDisponibilite());
        dto.setPreavis(c.getPreavis());
        dto.setAcceptContact(c.getAcceptContact());
        dto.setAcceptRGPD(c.getAcceptRGPD());
        dto.setScoreEntretien(c.getScoreEntretien());
        dto.setTotalQuestionsEntretien(c.getTotalQuestionsEntretien());
        dto.setBonnesReponsesEntretien(c.getBonnesReponsesEntretien());
        dto.setDateEvaluationEntretien(c.getDateEvaluationEntretien());

        if (c.getCandidat() != null) {
            dto.setCandidatId(c.getCandidat().getId());
            dto.setCandidatNom(c.getCandidat().getNom());
        }

        if (c.getDocument() != null) {
            dto.setDocumentId(c.getDocument().getId());
            dto.setDocumentType(c.getDocument().getType().toString());
        }

        if (c.getOffreEmploi() != null) {
            dto.setOffreId(c.getOffreEmploi().getId());
            dto.setOffreTitre(c.getOffreEmploi().getTitre());
            dto.setEntreprise(c.getOffreEmploi().getEntreprise());
            dto.setPoste(c.getOffreEmploi().getTitre());
        }

        return dto;
    }
}