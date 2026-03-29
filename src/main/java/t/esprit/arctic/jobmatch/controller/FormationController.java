package t.esprit.arctic.jobmatch.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import t.esprit.arctic.jobmatch.entity.Formation;
import t.esprit.arctic.jobmatch.service.FormationService;
import java.util.List;

@RestController
@RequestMapping("/api/formations")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class FormationController {

    private final FormationService formationService;

    @GetMapping
    public ResponseEntity<List<Formation>> getAll() {
        return ResponseEntity.ok(formationService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Formation> getById(@PathVariable Long id) {
        return ResponseEntity.ok(formationService.getById(id));
    }

    @PostMapping
    public ResponseEntity<Formation> create(@RequestBody Formation formation) {
        return ResponseEntity.ok(formationService.create(formation));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Formation> update(@PathVariable Long id, @RequestBody Formation formation) {
        return ResponseEntity.ok(formationService.update(id, formation));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        formationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/niveau/{niveau}")
    public ResponseEntity<List<Formation>> getByNiveau(@PathVariable String niveau) {
        return ResponseEntity.ok(formationService.getByNiveau(niveau));
    }

    @GetMapping("/categorie/{categorie}")
    public ResponseEntity<List<Formation>> getByCategorie(@PathVariable String categorie) {
        return ResponseEntity.ok(formationService.getByCategorie(categorie));
    }
}