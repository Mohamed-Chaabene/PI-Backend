package t.esprit.arctic.jobmatch.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import t.esprit.arctic.jobmatch.dto.QuestionDTO;
import t.esprit.arctic.jobmatch.service.QuestionService;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
@CrossOrigin(origins = "*")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @PostMapping("/entretien/{entretienId}")
    public ResponseEntity<?> createQuestion(
            @PathVariable Long entretienId,
            @Valid @RequestBody QuestionDTO questionDTO) {
        try {
            QuestionDTO created = questionService.createQuestion(questionDTO, entretienId);
            return ResponseEntity.ok(created);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body("Erreur interne lors de la création de la question");
        }
    }

    @GetMapping("/entretien/{entretienId}")
    public ResponseEntity<List<QuestionDTO>> getQuestionsByEntretien(@PathVariable Long entretienId) {
        return ResponseEntity.ok(questionService.getQuestionsByEntretien(entretienId));
    }

    @GetMapping("/entretien/{entretienId}/domaine/{domaineName}")
    public ResponseEntity<List<QuestionDTO>> getQuestionsByDomaine(
            @PathVariable Long entretienId,
            @PathVariable String domaineName) {
        return ResponseEntity.ok(questionService.getQuestionsByDomaine(entretienId, domaineName));
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuestionDTO> getQuestion(@PathVariable Long id) {
        QuestionDTO question = questionService.getQuestion(id);
        return question != null ? ResponseEntity.ok(question) : ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateQuestion(
            @PathVariable Long id,
            @Valid @RequestBody QuestionDTO questionDTO) {
        try {
            QuestionDTO updated = questionService.updateQuestion(id, questionDTO);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body("Erreur interne lors de la mise à jour de la question: " + ex.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return ResponseEntity.ok().build();
    }
}

