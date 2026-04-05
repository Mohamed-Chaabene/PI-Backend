package t.esprit.arctic.jobmatch.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import t.esprit.arctic.jobmatch.dto.ParticipationRequest;
import t.esprit.arctic.jobmatch.dto.ParticipationResponse;
import t.esprit.arctic.jobmatch.service.ParticipationService;

import java.util.List;

@RestController
@RequestMapping("/api/participations")
@RequiredArgsConstructor
public class ParticipationController {

    private final ParticipationService service;

    @GetMapping
    public ResponseEntity<List<ParticipationResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParticipationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    // confirmer() → POST
    @PostMapping
    public ResponseEntity<ParticipationResponse> confirmer(@RequestBody ParticipationRequest request) {
        return ResponseEntity.ok(service.confirmer(request));
    }

    // annuler() → PUT
    @PutMapping("/{id}/annuler")
    public ResponseEntity<ParticipationResponse> annuler(@PathVariable Long id) {
        return ResponseEntity.ok(service.annuler(id));
    }

    @GetMapping("/evenement/{evenementId}")
    public ResponseEntity<List<ParticipationResponse>> getByEvenement(@PathVariable Long evenementId) {
        return ResponseEntity.ok(service.getByEvenement(evenementId));
    }
}

