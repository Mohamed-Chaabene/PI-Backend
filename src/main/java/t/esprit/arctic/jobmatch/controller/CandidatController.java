package t.esprit.arctic.jobmatch.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
<<<<<<< HEAD
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
=======
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
>>>>>>> origin/Entre_tien
import org.springframework.web.bind.annotation.*;
import t.esprit.arctic.jobmatch.entity.Candidat;
import t.esprit.arctic.jobmatch.service.CandidatService;

<<<<<<< HEAD
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
=======
>>>>>>> origin/Entre_tien
import java.util.List;

@RestController
@RequestMapping("/api/candidats")
@RequiredArgsConstructor
public class CandidatController {

    private final CandidatService service;
<<<<<<< HEAD
    private static final Logger logger = LoggerFactory.getLogger(CandidatController.class);
=======
>>>>>>> origin/Entre_tien

    @PostMapping
    public ResponseEntity<Candidat> create(@Valid @RequestBody Candidat candidat) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(candidat));
    }

    @GetMapping
    public ResponseEntity<List<Candidat>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

<<<<<<< HEAD
    @GetMapping("/email/{email}")
    public ResponseEntity<Candidat> getByEmail(@PathVariable String email) {
        return ResponseEntity.ok(service.findByEmail(email));
    }

    @GetMapping("/{id}/download-cv")
    public ResponseEntity<?> downloadCV(@PathVariable Long id) {
        try {
            logger.info("=== CV Download Request for ID: {} ===", id);
            
            Candidat candidat = service.getById(id);
            logger.info("Candidate found: {} (ID: {})", candidat.getNom(), candidat.getId());
            
            if (candidat.getCvUrl() == null || candidat.getCvUrl().trim().isEmpty()) {
                logger.warn("CV URL is null or empty for candidate ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("CV not found for this candidate");
            }
            
            String cvUrl = candidat.getCvUrl().trim();
            logger.info("CV URL: {}", cvUrl);
            
            // Redirect to the Cloudinary URL with attachment header
            return ResponseEntity.ok()
                    .header(HttpHeaders.LOCATION, cvUrl)
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"cv_" + candidat.getId() + ".pdf\"")
                    .build();
                    
        } catch (Exception e) {
            logger.error("Error downloading CV for ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

=======
>>>>>>> origin/Entre_tien
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
<<<<<<< HEAD
=======

    @GetMapping("/email/{email}")
    public ResponseEntity<Candidat> getByEmail(@PathVariable String email) {
        return ResponseEntity.ok(service.findByEmail(email));
    }
>>>>>>> origin/Entre_tien
}

