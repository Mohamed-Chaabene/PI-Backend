package t.esprit.arctic.jobmatch.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import t.esprit.arctic.jobmatch.dto.FeedbackEventRequest;
import t.esprit.arctic.jobmatch.dto.FeedbackEventResponse;
import t.esprit.arctic.jobmatch.service.FeedbackEventService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/feedbacks-evenement")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class FeedbackEventController {

    private final FeedbackEventService feedbackEventService;

    // Créer un feedback — CANDIDAT seulement
    @PostMapping
    public ResponseEntity<FeedbackEventResponse> create(
            @RequestBody FeedbackEventRequest request) {
        return ResponseEntity.ok(feedbackEventService.create(request));
    }

    // Feedbacks d'une participation
    @GetMapping("/participation/{participationId}")
    public ResponseEntity<List<FeedbackEventResponse>> getByParticipation(
            @PathVariable Long participationId) {
        return ResponseEntity.ok(feedbackEventService.getByParticipation(participationId));
    }

    // Feedbacks d'un événement — pour l'organisateur
    @GetMapping("/evenement/{evenementId}")
    public ResponseEntity<List<FeedbackEventResponse>> getByEvenement(
            @PathVariable Long evenementId) {
        return ResponseEntity.ok(feedbackEventService.getByEvenement(evenementId));
    }

    // Note moyenne d'un événement
    @GetMapping("/evenement/{evenementId}/moyenne")
    public ResponseEntity<Map<String, Object>> getNoteMoyenne(
            @PathVariable Long evenementId) {
        Double moyenne = feedbackEventService.getNoteMoyenne(evenementId);
        return ResponseEntity.ok(Map.of("moyenne", moyenne));
    }

    // Modifier un feedback
    @PutMapping("/{id}")
    public ResponseEntity<FeedbackEventResponse> update(
            @PathVariable Long id,
            @RequestBody FeedbackEventRequest request) {
        return ResponseEntity.ok(feedbackEventService.update(id, request));
    }

    // Supprimer un feedback
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        feedbackEventService.delete(id);
        return ResponseEntity.noContent().build();
    }
}