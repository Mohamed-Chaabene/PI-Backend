package t.esprit.arctic.jobmatch.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import t.esprit.arctic.jobmatch.dto.EntretienDTO;
import t.esprit.arctic.jobmatch.dto.EntretienCreateDTO;
import t.esprit.arctic.jobmatch.dto.ReponseDTO;
import t.esprit.arctic.jobmatch.dto.ResultatDTO;
import t.esprit.arctic.jobmatch.service.EntretienService;
import t.esprit.arctic.jobmatch.service.ReponseCandidatService;
import t.esprit.arctic.jobmatch.service.ResultatService;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/entretiens")
@CrossOrigin(origins = "*")
public class EntretienController {

    @Autowired
    private EntretienService entretienService;

    @Autowired
    private ReponseCandidatService reponseCandidatService;

    @Autowired
    private ResultatService resultatService;

    @PostMapping
    public ResponseEntity<EntretienDTO> createEntretien(
            @Valid @RequestBody EntretienCreateDTO dto,
            @RequestHeader("Recruteur-ID") Long recruteurId) {
        return ResponseEntity.ok(entretienService.createEntretien(dto, recruteurId));
    }

    @GetMapping
    public ResponseEntity<List<EntretienDTO>> getAllEntretiens() {
        return ResponseEntity.ok(entretienService.getAllEntretiens());
    }

    @GetMapping("/recruteur/{recruteurId}")
    public ResponseEntity<List<EntretienDTO>> getEntretiensByRecruteur(@PathVariable Long recruteurId) {
        return ResponseEntity.ok(entretienService.getEntretiensByRecruteur(recruteurId));
    }

    @GetMapping("/candidat/{candidatId}")
    public ResponseEntity<List<EntretienDTO>> getEntretiensByCandidat(@PathVariable Long candidatId) {
        return ResponseEntity.ok(entretienService.getEntretiensByCandidat(candidatId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntretienDTO> getEntretien(@PathVariable Long id) {
        EntretienDTO entretien = entretienService.getEntretien(id);
        return entretien != null ? ResponseEntity.ok(entretien) : ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/submit-responses")
    public ResponseEntity<?> submitResponses(
            @PathVariable("id") Long entretienId,
            @RequestBody List<t.esprit.arctic.jobmatch.dto.ReponseDTO> responses
    ) {
        try {
            if (responses == null || responses.isEmpty()) {
                return ResponseEntity.badRequest().body("Aucune réponse fournie");
            }

            // Use first candidate id (supposé identique dans toutes les réponses)
            Long candidatId = responses.get(0).getCandidatId();
            if (candidatId == null) {
                return ResponseEntity.badRequest().body("CandidatId requis dans les réponses");
            }

            reponseCandidatService.submitReponses(responses, entretienId);
            ResultatDTO resultat = resultatService.calculerResultat(entretienId, candidatId);
            return ResponseEntity.ok(resultat);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body("Erreur interne pendant la soumission des réponses");
        }
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<Void> markAsCompleted(@PathVariable Long id) {
        entretienService.markAsCompleted(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateEntretien(
            @PathVariable Long id,
            @Valid @RequestBody EntretienCreateDTO dto,
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
