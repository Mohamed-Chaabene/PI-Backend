package t.esprit.arctic.jobmatch.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import t.esprit.arctic.jobmatch.dto.FeedbackRequest;
import t.esprit.arctic.jobmatch.dto.FeedbackResponse;
import t.esprit.arctic.jobmatch.service.FeedbackService;

import java.util.List;

@RestController
@RequestMapping("/api/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService service;

    @GetMapping
    public ResponseEntity<List<FeedbackResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FeedbackResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/participation/{participationId}")
    public ResponseEntity<List<FeedbackResponse>> getByParticipation(@PathVariable Long participationId) {
        return ResponseEntity.ok(service.getByParticipation(participationId));
    }

    @PostMapping
    public ResponseEntity<FeedbackResponse> create(@RequestBody FeedbackRequest request) {
        return ResponseEntity.ok(service.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FeedbackResponse> update(@PathVariable Long id,
                                                   @RequestBody FeedbackRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}