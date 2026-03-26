package t.esprit.arctic.jobmatch.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

@RestController
@RequestMapping("/api/candidatures")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class CandidatureController {

    private final CandidatureRepository candidatureRepository;
    private final CandidatRepository candidatRepository;

    // ==================== CREATE ====================
    @PostMapping
    public ResponseEntity<CandidatureDTO> creerCandidature(@RequestBody CandidatureDTO dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Candidat candidat = candidatRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé"));

        Candidature candidature = new Candidature();
        candidature.setDateEnvoi(new Date());
        candidature.setStatut("EN_ATTENTE");
        candidature.setCandidat(candidat);

        // ⭐ AJOUTE CES LIGNES ⭐
        candidature.setEntreprise(dto.getEntreprise());
        candidature.setPoste(dto.getPoste());
        candidature.setLettreGeneree(dto.getLettreGeneree());

        Candidature saved = candidatureRepository.save(candidature);
        return new ResponseEntity<>(convertToDTO(saved), HttpStatus.CREATED);
    }

    // ==================== READ - ROUTES SPÉCIFIQUES (AVANT LA ROUTE GENERIQUE) ====================

    // GET /api/candidatures/mes-candidatures
    @GetMapping("/mes-candidatures")
    public ResponseEntity<List<CandidatureDTO>> getMesCandidatures() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        System.out.println("📧 [GET] Email: " + email);

        Candidat candidat = candidatRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé pour email: " + email));

        List<CandidatureDTO> candidatures = candidatureRepository.findByCandidatId(candidat.getId())
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(candidatures);
    }

    // GET /api/candidatures/stats
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Candidat candidat = candidatRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé pour email: " + email));

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

    // GET /api/candidatures/filtre/statut/{statut}
    @GetMapping("/filtre/statut/{statut}")
    public ResponseEntity<List<CandidatureDTO>> filtrerParStatut(@PathVariable String statut) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Candidat candidat = candidatRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé pour email: " + email));

        List<CandidatureDTO> resultats = candidatureRepository.findByCandidatId(candidat.getId())
                .stream()
                .filter(c -> c.getStatut().equals(statut))
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(resultats);
    }

    // GET /api/candidatures/tri/date
    @GetMapping("/tri/date")
    public ResponseEntity<List<CandidatureDTO>> trierParDate() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Candidat candidat = candidatRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé pour email: " + email));

        List<CandidatureDTO> resultats = candidatureRepository.findByCandidatId(candidat.getId())
                .stream()
                .sorted((c1, c2) -> c2.getDateEnvoi().compareTo(c1.getDateEnvoi()))
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(resultats);
    }

    // GET /api/candidatures/recherche
    @GetMapping("/recherche")
    public ResponseEntity<List<CandidatureDTO>> rechercherParEntreprise(@RequestParam String entreprise) {
        // Note: Cette recherche est basée sur l'ID du candidat connecté
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Candidat candidat = candidatRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé pour email: " + email));

        // Filtrage côté Java (à améliorer avec une requête JPQL)
        List<CandidatureDTO> resultats = candidatureRepository.findByCandidatId(candidat.getId())
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        // Pour l'instant, on retourne toutes les candidatures
        // À améliorer avec une vraie recherche par entreprise
        return ResponseEntity.ok(resultats);
    }

    // ==================== READ - ROUTE GENERIQUE (APRES LES ROUTES SPÉCIFIQUES) ====================

    // GET /api/candidatures/{id}
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
    public ResponseEntity<CandidatureDTO> modifierCandidature(
            @PathVariable Long id,
            @RequestBody CandidatureDTO dto) {

        Candidature candidature = candidatureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée"));

        // Vérifier que le candidat connecté est le propriétaire
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Candidat candidat = candidatRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé"));

        if (!candidature.getCandidat().getId().equals(candidat.getId())) {
            throw new RuntimeException("Vous ne pouvez pas modifier cette candidature");
        }

        // Modifier uniquement les champs autorisés
        candidature.setEntreprise(dto.getEntreprise());
        candidature.setPoste(dto.getPoste());
        candidature.setLettreGeneree(dto.getLettreGeneree());

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

    // ==================== METHODE UTILITAIRE ====================
    private CandidatureDTO convertToDTO(Candidature c) {
        CandidatureDTO dto = new CandidatureDTO();
        dto.setId(c.getId());
        dto.setDateEnvoi(c.getDateEnvoi());
        dto.setStatut(c.getStatut());
        dto.setLettreGeneree(c.getLettreGeneree());
        dto.setEntreprise(c.getEntreprise());
        dto.setPoste(c.getPoste());

        if (c.getCandidat() != null) {
            dto.setCandidatId(c.getCandidat().getId());
            dto.setCandidatNom(c.getCandidat().getNom());
        }

        // Ajouter les infos du document
        if (c.getDocument() != null) {
            dto.setDocumentId(c.getDocument().getId());
            dto.setDocumentType(c.getDocument().getType().toString());
        }

        // Ajouter les infos de l'offre d'emploi
        if (c.getOffreEmploi() != null) {
            dto.setOffreId(c.getOffreEmploi().getId());
            dto.setOffreTitre(c.getOffreEmploi().getTitre());
        }

        /* Ajouter les infos de l'entretien
        if (c.getEntretien() != null) {
            dto.setEntretienId(c.getEntretien().getId());
        }*/

        return dto;
    }
}