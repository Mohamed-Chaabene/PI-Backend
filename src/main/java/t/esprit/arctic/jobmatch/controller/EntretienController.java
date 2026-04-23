package t.esprit.arctic.jobmatch.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import t.esprit.arctic.jobmatch.dto.EntretienDTO;
import t.esprit.arctic.jobmatch.dto.EntretienTestPublicDto;
import t.esprit.arctic.jobmatch.service.EntretienService;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/entretiens")
@CrossOrigin(origins = "*")
public class EntretienController {

    @Autowired
    private EntretienService entretienService;

    @PostMapping
    public ResponseEntity<?> createEntretien(
            @Valid @RequestBody EntretienDTO dto,
            @RequestHeader("Recruteur-ID") Long recruteurId) {
        try {
            EntretienDTO created = entretienService.createEntretien(dto, recruteurId);
            return ResponseEntity.ok(created);
        } catch (RuntimeException ex) {
            if (ex.getMessage().contains("Recruteur non trouvé")) {
                return ResponseEntity.status(404).body("Recruteur non trouvé");
            }
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body("Erreur interne lors de la création de l'entretien");
        }
    }

    @GetMapping
    public ResponseEntity<List<EntretienDTO>> getAllEntretiens() {
        return ResponseEntity.ok(entretienService.getAllEntretiens());
    }

    @GetMapping("/public/tests")
    public ResponseEntity<List<EntretienTestPublicDto>> getPublicTestEntretiens() {
        return ResponseEntity.ok(entretienService.getPublicTestEntretiens());
    }

    @GetMapping("/recruteur/{recruteurId}")
    public ResponseEntity<List<EntretienDTO>> getEntretiensByRecruteur(@PathVariable Long recruteurId) {
        return ResponseEntity.ok(entretienService.getEntretiensByRecruteur(recruteurId));
    }

    @GetMapping("/candidat/{candidatId}")
    public ResponseEntity<List<EntretienDTO>> getEntretiensByCandidat(@PathVariable Long candidatId) {
        try {
            return ResponseEntity.ok(entretienService.getEntretiensByCandidat(candidatId));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.ok(List.of());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntretienDTO> getEntretien(@PathVariable Long id) {
        EntretienDTO entretien = entretienService.getEntretien(id);
        return entretien != null ? ResponseEntity.ok(entretien) : ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/submit-responses")
    public ResponseEntity<?> submitResponses(
            @PathVariable("id") Long entretienId,
            @RequestBody java.util.Map<String, Object> scoreData
    ) {
        try {
            // Récupère le score depuis le payload
            Double score = null;
            if (scoreData.containsKey("score")) {
                score = ((Number) scoreData.get("score")).doubleValue();
            }
            if (score == null) {
                return ResponseEntity.badRequest().body("Le score est obligatoire");
            }

            String commentaire = null;
            if (scoreData.containsKey("rapport") && scoreData.get("rapport") != null) {
                commentaire = String.valueOf(scoreData.get("rapport"));
            }

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String candidatEmail = authentication != null ? authentication.getName() : null;

            EntretienDTO resultat = entretienService.updateScore(entretienId, score, commentaire, candidatEmail);
            return ResponseEntity.ok(resultat);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body("Erreur interne pendant l'enregistrement du score");
        }
    }

    @GetMapping("/{id}/resultat")
    public ResponseEntity<EntretienDTO> getResultat(@PathVariable Long id) {
        EntretienDTO entretien = entretienService.getEntretien(id);
        return entretien != null ? ResponseEntity.ok(entretien) : ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<Void> markAsCompleted(@PathVariable Long id) {
        entretienService.markAsCompleted(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateEntretien(
            @PathVariable Long id,
            @Valid @RequestBody EntretienDTO dto,
            @RequestHeader("Recruteur-ID") Long recruteurId) {
        try {
            EntretienDTO updated = entretienService.updateEntretien(id, dto, recruteurId);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body("Erreur interne lors de la mise à jour de l'entretien");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEntretien(
            @PathVariable Long id,
            @RequestHeader("Recruteur-ID") Long recruteurId) {
        try {
            entretienService.deleteEntretien(id, recruteurId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body("Erreur interne lors de la suppression de l'entretien");
        }
    }
}
