package t.esprit.arctic.jobmatch.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import t.esprit.arctic.jobmatch.entity.Document;
import t.esprit.arctic.jobmatch.repository.DocumentRepository;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentRepository documentRepository;

    // CREATE
    @PostMapping
    public ResponseEntity<Document> create(@RequestBody Document document) {
        Document saved = documentRepository.save(document);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    // READ ALL
    @GetMapping
    public ResponseEntity<List<Document>> getAll() {
        return ResponseEntity.ok(documentRepository.findAll());
    }

    // READ ONE
    @GetMapping("/{id}")
    public ResponseEntity<Document> getById(@PathVariable Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document non trouvé"));
        return ResponseEntity.ok(document);
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<Document> update(@PathVariable Long id, @RequestBody Document document) {
        Document existing = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document non trouvé"));

        existing.setNom(document.getNom());
        existing.setType(document.getType());
        existing.setContenu(document.getContenu());
        existing.setTemplate(document.getTemplate());
        existing.setCompatibleATS(document.getCompatibleATS());
        existing.setAjouterPhoto(document.getAjouterPhoto());

        return ResponseEntity.ok(documentRepository.save(existing));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        documentRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}