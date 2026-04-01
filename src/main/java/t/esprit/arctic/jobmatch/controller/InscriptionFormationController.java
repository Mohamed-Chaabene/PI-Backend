package t.esprit.arctic.jobmatch.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import t.esprit.arctic.jobmatch.entity.InscriptionFormation;
import t.esprit.arctic.jobmatch.service.InscriptionFormationService;
import java.util.List;

@RestController
@RequestMapping("/api/inscriptions")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class InscriptionFormationController {

    private final InscriptionFormationService inscriptionService;

    @GetMapping
    public ResponseEntity<List<InscriptionFormation>> getAll() {
        return ResponseEntity.ok(inscriptionService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InscriptionFormation> getById(@PathVariable Long id) {
        return ResponseEntity.ok(inscriptionService.getById(id));
    }

    @PostMapping
    public ResponseEntity<InscriptionFormation> create(@RequestBody InscriptionFormation inscription) {
        return ResponseEntity.ok(inscriptionService.create(inscription));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InscriptionFormation> update(@PathVariable Long id,
                                                       @RequestBody InscriptionFormation inscription) {
        return ResponseEntity.ok(inscriptionService.update(id, inscription));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        inscriptionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/candidat/{candidatId}")
    public ResponseEntity<List<InscriptionFormation>> getByCandidat(@PathVariable Long candidatId) {
        return ResponseEntity.ok(inscriptionService.getByCandidat(candidatId));
    }

    @GetMapping("/formation/{formationId}")
    public ResponseEntity<List<InscriptionFormation>> getByFormation(@PathVariable Long formationId) {
        return ResponseEntity.ok(inscriptionService.getByFormation(formationId));
    }
}