package t.esprit.arctic.jobmatch.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import t.esprit.arctic.jobmatch.dto.FeedbackEventRequest;
import t.esprit.arctic.jobmatch.dto.FeedbackEventResponse;
import t.esprit.arctic.jobmatch.dto.OrganisateurReputationResponse;
import t.esprit.arctic.jobmatch.service.FeedbackEventService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/feedbacks-evenement")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class FeedbackEventController {

    private final FeedbackEventService feedbackEventService;

<<<<<<< HEAD
    // Créer un feedback — CANDIDAT seulement
=======

>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
    @PostMapping
    public ResponseEntity<FeedbackEventResponse> create(
            @RequestBody FeedbackEventRequest request) {
        return ResponseEntity.ok(feedbackEventService.create(request));
    }

<<<<<<< HEAD
    // Feedbacks d'une participation
=======

>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
    @GetMapping("/participation/{participationId}")
    public ResponseEntity<List<FeedbackEventResponse>> getByParticipation(
            @PathVariable Long participationId) {
        return ResponseEntity.ok(feedbackEventService.getByParticipation(participationId));
    }

<<<<<<< HEAD
    // Feedbacks d'un événement — pour l'organisateur
=======

>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
    @GetMapping("/evenement/{evenementId}")
    public ResponseEntity<List<FeedbackEventResponse>> getByEvenement(
            @PathVariable Long evenementId) {
        return ResponseEntity.ok(feedbackEventService.getByEvenement(evenementId));
    }

<<<<<<< HEAD
    // Note moyenne d'un événement
=======

>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
    @GetMapping("/evenement/{evenementId}/moyenne")
    public ResponseEntity<Map<String, Object>> getNoteMoyenne(
            @PathVariable Long evenementId) {
        Double moyenne = feedbackEventService.getNoteMoyenne(evenementId);
        return ResponseEntity.ok(Map.of("moyenne", moyenne));
    }

<<<<<<< HEAD
    // Modifier un feedback
=======

>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
    @PutMapping("/{id}")
    public ResponseEntity<FeedbackEventResponse> update(
            @PathVariable Long id,
            @RequestBody FeedbackEventRequest request) {
        return ResponseEntity.ok(feedbackEventService.update(id, request));
    }

<<<<<<< HEAD
    // Supprimer un feedback
=======

>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        feedbackEventService.delete(id);
        return ResponseEntity.noContent().build();
    }

<<<<<<< HEAD
    // Réputation d'un organisateur pour un événement précis
=======
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
    @GetMapping("/reputation")
    public ResponseEntity<OrganisateurReputationResponse> getReputation(
            @RequestParam Long organisateurId,
            @RequestParam String nomOrganisateur,
            @RequestParam String type,
            @RequestParam String titre) {
        return ResponseEntity.ok(
                feedbackEventService.getReputation(
                        organisateurId, nomOrganisateur, type, titre
                )
        );
    }
}