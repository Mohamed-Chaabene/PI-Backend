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

    // ── Endpoints admin (routes statiques AVANT routes dynamiques) ────────────

    // ✅ Liste complète pour admin (avec archivées)
    @GetMapping("/admin/all")
    public ResponseEntity<List<Formation>> getAllForAdmin() {
        return ResponseEntity.ok(formationService.getAllForAdmin());
    }

    // ✅ Liste des archivées uniquement
    @GetMapping("/admin/archivees")
    public ResponseEntity<List<Formation>> getArchivees() {
        return ResponseEntity.ok(formationService.getArchivees());
    }

    // ── Endpoints publics filtrage (routes statiques AVANT routes dynamiques) ─

    @GetMapping("/niveau/{niveau}")
    public ResponseEntity<List<Formation>> getByNiveau(@PathVariable String niveau) {
        return ResponseEntity.ok(formationService.getByNiveau(niveau));
    }

    @GetMapping("/categorie/{categorie}")
    public ResponseEntity<List<Formation>> getByCategorie(@PathVariable String categorie) {
        return ResponseEntity.ok(formationService.getByCategorie(categorie));
    }

    // ── Endpoints publics génériques (routes génériques en dernier! ───────────

    // ✅ Liste publique — exclut les archivées (sans paramètres = doit être après les routes spécifiques)
    @GetMapping
    public ResponseEntity<List<Formation>> getAll() {
        return ResponseEntity.ok(formationService.getAllActives());
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

    // ✅ Archiver une formation
    @PutMapping("/{id}/archiver")
    public ResponseEntity<Formation> archiver(@PathVariable Long id) {
        return ResponseEntity.ok(formationService.archiver(id));
    }

    // ✅ Désarchiver une formation
    @PutMapping("/{id}/desarchiver")
    public ResponseEntity<Formation> desarchiver(@PathVariable Long id) {
        return ResponseEntity.ok(formationService.desarchiver(id));
    }
}