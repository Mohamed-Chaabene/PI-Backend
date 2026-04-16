package t.esprit.arctic.jobmatch.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
<<<<<<< HEAD
import t.esprit.arctic.jobmatch.dto.EntretienCreateDTO;
=======
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
import t.esprit.arctic.jobmatch.dto.EntretienDTO;
import t.esprit.arctic.jobmatch.service.EntretienService;

@RestController
@RequestMapping("/api/recruteurs")
@CrossOrigin(origins = "*")
public class RecruteurEntretienController {

    @Autowired
    private EntretienService entretienService;

    @PostMapping("/{recruteurId}/entretiens")
    public ResponseEntity<?> createEntretienForRecruteur(
            @PathVariable Long recruteurId,
<<<<<<< HEAD
            @Valid @RequestBody EntretienCreateDTO dto,
=======
            @Valid @RequestBody EntretienDTO dto,
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
            @RequestHeader(value = "Recruteur-ID", required = false) Long recruteurIdHeader) {
        try {
            Long targetRecruteurId = recruteurIdHeader != null ? recruteurIdHeader : recruteurId;
            EntretienDTO created = entretienService.createEntretien(dto, targetRecruteurId);
            return ResponseEntity.ok(created);
        } catch (RuntimeException ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("Recruteur non trouvé")) {
                return ResponseEntity.status(404).body("Recruteur non trouvé");
            }
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body("Erreur interne lors de la création de l'entretien");
        }
    }
}
