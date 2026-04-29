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
import t.esprit.arctic.jobmatch.entity.Document;
import t.esprit.arctic.jobmatch.entity.OffreEmploi;
import t.esprit.arctic.jobmatch.repository.CandidatRepository;
import t.esprit.arctic.jobmatch.repository.CandidatureRepository;
import t.esprit.arctic.jobmatch.repository.OffreEmploiRepository;
import t.esprit.arctic.jobmatch.service.EmailService;

import java.time.LocalDateTime;
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
    private final EmailService emailService;

    @GetMapping("/admin/toutes")
    public ResponseEntity<List<CandidatureDTO>> getAllCandidaturesForAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthorized = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")
                        || authority.getAuthority().equals("ROLE_RECRUTEUR"));
        if (!isAuthorized) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        List<CandidatureDTO> candidatures = candidatureRepository.findAllByOrderByDateEnvoiDesc()
                .stream().map(this::convertToDTO).collect(Collectors.toList());
        return ResponseEntity.ok(candidatures);
    }

    @PostMapping
    public ResponseEntity<?> creerCandidature(@Valid @RequestBody CandidatureDTO dto, BindingResult result) {
        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            result.getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(errors);
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Candidat candidat = candidatRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé"));

        Candidature candidature = new Candidature();
        candidature.setDateEnvoi(LocalDateTime.now());
        candidature.setStatut("EN_ATTENTE");
        candidature.setCandidat(candidat);

        if (dto.getOffreId() != null) {
            OffreEmploi offre = offreEmploiRepository.findById(dto.getOffreId())
                    .orElseThrow(() -> new RuntimeException("Offre non trouvée"));
            candidature.setOffreEmploi(offre);
            if (offre.getTitre() != null) dto.setOffreTitre(offre.getTitre());
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

        try {
            String emailCandidat = candidat.getEmail();
            String candidatNom = (candidat.getPrenom() != null ? candidat.getPrenom() : "") + " "
                    + (candidat.getNom() != null ? candidat.getNom() : "");
            String posteNom = (saved.getOffreEmploi() != null && saved.getOffreEmploi().getTitre() != null)
                    ? saved.getOffreEmploi().getTitre() : "l'offre";
            emailService.envoyerConfirmationCandidature(emailCandidat, candidatNom.trim(), posteNom);
        } catch (Exception e) {
            System.out.println("Erreur envoi email: " + e.getMessage());
        }

        return new ResponseEntity<>(convertToDTO(saved), HttpStatus.CREATED);
    }

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

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Candidat candidat = candidatRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé"));

        List<Candidature> candidatures = candidatureRepository.findByCandidatId(candidat.getId());

        int currentMonth = LocalDateTime.now().getMonthValue();
        int currentYear = LocalDateTime.now().getYear();

        long total = candidatures.size();
        long enAttente = candidatures.stream().filter(c -> "EN_ATTENTE".equals(c.getStatut())).count();
        long acceptees = candidatures.stream().filter(c -> "ACCEPTEE".equals(c.getStatut())).count();
        long refusees = candidatures.stream().filter(c -> "REFUSEE".equals(c.getStatut())).count();
        long entretiens = candidatures.stream().filter(c -> "ENTRETIEN".equals(c.getStatut())).count();

        long candidaturesCeMois = candidatures.stream().filter(c -> {
            if (c.getDateEnvoi() == null) return false;
            return c.getDateEnvoi().getMonthValue() == currentMonth
                    && c.getDateEnvoi().getYear() == currentYear;
        }).count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("enAttente", enAttente);
        stats.put("acceptees", acceptees);
        stats.put("refusees", refusees);
        stats.put("entretiens", entretiens);
        stats.put("candidaturesCeMois", candidaturesCeMois);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/offre/{offreId}")
    public ResponseEntity<List<CandidatureDTO>> getCandidaturesByOffre(@PathVariable Long offreId) {
        List<CandidatureDTO> candidatures = candidatureRepository.findByOffreEmploiId(offreId)
                .stream().map(this::convertToDTO).collect(Collectors.toList());
        return ResponseEntity.ok(candidatures);
    }

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

    @GetMapping("/{id}")
    public ResponseEntity<CandidatureDTO> getCandidatureById(@PathVariable Long id) {
        Candidature candidature = candidatureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Candidat candidat = candidatRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé"));

        if (!candidature.getCandidat().getId().equals(candidat.getId()))
            throw new RuntimeException("Vous n'avez pas accès à cette candidature");

        return ResponseEntity.ok(convertToDTO(candidature));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> modifierCandidature(@PathVariable Long id,
                                                 @Valid @RequestBody CandidatureDTO dto, BindingResult result) {
        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            result.getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(errors);
        }

        Candidature candidature = candidatureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Candidat candidat = candidatRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé"));

        if (!candidature.getCandidat().getId().equals(candidat.getId()))
            throw new RuntimeException("Vous ne pouvez pas modifier cette candidature");

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

        return ResponseEntity.ok(convertToDTO(candidatureRepository.save(candidature)));
    }

    @PutMapping("/{id}/statut")
    public ResponseEntity<CandidatureDTO> modifierStatutCandidature(@PathVariable Long id,
                                                                    @RequestParam String statut) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isRecruteur = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_RECRUTEUR")
                        || a.getAuthority().equals("ROLE_ADMIN"));
        if (!isRecruteur) throw new RuntimeException("Seul un recruteur peut modifier le statut");

        Candidature candidature = candidatureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée"));

        if (!statut.equals("ACCEPTEE") && !statut.equals("REFUSEE"))
            throw new RuntimeException("Statut invalide");

        candidature.setStatut(statut);
        return ResponseEntity.ok(convertToDTO(candidatureRepository.save(candidature)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerCandidature(@PathVariable Long id) {
        Candidature candidature = candidatureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Candidat candidat = candidatRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé"));

        if (!candidature.getCandidat().getId().equals(candidat.getId()))
            throw new RuntimeException("Vous ne pouvez pas supprimer cette candidature");

        candidatureRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/quick-apply")
    public ResponseEntity<?> quickApply(@RequestBody Map<String, Object> quickApplyData) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            Candidat candidat = candidatRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Candidat non trouvé"));

            Candidature candidature = new Candidature();
            candidature.setDateEnvoi(LocalDateTime.now());
            candidature.setStatut("EN_ATTENTE");
            candidature.setCandidat(candidat);
            candidature.setAcceptRGPD(true);

            OffreEmploi offre = null;
            if (quickApplyData.get("offreId") != null) {
                Long offreId = Long.valueOf(quickApplyData.get("offreId").toString());
                offre = offreEmploiRepository.findById(offreId)
                        .orElseThrow(() -> new RuntimeException("Offre non trouvée"));
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
                                "Poste : %s\nEntreprise : %s\nDate : %s\nStatut : En attente",
                        offre.getTitre() != null ? offre.getTitre() : "Non spécifié",
                        offre.getEntreprise() != null ? offre.getEntreprise() : "Non spécifiée",
                        LocalDateTime.now()
                );
                candidature.setLettreGeneree(lettreGeneree);
                candidature.setDescription("Candidature pour: " + offre.getTitre());
            } else {
                candidature.setLettreGeneree(quickApplyData.get("lettreGeneree").toString());
                candidature.setDescription("Candidature rapide");
            }

            Candidature saved = candidatureRepository.save(candidature);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
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

    @GetMapping("/alertes")
    public ResponseEntity<List<Map<String, Object>>> getAlertes() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Candidat candidat = candidatRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé"));

        List<Candidature> candidatures = candidatureRepository.findByCandidatId(candidat.getId());
        long total = candidatures.size();
        long enAttente = candidatures.stream().filter(c -> "EN_ATTENTE".equals(c.getStatut())).count();
        long acceptees = candidatures.stream().filter(c -> "ACCEPTEE".equals(c.getStatut())).count();
        long refusees = candidatures.stream().filter(c -> "REFUSEE".equals(c.getStatut())).count();

        List<Map<String, Object>> alertes = new ArrayList<>();
        if (enAttente > 5) {
            Map<String, Object> a = new HashMap<>();
            a.put("type", "warning"); a.put("titre", "Candidatures en attente");
            a.put("message", "Vous avez " + enAttente + " candidatures en attente.");
            a.put("bouton", "Voir conseils"); a.put("action", "relancer");
            alertes.add(a);
        }
        if (acceptees > 0) {
            Map<String, Object> a = new HashMap<>();
            a.put("type", "success"); a.put("titre", "Félicitations !");
            a.put("message", "Vous avez " + acceptees + " candidature(s) acceptée(s).");
            a.put("bouton", "Préparer entretien"); a.put("action", "entretien");
            alertes.add(a);
        }
        if (total == 0) {
            Map<String, Object> a = new HashMap<>();
            a.put("type", "info"); a.put("titre", "Commencez votre recherche");
            a.put("message", "Découvrez les offres qui correspondent à vos compétences.");
            a.put("bouton", "Voir les offres"); a.put("action", "offres");
            alertes.add(a);
        }
        if (refusees > 2) {
            Map<String, Object> a = new HashMap<>();
            a.put("type", "info"); a.put("titre", "Besoin d'aide ?");
            a.put("message", "Plusieurs candidatures refusées. Conseils pour améliorer votre CV.");
            a.put("bouton", "Améliorer mon CV"); a.put("action", "cv");
            alertes.add(a);
        }
        return ResponseEntity.ok(alertes);
    }

    @GetMapping("/doublons")
    public ResponseEntity<List<List<CandidatureDTO>>> getDoublons() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Candidat candidat = candidatRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé"));

        List<Candidature> candidatures = candidatureRepository.findByCandidatId(candidat.getId());
        Map<String, List<Candidature>> emailCount = new HashMap<>();
        candidatures.forEach(c -> {
            if (c.getEmail() != null && !c.getEmail().isEmpty())
                emailCount.computeIfAbsent(c.getEmail().toLowerCase().trim(), k -> new ArrayList<>()).add(c);
        });

        List<List<CandidatureDTO>> doublons = emailCount.values().stream()
                .filter(group -> group.size() > 1)
                .map(group -> group.stream().map(this::convertToDTO).collect(Collectors.toList()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(doublons);
    }

    @GetMapping("/analyse-profil")
    public ResponseEntity<Map<String, Object>> getAnalyseProfil() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Candidat candidat = candidatRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé"));

        List<Candidature> candidatures = candidatureRepository.findByCandidatId(candidat.getId());
        boolean aCompetences = candidatures.stream().anyMatch(c -> c.getCompetences() != null && !c.getCompetences().isEmpty());
        boolean aExperience = candidatures.stream().anyMatch(c -> c.getExperience() != null && !c.getExperience().isEmpty());
        boolean aCV = candidatures.stream().anyMatch(c -> c.getDocument() != null);

        int score = 0;
        List<String> conseils = new ArrayList<>();
        if (aCompetences) score += 35; else conseils.add("Ajoutez vos compétences clés");
        if (aExperience) score += 35; else conseils.add("Renseignez votre expérience professionnelle");
        if (aCV) score += 30; else conseils.add("Téléchargez votre CV");

        Map<String, Object> result = new HashMap<>();
        result.put("scoreProfil", score);
        result.put("conseils", conseils);
        result.put("profilCompetences", aCompetences);
        result.put("profilExperience", aExperience);
        result.put("profilCV", aCV);
        return ResponseEntity.ok(result);
    }

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

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("acceptees", acceptees);
        result.put("refusees", refusees);
        result.put("enAttente", enAttente);
        result.put("tauxReussite", Math.round(tauxReussite));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/stats-par-mois")
    public ResponseEntity<List<Map<String, Object>>> getStatsParMois() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Candidat candidat = candidatRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé"));

        List<Candidature> candidatures = candidatureRepository.findByCandidatId(candidat.getId());
        String[] mois = {"Jan","Fév","Mar","Avr","Mai","Jun","Jul","Aoû","Sep","Oct","Nov","Déc"};
        int[] compteur = new int[12];

        // ✅ LocalDateTime — plus besoin de Calendar
        candidatures.forEach(c -> {
            if (c.getDateEnvoi() != null)
                compteur[c.getDateEnvoi().getMonthValue() - 1]++;
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

    @GetMapping("/smart-match")
    public ResponseEntity<List<Map<String, Object>>> getSmartMatch() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Candidat candidat = candidatRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé"));

        List<Candidature> mesCandidatures = candidatureRepository.findByCandidatId(candidat.getId());
        Set<String> mesCompetences = new HashSet<>();
        mesCandidatures.forEach(c -> {
            if (c.getCompetences() != null)
                Arrays.stream(c.getCompetences().split(",")).map(String::trim).map(String::toLowerCase).forEach(mesCompetences::add);
        });

        List<Map<String, Object>> result = offreEmploiRepository.findAll().stream().map(offre -> {
            String descOffre = ((offre.getDescription() != null ? offre.getDescription() : "") + " " +
                    (offre.getTitre() != null ? offre.getTitre() : "")).toLowerCase();
            long matches = mesCompetences.stream().filter(descOffre::contains).count();
            int score = mesCompetences.size() > 0 ? (int) Math.min((matches * 100) / mesCompetences.size(), 99) : 30;
            String label = score >= 70 ? "Excellent match" : score >= 40 ? "Bon match" : "Match partiel";
            Map<String, Object> m = new HashMap<>();
            m.put("offreId", offre.getId()); m.put("titrOffre", offre.getTitre());
            m.put("entreprise", offre.getEntreprise()); m.put("localisation", offre.getLocation());
            m.put("score", score); m.put("label", label);
            return m;
        }).sorted((a, b) -> (int) b.get("score") - (int) a.get("score")).limit(10).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/radar-competences")
    public ResponseEntity<Map<String, Object>> getRadarCompetences() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Candidat candidat = candidatRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé"));

        List<Candidature> candidatures = candidatureRepository.findByCandidatId(candidat.getId());
        Set<String> competences = new HashSet<>();
        candidatures.forEach(c -> {
            if (c.getCompetences() != null)
                Arrays.stream(c.getCompetences().split(",")).map(String::trim).filter(s -> !s.isEmpty()).forEach(competences::add);
        });

        long total = candidatures.size();
        long acceptees = candidatures.stream().filter(c -> "ACCEPTEE".equals(c.getStatut())).count();
        long avecExperience = candidatures.stream().filter(c -> c.getExperience() != null && !c.getExperience().isEmpty()).count();

        List<Map<String, Object>> radarData = new ArrayList<>();
        Map<String, Object> r1 = new HashMap<>(); r1.put("label", "Compétences techniques"); r1.put("valeur", Math.min(competences.size() * 15, 100)); radarData.add(r1);
        Map<String, Object> r2 = new HashMap<>(); r2.put("label", "Expérience"); r2.put("valeur", avecExperience > 0 ? 75 : 20); radarData.add(r2);
        Map<String, Object> r3 = new HashMap<>(); r3.put("label", "Candidatures"); r3.put("valeur", Math.min(total * 10, 100)); radarData.add(r3);
        Map<String, Object> r4 = new HashMap<>(); r4.put("label", "Taux de succès"); r4.put("valeur", total > 0 ? (int)((double) acceptees / total * 100) : 0); radarData.add(r4);

        Map<String, Object> result = new HashMap<>();
        result.put("radarData", radarData);
        result.put("competences", new ArrayList<>(competences));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/prediction-succes")
    public ResponseEntity<Map<String, Object>> getPredictionSucces() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Candidat candidat = candidatRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé"));

        List<Candidature> candidatures = candidatureRepository.findByCandidatId(candidat.getId());
        long total = candidatures.size();
        long acceptees = candidatures.stream().filter(c -> "ACCEPTEE".equals(c.getStatut())).count();

        int probabilite = 40;
        if (total > 0) probabilite += 10;
        if (total >= 5) probabilite += 10;
        if (acceptees > 0) probabilite += 15;
        boolean aDesCompetences = candidatures.stream().anyMatch(c -> c.getCompetences() != null && !c.getCompetences().isEmpty());
        boolean aLettre = candidatures.stream().anyMatch(c -> c.getLettreMotivation() != null && !c.getLettreMotivation().isEmpty());
        if (aDesCompetences) probabilite += 10;
        if (aLettre) probabilite += 10;
        probabilite = Math.min(probabilite, 95);

        List<String> pointsForts = new ArrayList<>();
        if (aDesCompetences) pointsForts.add("Compétences bien renseignées");
        if (aLettre) pointsForts.add("Lettre de motivation présente");
        if (acceptees > 0) pointsForts.add("Historique de succès");

        List<String> pointsAmeliorer = new ArrayList<>();
        if (!aDesCompetences) pointsAmeliorer.add("Ajoutez vos compétences");
        if (!aLettre) pointsAmeliorer.add("Rédigez une lettre de motivation");
        if (total < 3) pointsAmeliorer.add("Envoyez plus de candidatures");

        String[] moments = {"Mardi matin", "Mercredi matin", "Lundi après-midi", "Jeudi matin"};
        Map<String, Object> result = new HashMap<>();
        result.put("probabilite", probabilite);
        result.put("meilleurMoment", moments[(int)(Math.random() * moments.length)]);
        result.put("pointsForts", pointsForts);
        result.put("pointsAmeliorer", pointsAmeliorer);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/relances")
    public ResponseEntity<List<Map<String, Object>>> getRelances() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Candidat candidat = candidatRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé"));

        List<Candidature> candidatures = candidatureRepository.findByCandidatIdAndStatut(candidat.getId(), "EN_ATTENTE");

        List<Map<String, Object>> result = candidatures.stream().map(c -> {
                    long joursEcoules = 0;
                    if (c.getDateEnvoi() != null) {
                        // ✅ LocalDateTime — calcul sans Calendar
                        joursEcoules = java.time.temporal.ChronoUnit.DAYS.between(c.getDateEnvoi(), LocalDateTime.now());
                    }
                    String urgence = joursEcoules > 14 ? "haute" : joursEcoules > 7 ? "moyenne" : "basse";
                    String niveauRappel = joursEcoules > 21 ? "critique" : joursEcoules > 14 ? "urgent" : "normal";
                    String couleurRappel = joursEcoules > 21 ? "#ef4444" : joursEcoules > 14 ? "#f59e0b" : "#3b82f6";

                    String messageRelance = String.format(
                            "Bonjour,\n\nJe me permets de vous relancer concernant ma candidature pour le poste de %s.\n\nCordialement,\n%s",
                            c.getOffreEmploi() != null ? c.getOffreEmploi().getTitre() : "votre offre",
                            c.getNomComplet() != null ? c.getNomComplet() : "Le candidat"
                    );

                    Map<String, Object> m = new HashMap<>();
                    m.put("id", c.getId());
                    m.put("offreTitre", c.getOffreEmploi() != null ? c.getOffreEmploi().getTitre() : "Candidature spontanée");
                    m.put("dateEnvoi", c.getDateEnvoi());
                    m.put("joursEcoules", joursEcoules);
                    m.put("urgence", urgence);
                    m.put("messageRelance", messageRelance);
                    m.put("niveauRappel", niveauRappel);
                    m.put("couleurRappel", couleurRappel);
                    return m;
                }).sorted((a, b) -> Long.compare((long) b.get("joursEcoules"), (long) a.get("joursEcoules")))
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

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

        // ✅ LocalDateTime — plus besoin de Calendar
        int currentMonth = LocalDateTime.now().getMonthValue();
        int currentYear = LocalDateTime.now().getYear();

        long candidaturesCeMois = candidatures.stream().filter(c ->
                c.getDateEnvoi() != null &&
                        c.getDateEnvoi().getMonthValue() == currentMonth &&
                        c.getDateEnvoi().getYear() == currentYear).count();

        long accepteesCeMois = candidatures.stream().filter(c ->
                c.getDateEnvoi() != null &&
                        "ACCEPTEE".equals(c.getStatut()) &&
                        c.getDateEnvoi().getMonthValue() == currentMonth &&
                        c.getDateEnvoi().getYear() == currentYear).count();

        Set<String> competences = new HashSet<>();
        candidatures.forEach(c -> {
            if (c.getCompetences() != null)
                Arrays.stream(c.getCompetences().split(",")).map(String::trim).filter(s -> !s.isEmpty()).forEach(competences::add);
        });

        int points = 0;
        points += Math.min(total * 8, 200);
        points += (int)(acceptees * 60);
        double tauxReussite = total > 0 ? (double) acceptees / total * 100 : 0;
        if (tauxReussite >= 50) points += 50; else if (tauxReussite >= 25) points += 25; else if (tauxReussite > 0) points += 10;
        points += Math.min(candidaturesCeMois * 10, 50);
        points += accepteesCeMois * 30;
        points += Math.min(competences.size() * 5, 50);
        if (refusees > 0 && acceptees > 0) points += 20;
        if (total >= 20) points += 30;

        boolean aCV = candidatures.stream().anyMatch(c -> c.getDocument() != null);
        boolean aLettre = candidatures.stream().anyMatch(c -> c.getLettreMotivation() != null && !c.getLettreMotivation().isEmpty());
        if (aCV) points += 30;
        if (aLettre) points += 20;

        String niveau; String niveauSuivant; int niveauProgress; int pointsPourNiveauSuivant;
        if (points < 100) { niveau = "Débutant"; niveauSuivant = "Apprenti"; niveauProgress = points; pointsPourNiveauSuivant = 100 - points; }
        else if (points < 250) { niveau = "Apprenti"; niveauSuivant = "Intermédiaire"; niveauProgress = (points - 100) * 100 / 150; pointsPourNiveauSuivant = 250 - points; }
        else if (points < 450) { niveau = "Intermédiaire"; niveauSuivant = "Confirmé"; niveauProgress = (points - 250) * 100 / 200; pointsPourNiveauSuivant = 450 - points; }
        else if (points < 700) { niveau = "Confirmé"; niveauSuivant = "Expert"; niveauProgress = (points - 450) * 100 / 250; pointsPourNiveauSuivant = 700 - points; }
        else if (points < 1000) { niveau = "Expert"; niveauSuivant = "Légende"; niveauProgress = (points - 700) * 100 / 300; pointsPourNiveauSuivant = 1000 - points; }
        else { niveau = "Légende"; niveauSuivant = "Maximum !"; niveauProgress = 100; pointsPourNiveauSuivant = 0; }

        List<Map<String, Object>> badges = new ArrayList<>();
        addBadge(badges, "🌱", "Premier pas", "Première candidature", total >= 1);
        addBadge(badges, "📊", "Actif", "5 candidatures", total >= 5);
        addBadge(badges, "🎯", "En mission", "10 candidatures", total >= 10);
        addBadge(badges, "🏆", "Premier succès", "1ère acceptation", acceptees >= 1);
        addBadge(badges, "🔥", "En demande", "3 acceptations", acceptees >= 3);
        addBadge(badges, "📅", "Régulier", "3 candidatures ce mois", candidaturesCeMois >= 3);
        if (competences.size() >= 5) addBadge(badges, "🧠", "Polyvalent", "5+ compétences", true);
        if (aCV && aLettre) addBadge(badges, "✅", "Prêt", "CV + Lettre", true);

        Map<String, Object> result = new HashMap<>();
        result.put("points", points); result.put("niveau", niveau);
        result.put("niveauSuivant", niveauSuivant); result.put("niveauProgress", niveauProgress);
        result.put("pointsPourNiveauSuivant", pointsPourNiveauSuivant); result.put("badges", badges);
        result.put("candidaturesCeMois", candidaturesCeMois); result.put("tauxReussite", Math.round(tauxReussite));
        return ResponseEntity.ok(result);
    }

    private void addBadge(List<Map<String, Object>> badges, String icon, String nom, String desc, boolean obtenu) {
        Map<String, Object> badge = new HashMap<>();
        badge.put("icon", icon); badge.put("nom", nom); badge.put("desc", desc); badge.put("obtenu", obtenu);
        badges.add(badge);
    }

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
                    String couleur = "ACCEPTEE".equals(c.getStatut()) ? "#10b981" :
                            "REFUSEE".equals(c.getStatut()) ? "#ef4444" : "#f59e0b";
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", c.getId()); m.put("nomComplet", c.getNomComplet());
                    m.put("statut", c.getStatut()); m.put("dateEnvoi", c.getDateEnvoi());
                    m.put("offreTitre", c.getOffreEmploi() != null ? c.getOffreEmploi().getTitre() : "Candidature spontanée");
                    m.put("couleur", couleur);
                    return m;
                }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    // ==================== ARCHIVAGE ====================

    // ==================== ARCHIVAGE ====================

    @PutMapping("/{id}/archiver")
    public ResponseEntity<?> archiverCandidature(@PathVariable Long id) {
        Candidature c = candidatureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée"));
        c.setArchive(true);
        c.setArchiveDate(LocalDateTime.now());
        candidatureRepository.save(c);
        return ResponseEntity.ok(Map.of("message", "Candidature archivée"));
    }

    @PutMapping("/{id}/restaurer")
    public ResponseEntity<?> restaurerCandidature(@PathVariable Long id) {
        candidatureRepository.restaurerCandidature(id);
        return ResponseEntity.ok(Map.of("message", "Candidature restaurée"));
    }

    @GetMapping("/test-archivage")
    public ResponseEntity<?> testArchivage() {
        LocalDateTime dateLimite = LocalDateTime.now().minusDays(7);
        LocalDateTime maintenant = LocalDateTime.now();

        List<Candidature> eligibles = candidatureRepository.findCandidaturesPlusDe7Jours(dateLimite);
        int count = candidatureRepository.archiverCandidaturesPlusDe7Jours(dateLimite, maintenant);

        return ResponseEntity.ok(Map.of(
                "eligibles", eligibles.size(),
                "archivees", count
        ));
    }

    // ==================== JPQL ====================

    @GetMapping("/jpql/par-nom-candidat")
    public ResponseEntity<List<Map<String, Object>>> getCandidaturesByCandidatNom(@RequestParam String nom) {
        List<Object[]> results = candidatureRepository.findCandidaturesByCandidatNom(nom);
        List<Map<String, Object>> response = new ArrayList<>();
        for (Object[] row : results) {
            Candidature c = (Candidature) row[0]; Candidat ca = (Candidat) row[1];
            Map<String, Object> map = new HashMap<>();
            map.put("candidatureId", c.getId()); map.put("statut", c.getStatut());
            map.put("dateEnvoi", c.getDateEnvoi()); map.put("candidatNom", ca.getNom());
            response.add(map);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/jpql/par-entreprise")
    public ResponseEntity<List<Map<String, Object>>> getCandidaturesByOffreEntreprise(@RequestParam String entreprise) {
        List<Object[]> results = candidatureRepository.findCandidaturesByOffreEntreprise(entreprise);
        List<Map<String, Object>> response = new ArrayList<>();
        for (Object[] row : results) {
            Candidature c = (Candidature) row[0]; OffreEmploi o = (OffreEmploi) row[1];
            Map<String, Object> map = new HashMap<>();
            map.put("candidatureId", c.getId()); map.put("statut", c.getStatut());
            map.put("offreTitre", o.getTitre()); map.put("offreEntreprise", o.getEntreprise());
            response.add(map);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/jpql/avec-document-type")
    public ResponseEntity<List<Map<String, Object>>> getCandidaturesByDocumentType(@RequestParam String type) {
        List<Object[]> results = candidatureRepository.findCandidaturesByDocumentType(type);
        List<Map<String, Object>> response = new ArrayList<>();
        for (Object[] row : results) {
            Candidature c = (Candidature) row[0]; Document d = (Document) row[1];
            Map<String, Object> map = new HashMap<>();
            map.put("candidatureId", c.getId()); map.put("candidatNom", c.getNomComplet());
            map.put("documentNom", d.getNom());
            response.add(map);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/jpql/complet-par-statut")
    public ResponseEntity<List<Map<String, Object>>> getFullCandidaturesByStatut(@RequestParam String statut) {
        List<Object[]> results = candidatureRepository.findFullCandidaturesByStatut(statut);
        List<Map<String, Object>> response = new ArrayList<>();
        for (Object[] row : results) {
            Candidature c = (Candidature) row[0]; Candidat ca = (Candidat) row[1]; OffreEmploi o = (OffreEmploi) row[2];
            Map<String, Object> map = new HashMap<>();
            map.put("candidatureId", c.getId()); map.put("statut", c.getStatut());
            map.put("candidatNom", ca.getNom()); map.put("offreTitre", o.getTitre());
            response.add(map);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/jpql/toutes-avec-left-join")
    public ResponseEntity<List<Map<String, Object>>> getAllCandidaturesWithLeftJoin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthorized = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_RECRUTEUR"));
        if (!isAuthorized) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        List<Object[]> results = candidatureRepository.findAllCandidaturesWithLeftJoinDocument();
        List<Map<String, Object>> response = new ArrayList<>();
        for (Object[] row : results) {
            Candidature c = (Candidature) row[0]; Candidat ca = (Candidat) row[1]; Document d = (Document) row[2];
            Map<String, Object> map = new HashMap<>();
            map.put("candidatureId", c.getId()); map.put("candidatNom", ca.getNom());
            map.put("documentNom", d != null ? d.getNom() : "Aucun document");
            response.add(map);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/jpql/recherche-multi")
    public ResponseEntity<List<Map<String, Object>>> getCandidaturesByEntrepriseAndStatut(
            @RequestParam String entreprise, @RequestParam String statut) {
        List<Object[]> results = candidatureRepository.findCandidaturesByEntrepriseAndStatut(entreprise, statut);
        List<Map<String, Object>> response = new ArrayList<>();
        for (Object[] row : results) {
            Candidature c = (Candidature) row[0]; Candidat ca = (Candidat) row[1]; OffreEmploi o = (OffreEmploi) row[2];
            Map<String, Object> map = new HashMap<>();
            map.put("candidatureId", c.getId()); map.put("candidatNom", ca.getNom());
            map.put("offreTitre", o.getTitre());
            response.add(map);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/jpql/stats-par-candidat")
    public ResponseEntity<List<Map<String, Object>>> getStatsByCandidat() {
        List<Object[]> results = candidatureRepository.getStatsByCandidat();
        List<Map<String, Object>> response = new ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> map = new HashMap<>();
            map.put("candidatId", row[0]); map.put("candidatNom", row[1]);
            map.put("totalCandidatures", row[3]); map.put("totalAcceptees", row[4]);
            long total = (Long) row[3]; long acceptees = (Long) row[4];
            map.put("tauxReussite", Math.round(total > 0 ? acceptees * 100.0 / total : 0));
            response.add(map);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/jpql/high-salary")
    public ResponseEntity<List<Map<String, Object>>> getCandidaturesByMinSalary(@RequestParam Double minSalary) {
        List<Object[]> results = candidatureRepository.findCandidaturesByMinSalary(minSalary);
        List<Map<String, Object>> response = new ArrayList<>();
        for (Object[] row : results) {
            Candidature c = (Candidature) row[0]; Candidat ca = (Candidat) row[1]; OffreEmploi o = (OffreEmploi) row[2];
            Map<String, Object> map = new HashMap<>();
            map.put("candidatureId", c.getId()); map.put("candidatNom", ca.getNom());
            map.put("offreTitre", o.getTitre()); map.put("offreSalary", o.getSalary());
            response.add(map);
        }
        return ResponseEntity.ok(response);
    }

    // ==================== CONVERT TO DTO ====================

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

        // ✅ CHAMPS ARCHIVE — c'était la cause du undefined dans le front
        dto.setArchive(c.getArchive());
        dto.setArchiveDate(c.getArchiveDate());

        String statutLabel = switch (c.getStatut() != null ? c.getStatut() : "") {
            case "EN_ATTENTE" -> "En attente";
            case "ACCEPTEE" -> "Acceptée";
            case "REFUSEE" -> "Refusée";
            default -> c.getStatut() != null ? c.getStatut() : "";
        };
        String statutClass = switch (c.getStatut() != null ? c.getStatut() : "") {
            case "EN_ATTENTE" -> "pending";
            case "ACCEPTEE" -> "accepted";
            case "REFUSEE" -> "rejected";
            default -> "";
        };
        dto.setStatutLabel(statutLabel);
        dto.setStatutClass(statutClass);

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