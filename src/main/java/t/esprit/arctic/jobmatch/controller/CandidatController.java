package t.esprit.arctic.jobmatch.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import t.esprit.arctic.jobmatch.dto.CandidatListDto;
import t.esprit.arctic.jobmatch.entity.Candidat;
import t.esprit.arctic.jobmatch.service.CandidatService;

import java.util.List;

@RestController
@RequestMapping("/api/candidats")
@RequiredArgsConstructor
public class CandidatController {

    private final CandidatService service;

    @PostMapping
    public ResponseEntity<Candidat> create(@Valid @RequestBody Candidat candidat) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(candidat));
    }

    @GetMapping
    public ResponseEntity<List<CandidatListDto>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Candidat> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Candidat> update(@PathVariable Long id, @Valid @RequestBody Candidat candidatDetails) {
        return ResponseEntity.ok(service.update(id, candidatDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<Candidat> getByEmail(@PathVariable String email) {
        return ResponseEntity.ok(service.findByEmail(email));
    }
}

