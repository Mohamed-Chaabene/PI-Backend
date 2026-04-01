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
import t.esprit.arctic.jobmatch.repository.CandidatRepository;
import t.esprit.arctic.jobmatch.repository.CandidatureRepository;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import t.esprit.arctic.jobmatch.repository.OffreEmploiRepository;
import t.esprit.arctic.jobmatch.entity.OffreEmploi;
@RestController
@RequestMapping("/api/candidatures")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class CandidatureController {

    private final CandidatureRepository candidatureRepository;
    private final CandidatRepository candidatRepository;
    private final OffreEmploiRepository offreEmploiRepository;
    // ==================== CREATE ====================
    @PostMapping
    public ResponseEntity<?> creerCandidature(@Valid @RequestBody CandidatureDTO dto, BindingResult result) {

        // 1. Validation des champs
        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            result.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest().body(errors);
        }

        // 2. Vérifier RGPD
        if (!dto.isAcceptRGPD()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Vous devez accepter les conditions RGPD"));
        }

        // 3. Récupérer le candidat connecté
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Candidat candidat = candidatRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé"));

        // 4. Créer la candidature
        Candidature candidature = new Candidature();
        candidature.setDateEnvoi(new Date());
        candidature.setStatut("EN_ATTENTE");
        candidature.setCandidat(candidat);

        if (dto.getOffreId() != null) {
            OffreEmploi offre = offreEmploiRepository.findById(dto.getOffreId())
                    .orElseThrow(() -> new RuntimeException("Offre d'emploi non trouvée avec ID: " + dto.getOffreId()));
            candidature.setOffreEmploi(offre);

            // Also set the offre title and entreprise for convenience
            if (offre.getTitre() != null) {
                dto.setOffreTitre(offre.getTitre());
            }
        }


        // 5. Ajouter les données du formulaire
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

        // 6. Sauvegarder
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
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

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

        long total = candidatures.size();
        long enAttente = candidatures.stream().filter(c -> "EN_ATTENTE".equals(c.getStatut())).count();
        long acceptees = candidatures.stream().filter(c -> "ACCEPTEE".equals(c.getStatut())).count();
        long refusees = candidatures.stream().filter(c -> "REFUSEE".equals(c.getStatut())).count();

        Map<String, Long> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("enAttente", enAttente);
        stats.put("acceptees", acceptees);
        stats.put("refusees", refusees);

        return ResponseEntity.ok(stats);
    }

    // ==================== READ - Filtrer par statut ====================
    @GetMapping("/filtre/statut/{statut}")
    public ResponseEntity<List<CandidatureDTO>> filtrerParStatut(@PathVariable String statut) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Candidat candidat = candidatRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé"));

        List<CandidatureDTO> resultats = candidatureRepository.findByCandidatId(candidat.getId())
                .stream()
                .filter(c -> c.getStatut().equals(statut))
                .map(this::convertToDTO)
                .collect(Collectors.toList());

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
                .stream()
                .sorted((c1, c2) -> c2.getDateEnvoi().compareTo(c1.getDateEnvoi()))
                .map(this::convertToDTO)
                .collect(Collectors.toList());

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
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

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

    // ==================== UPDATE - MODIFICATION COMPLÈTE ====================
    @PutMapping("/{id}")
    public ResponseEntity<?> modifierCandidature(
            @PathVariable Long id,
            @Valid @RequestBody CandidatureDTO dto,
            BindingResult result) {

        // LOG pour déboguer
        System.out.println("=== CONTROLLER - MODIFICATION ===");
        System.out.println("ID reçu: " + id);
        System.out.println("DTO reçu: " + dto);

        // Validation des champs
        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            result.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            System.err.println("Erreurs de validation: " + errors);
            return ResponseEntity.badRequest().body(errors);
        }

        // Vérifier que la candidature existe
        Candidature candidature = candidatureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée avec ID: " + id));

        // Vérifier que l'utilisateur est le propriétaire
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Candidat candidat = candidatRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé"));

        if (!candidature.getCandidat().getId().equals(candidat.getId())) {
            throw new RuntimeException("Vous ne pouvez pas modifier cette candidature");
        }

        System.out.println("Candidature trouvée - Avant modification:");
        System.out.println("  - nomComplet: " + candidature.getNomComplet());
        System.out.println("  - formation: " + candidature.getFormation());

        if (dto.getNomComplet() != null) {
            candidature.setNomComplet(dto.getNomComplet());
            System.out.println("  ✓ nomComplet mis à jour: " + dto.getNomComplet());
        }
        if (dto.getEmail() != null) {
            candidature.setEmail(dto.getEmail());
            System.out.println("  ✓ email mis à jour: " + dto.getEmail());
        }
        if (dto.getTelephone() != null) {
            candidature.setTelephone(dto.getTelephone());
            System.out.println("  ✓ telephone mis à jour: " + dto.getTelephone());
        }
        if (dto.getDescription() != null) {
            candidature.setDescription(dto.getDescription());
            System.out.println("  ✓ description mis à jour");
        }
        if (dto.getFormation() != null) {
            candidature.setFormation(dto.getFormation());
            System.out.println("  ✓ formation mis à jour: " + dto.getFormation());
        }
        if (dto.getExperience() != null) {
            candidature.setExperience(dto.getExperience());
            System.out.println("  ✓ experience mis à jour: " + dto.getExperience());
        }
        if (dto.getCompetences() != null) {
            candidature.setCompetences(dto.getCompetences());
            System.out.println("  ✓ competences mis à jour: " + dto.getCompetences());
        }
        if (dto.getLettreMotivation() != null) {
            candidature.setLettreMotivation(dto.getLettreMotivation());
            System.out.println("  ✓ lettreMotivation mis à jour");
        }
        if (dto.getDateDisponibilite() != null) {
            candidature.setDateDisponibilite(dto.getDateDisponibilite());
            System.out.println("  ✓ dateDisponibilite mis à jour: " + dto.getDateDisponibilite());
        }
        if (dto.getPreavis() != null) {
            candidature.setPreavis(dto.getPreavis());
            System.out.println("  ✓ preavis mis à jour: " + dto.getPreavis());
        }
        if (dto.getAcceptContact() != null) {
            candidature.setAcceptContact(dto.getAcceptContact());
            System.out.println("  ✓ acceptContact mis à jour: " + dto.getAcceptContact());
        }
        // RGPD - on met à jour même si normalement déjà accepté
        candidature.setAcceptRGPD(dto.isAcceptRGPD());

        // Sauvegarder
        System.out.println("Sauvegarde en cours...");
        Candidature updated = candidatureRepository.save(candidature);
        System.out.println(" Sauvegarde réussie - ID: " + updated.getId());

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
            throw new RuntimeException("Seul un recruteur peut modifier le statut d'une candidature");
        }

        Candidature candidature = candidatureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée"));

        if (!statut.equals("ACCEPTEE") && !statut.equals("REFUSEE")) {
            throw new RuntimeException("Statut invalide. Valeurs acceptées: ACCEPTEE ou REFUSEE");
        }

        candidature.setStatut(statut);
        Candidature updated = candidatureRepository.save(candidature);

        return ResponseEntity.ok(convertToDTO(updated));
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


    // Dans CandidatureController.java
    @PostMapping("/quick-apply")
    public ResponseEntity<?> quickApply(@RequestBody Map<String, Object> quickApplyData) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();

            Candidat candidat = candidatRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Candidat non trouvé"));

            // Créer la candidature
            Candidature candidature = new Candidature();
            candidature.setDateEnvoi(new Date());
            candidature.setStatut("EN_ATTENTE");
            candidature.setCandidat(candidat);
            candidature.setAcceptRGPD(true);

            // Récupérer et lier l'offre d'emploi
            OffreEmploi offre = null;
            if (quickApplyData.get("offreId") != null) {
                Long offreId = Long.valueOf(quickApplyData.get("offreId").toString());
                offre = offreEmploiRepository.findById(offreId)
                        .orElseThrow(() -> new RuntimeException("Offre non trouvée avec ID: " + offreId));
                candidature.setOffreEmploi(offre);
            }

            // Remplir les informations du candidat avec ce qui est disponible
            String nomComplet = "";
            if (candidat.getNom() != null) {
                nomComplet = candidat.getNom();
                if (candidat.getPrenom() != null) {
                    nomComplet = candidat.getPrenom() + " " + candidat.getNom();
                }
            } else {
                nomComplet = "Candidat";
            }

            candidature.setNomComplet(nomComplet);
            candidature.setEmail(candidat.getEmail()); // L'email est normalement disponible

            // Téléphone - vérifier s'il existe
            try {
                java.lang.reflect.Method getTelephone = candidat.getClass().getMethod("getTelephone");
                String telephone = (String) getTelephone.invoke(candidat);
                candidature.setTelephone(telephone != null ? telephone : "");
            } catch (Exception e) {
                // Si la méthode n'existe pas, mettre une valeur par défaut
                candidature.setTelephone("Non spécifié");
            }

            if (offre != null) {
                // Stocker le titre de l'offre et l'entreprise dans la lettre générée
                String lettreGeneree = String.format(
                        "CANDIDATURE POUR L'OFFRE\n" +
                                "================================\n" +
                                "Poste : %s\n" +
                                "Entreprise : %s\n" +
                                "Localisation : %s\n" +
                                "Type de contrat : %s\n" +
                                "Salaire : %s\n" +
                                "================================\n\n" +
                                "Description du poste :\n%s\n\n" +
                                "Date de candidature : %s\n" +
                                "Statut : En attente",
                        offre.getTitre() != null ? offre.getTitre() : "Non spécifié",
                        offre.getEntreprise() != null ? offre.getEntreprise() : "Non spécifiée",
                        offre.getLocation() != null ? offre.getLocation() : "Non spécifiée",
                        offre.getTypeContrat() != null ? offre.getTypeContrat() : "Non spécifié",
                        offre.getSalary() != null ? offre.getSalary() : "Non spécifié",
                        offre.getDescription() != null ? offre.getDescription() : "Aucune description fournie",
                        new Date().toString()
                );

                candidature.setLettreGeneree(lettreGeneree);
                candidature.setDescription("Candidature pour: " + offre.getTitre() + " chez " + offre.getEntreprise());
            } else {
                candidature.setLettreGeneree(quickApplyData.get("lettreGeneree").toString());
                candidature.setDescription("Candidature rapide");
            }

            // Sauvegarder
            Candidature saved = candidatureRepository.save(candidature);

            // Préparer la réponse avec tous les détails
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Candidature envoyée avec succès");
            response.put("id", saved.getId());

            if (offre != null) {
                response.put("offreTitre", offre.getTitre());
                response.put("entreprise", offre.getEntreprise());
                response.put("poste", offre.getTitre());
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("success", false);
            return ResponseEntity.badRequest().body(error);
        }
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

            // Extraire l'entreprise et le poste de la lettre générée ou de l'offre
            if (c.getOffreEmploi().getEntreprise() != null) {
                dto.setEntreprise(c.getOffreEmploi().getEntreprise());
            } else {
                // Essayer d'extraire de la lettre générée
                String lettre = c.getLettreGeneree();
                if (lettre != null && lettre.contains("Entreprise :")) {
                    int start = lettre.indexOf("Entreprise :") + 12;
                    int end = lettre.indexOf("\n", start);
                    if (end > start) {
                        dto.setEntreprise(lettre.substring(start, end).trim());
                    }
                }
            }

            dto.setPoste(c.getOffreEmploi().getTitre());
        }

        return dto;
    }
}