package t.esprit.arctic.jobmatch.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import t.esprit.arctic.jobmatch.entity.Localisation;
import t.esprit.arctic.jobmatch.service.LocalisationService;

import java.util.List;

@RestController
@RequestMapping("/api/localisations")
@RequiredArgsConstructor
public class LocalisationController {

    private final LocalisationService service;

    @PostMapping
    public ResponseEntity<Localisation> create(@Valid @RequestBody Localisation localisation) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(localisation));
    }

    @GetMapping
    public ResponseEntity<List<Localisation>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Localisation> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Localisation> update(@PathVariable Long id, @Valid @RequestBody Localisation localisationDetails) {
        return ResponseEntity.ok(service.update(id, localisationDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

