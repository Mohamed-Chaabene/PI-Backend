package t.esprit.arctic.jobmatch.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import t.esprit.arctic.jobmatch.entity.Certificat;
import t.esprit.arctic.jobmatch.service.CertificatService;
import t.esprit.arctic.jobmatch.repository.InscriptionFormationRepository;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/certificats")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class CertificatController {

    private final CertificatService              certificatService;
    private final InscriptionFormationRepository inscriptionRepo;

    @GetMapping
    public ResponseEntity<List<Certificat>> getAll() {
        return ResponseEntity.ok(certificatService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Certificat> getById(@PathVariable Long id) {
        return ResponseEntity.ok(certificatService.getById(id));
    }

    @GetMapping("/candidat/{candidatId}")
    public ResponseEntity<List<Certificat>> getByCandidat(
            @PathVariable Long candidatId) {
        return ResponseEntity.ok(
                certificatService.getByCandidat(candidatId));
    }

<<<<<<< HEAD
    // ✅ Générer certificat depuis une inscription (après quiz final réussi)
=======
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
    @PostMapping("/generer/{inscriptionId}")
    public ResponseEntity<?> genererDepuisInscription(
            @PathVariable Long inscriptionId) {
        return inscriptionRepo.findById(inscriptionId)
                .map(ins -> {
                    try {
                        Certificat cert =
                                certificatService.genererAutomatiquement(ins);
                        return ResponseEntity.ok(cert);
                    } catch (RuntimeException e) {
                        // Certificat déjà existant → le retourner
                        List<Certificat> certs =
                                certificatService.getByCandidat(
                                        ins.getCandidat().getId());
                        return ResponseEntity.ok(
                                certs.stream()
                                        .filter(c -> c.getInscription().getId()
                                                .equals(inscriptionId))
                                        .findFirst()
                                        .orElseThrow()
                        );
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

<<<<<<< HEAD
    // ── Téléchargement PDF ────────────────────────────────────────
=======
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
    @GetMapping("/{id}/telecharger")
    public ResponseEntity<byte[]> telecharger(@PathVariable Long id) {
        byte[] pdf = certificatService.genererPdf(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData(
                "attachment", "certificat-" + id + ".pdf");
        headers.setContentLength(pdf.length);
        return ResponseEntity.ok().headers(headers).body(pdf);
    }
}