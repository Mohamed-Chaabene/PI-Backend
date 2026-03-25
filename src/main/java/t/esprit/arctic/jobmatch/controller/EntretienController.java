package t.esprit.arctic.jobmatch.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import t.esprit.arctic.jobmatch.dto.EntretienDTO;
import t.esprit.arctic.jobmatch.dto.EntretienCreateDTO;
import t.esprit.arctic.jobmatch.service.EntretienService;

import java.util.List;

@RestController
@RequestMapping("/api/entretiens")
@CrossOrigin(origins = "*")
public class EntretienController {

    @Autowired
    private EntretienService entretienService;

    @PostMapping
    public ResponseEntity<EntretienDTO> createEntretien(
            @RequestBody EntretienCreateDTO dto,
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

    @PutMapping("/{id}/complete")
    public ResponseEntity<Void> markAsCompleted(@PathVariable Long id) {
        entretienService.markAsCompleted(id);
        return ResponseEntity.ok().build();
    }
}
