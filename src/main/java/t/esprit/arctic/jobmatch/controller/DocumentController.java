package t.esprit.arctic.jobmatch.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import t.esprit.arctic.jobmatch.entity.Candidat;
import t.esprit.arctic.jobmatch.entity.Document;
import t.esprit.arctic.jobmatch.entity.Utilisateur;
import t.esprit.arctic.jobmatch.repository.CandidatRepository;
import t.esprit.arctic.jobmatch.repository.DocumentRepository;
import t.esprit.arctic.jobmatch.repository.UtilisateurRepository;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentRepository documentRepository;
    private final CandidatRepository candidatRepository;
    private final UtilisateurRepository utilisateurRepository;

    // ✅ Récupérer le candidat connecté par son ID
    private Candidat getCandidatConnecte() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Non authentifié");
        }

        // auth.getName() = email (mis par ton JwtFilter)
        String email = auth.getName();

        // Trouver l'utilisateur par email → récupérer son ID
        Utilisateur user = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé : " + email));

        System.out.println("✅ Utilisateur connecté - ID: " + user.getId() + " | Email: " + email);

        // Trouver le candidat par ID
        return candidatRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé pour ID: " + user.getId()));
    }

    // CREATE
    @PostMapping
    public ResponseEntity<Document> create(@RequestBody Document document) {
        try {
            Candidat candidat = getCandidatConnecte();
            document.setCandidat(candidat);
            System.out.println("✅ Document lié au candidat ID: " + candidat.getId());
        } catch (Exception e) {
            System.out.println("⚠️ Liaison candidat échouée: " + e.getMessage());
        }
        Document saved = documentRepository.save(document);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    // READ ALL — uniquement les documents du candidat connecté
    @GetMapping
    public ResponseEntity<List<Document>> getAll() {
        try {
            Candidat candidat = getCandidatConnecte();
            System.out.println("📄 Documents pour candidat ID: " + candidat.getId());
            List<Document> docs = documentRepository.findByCandidatId(candidat.getId());
            return ResponseEntity.ok(docs);
        } catch (Exception e) {
            System.out.println("⚠️ Fallback getAll: " + e.getMessage());
            return ResponseEntity.ok(documentRepository.findAll());
        }
    }

    // READ ONE
    @GetMapping("/{id}")
    public ResponseEntity<Document> getById(@PathVariable Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document non trouvé"));
        try {
            Candidat candidat = getCandidatConnecte();
            if (document.getCandidat() != null &&
                    !document.getCandidat().getId().equals(candidat.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } catch (Exception ignored) {}
        return ResponseEntity.ok(document);
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<Document> update(@PathVariable Long id,
                                           @RequestBody Document document) {
        Document existing = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document non trouvé"));
        try {
            Candidat candidat = getCandidatConnecte();
            if (existing.getCandidat() != null &&
                    !existing.getCandidat().getId().equals(candidat.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } catch (Exception ignored) {}

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
        Document existing = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document non trouvé"));
        try {
            Candidat candidat = getCandidatConnecte();
            if (existing.getCandidat() != null &&
                    !existing.getCandidat().getId().equals(candidat.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } catch (Exception ignored) {}

        documentRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}