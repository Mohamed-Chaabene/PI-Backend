package t.esprit.arctic.jobmatch.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import t.esprit.arctic.jobmatch.dto.DomaineDTO;
import t.esprit.arctic.jobmatch.service.DomaineService;

import java.util.List;

@RestController
@RequestMapping("/api/domaines")
@CrossOrigin(origins = "*")
public class DomaineController {

    @Autowired
    private DomaineService domaineService;

    @PostMapping
    public ResponseEntity<DomaineDTO> createDomaine(@RequestBody DomaineDTO domaineDTO) {
        return ResponseEntity.ok(domaineService.createDomaine(domaineDTO));
    }

    @GetMapping
    public ResponseEntity<List<DomaineDTO>> getAllDomaines() {
        return ResponseEntity.ok(domaineService.getAllDomaines());
    }

    @GetMapping("/active")
    public ResponseEntity<List<DomaineDTO>> getActiveDomaines() {
        return ResponseEntity.ok(domaineService.getActiveDomaines());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DomaineDTO> getDomaine(@PathVariable Long id) {
        DomaineDTO domaine = domaineService.getDomaine(id);
        return domaine != null ? ResponseEntity.ok(domaine) : ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<DomaineDTO> updateDomaine(@PathVariable Long id, @RequestBody DomaineDTO domaineDTO) {
        DomaineDTO updated = domaineService.updateDomaine(id, domaineDTO);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDomaine(@PathVariable Long id) {
        domaineService.deleteDomaine(id);
        return ResponseEntity.ok().build();
    }
}

