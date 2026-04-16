package t.esprit.arctic.jobmatch.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import t.esprit.arctic.jobmatch.entity.Formation;
import t.esprit.arctic.jobmatch.dto.FormationDTO;
import org.springframework.beans.BeanUtils;
import t.esprit.arctic.jobmatch.service.FormationService;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/formations")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class FormationController {

    private final FormationService formationService;

<<<<<<< HEAD
    // ── Endpoints admin (routes statiques AVANT routes dynamiques) ────────────

    // ✅ Liste complète pour admin (avec archivées)
=======

>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
    @GetMapping("/admin/all")
    public ResponseEntity<List<Formation>> getAllForAdmin() {
        return ResponseEntity.ok(formationService.getAllForAdmin());
    }

<<<<<<< HEAD
    // ✅ Liste des archivées uniquement
=======
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
    @GetMapping("/admin/archivees")
    public ResponseEntity<List<Formation>> getArchivees() {
        return ResponseEntity.ok(formationService.getArchivees());
    }

<<<<<<< HEAD
    // ── Endpoints publics filtrage (routes statiques AVANT routes dynamiques) ─
=======
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0

    @GetMapping("/niveau/{niveau}")
    public ResponseEntity<List<Formation>> getByNiveau(@PathVariable String niveau) {
        return ResponseEntity.ok(formationService.getByNiveau(niveau));
    }

    @GetMapping("/categorie/{categorie}")
    public ResponseEntity<List<Formation>> getByCategorie(@PathVariable String categorie) {
        return ResponseEntity.ok(formationService.getByCategorie(categorie));
    }

<<<<<<< HEAD
    // ── Endpoints publics génériques (routes génériques en dernier! ───────────

    // ✅ Liste publique — exclut les archivées (sans paramètres = doit être après les routes spécifiques)
=======

>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
    @GetMapping
    public ResponseEntity<List<Formation>> getAll() {
        return ResponseEntity.ok(formationService.getAllActives());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Formation> getById(@PathVariable Long id) {
        return ResponseEntity.ok(formationService.getById(id));
    }

    @PostMapping
    public ResponseEntity<Formation> create(@Valid @RequestBody FormationDTO formationDto) {
        Formation formation = new Formation();
        BeanUtils.copyProperties(formationDto, formation);
        return ResponseEntity.ok(formationService.create(formation));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Formation> update(@PathVariable Long id, @Valid @RequestBody FormationDTO formationDto) {
        Formation formation = new Formation();
        BeanUtils.copyProperties(formationDto, formation);
        return ResponseEntity.ok(formationService.update(id, formation));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        formationService.delete(id);
        return ResponseEntity.noContent().build();
    }

<<<<<<< HEAD
    // ✅ Archiver une formation
=======
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
    @PutMapping("/{id}/archiver")
    public ResponseEntity<Formation> archiver(@PathVariable Long id) {
        return ResponseEntity.ok(formationService.archiver(id));
    }

<<<<<<< HEAD
    // ✅ Désarchiver une formation
=======
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
    @PutMapping("/{id}/desarchiver")
    public ResponseEntity<Formation> desarchiver(@PathVariable Long id) {
        return ResponseEntity.ok(formationService.desarchiver(id));
    }
<<<<<<< HEAD
=======


    @GetMapping("/stats")
    public ResponseEntity<List<t.esprit.arctic.jobmatch.dto.FormationStatsDTO>> getAllStats() {
        return ResponseEntity.ok(
            formationService.getFormationsAvecStatistiques());
    }

    @GetMapping("/stats/categorie/{categorie}")
    public ResponseEntity<List<t.esprit.arctic.jobmatch.dto.FormationStatsDTO>> getStatsByCategorie(
            @PathVariable String categorie) {
        return ResponseEntity.ok(
            formationService.getStatsParCategorie(categorie));
    }

    @GetMapping("/top")
    public ResponseEntity<List<t.esprit.arctic.jobmatch.dto.FormationStatsDTO>> getTop() {
        return ResponseEntity.ok(formationService.getTopFormations());
    }

    @PostMapping("/refresh")
    public ResponseEntity<java.util.Map<String, Integer>> refreshBadgesAndScores() {
        return ResponseEntity.ok(formationService.refreshScoresEtBadges());
    }


    @GetMapping("/badge/{badge}")
    public ResponseEntity<List<Formation>> getByBadge(
            @PathVariable String badge) {
        return ResponseEntity.ok(
            formationService.getFormationsParBadge(badge));
    }

    @GetMapping("/populaires")
    public ResponseEntity<List<Formation>> getPopulaires(
            @RequestParam(defaultValue = "50") Double scoreMin) {
        return ResponseEntity.ok(
            formationService.getFormationsPopulaires(scoreMin));
    }
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
}