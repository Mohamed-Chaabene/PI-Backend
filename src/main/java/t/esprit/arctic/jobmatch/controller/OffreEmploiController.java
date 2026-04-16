package t.esprit.arctic.jobmatch.controller;

import lombok.RequiredArgsConstructor;
<<<<<<< HEAD
=======
import org.springframework.dao.DataIntegrityViolationException;
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import t.esprit.arctic.jobmatch.entity.OffreEmploi;
<<<<<<< HEAD
import t.esprit.arctic.jobmatch.entity.Recruteur;
=======
import t.esprit.arctic.jobmatch.entity.Entretien;
import t.esprit.arctic.jobmatch.entity.Recruteur;
import t.esprit.arctic.jobmatch.repository.EntretienRepository;
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
import t.esprit.arctic.jobmatch.repository.OffreEmploiRepository;
import t.esprit.arctic.jobmatch.repository.RecruteurRepository;
import t.esprit.arctic.jobmatch.service.NotificationService;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/offres-emploi")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class OffreEmploiController {

    private final OffreEmploiRepository offreEmploiRepository;
<<<<<<< HEAD
=======
    private final EntretienRepository entretienRepository;
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
    private final RecruteurRepository recruteurRepository;
    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<OffreEmploi>> getAllOffres() {
        List<OffreEmploi> offres = offreEmploiRepository.findAll();
        offres.sort(Comparator.comparing(OffreEmploi::getDatePublication,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return ResponseEntity.ok(offres);
    }

    @GetMapping("/mes-offres")
    public ResponseEntity<List<OffreEmploi>> getMesOffres() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String identity = authentication.getName();
        if ("anonymousUser".equalsIgnoreCase(identity)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<OffreEmploi> offresParEmail = offreEmploiRepository
                .findByRecruteurEmailIgnoreCaseOrderByDatePublicationDesc(identity);
        if (!offresParEmail.isEmpty()) {
            return ResponseEntity.ok(offresParEmail);
        }

        return recruteurRepository.findByEmailIgnoreCase(identity)
                .map(recruteur -> {
                    List<OffreEmploi> offresParId = offreEmploiRepository
                            .findByRecruteurIdOrderByDatePublicationDesc(recruteur.getId());

                    if (!offresParId.isEmpty()) {
                        return ResponseEntity.ok(offresParId);
                    }

                    if (recruteur.getEntreprise() != null && !recruteur.getEntreprise().isBlank()) {
                        List<OffreEmploi> offresParEntreprise = offreEmploiRepository
                                .findByEntrepriseIgnoreCaseOrderByDatePublicationDesc(recruteur.getEntreprise());
                        if (!offresParEntreprise.isEmpty()) {
                            return ResponseEntity.ok(offresParEntreprise);
                        }
                    }

                    return ResponseEntity.ok(offresParId);
                })
                .orElseGet(() -> ResponseEntity.ok(List.of()));
    }

    @PostMapping
    public ResponseEntity<OffreEmploi> createOffre(@RequestBody OffreEmploi offrePayload) {
        Recruteur recruteur = getCurrentRecruteur();

        OffreEmploi offre = new OffreEmploi();
        offre.setTitre(offrePayload.getTitre());
        offre.setDescription(offrePayload.getDescription());
        offre.setEntreprise(offrePayload.getEntreprise() != null ? offrePayload.getEntreprise() : recruteur.getEntreprise());
        offre.setLocation(offrePayload.getLocation());
        offre.setSalary(offrePayload.getSalary());
        offre.setTypeContrat(offrePayload.getTypeContrat());
        offre.setDeadline(offrePayload.getDeadline());
        offre.setCompetencesRequises(offrePayload.getCompetencesRequises());
        offre.setImage(offrePayload.getImage());
        offre.setStatut(offrePayload.getStatut() != null ? offrePayload.getStatut() : "ACTIVE");
        offre.setDatePublication(new Date());
        offre.setRecruteur(recruteur);

        OffreEmploi savedOffre = offreEmploiRepository.save(offre);
        
        // Notify all followers of this recruiter about the new job
        notificationService.notifyFollowersOfNewJob(
            recruteur.getId(),
            recruteur.getNom(),
            offre.getTitre()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(savedOffre);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateOffre(@PathVariable Long id, @RequestBody OffreEmploi payload) {
        Recruteur recruteur = getCurrentRecruteur();
        OffreEmploi offre = offreEmploiRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Offre non trouvée"));

        if (offre.getRecruteur() == null || !offre.getRecruteur().getId().equals(recruteur.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Vous ne pouvez modifier que vos propres offres"));
        }

        offre.setTitre(payload.getTitre());
        offre.setDescription(payload.getDescription());
        offre.setEntreprise(payload.getEntreprise() != null ? payload.getEntreprise() : offre.getEntreprise());
        offre.setLocation(payload.getLocation());
        offre.setSalary(payload.getSalary());
        offre.setTypeContrat(payload.getTypeContrat());
        offre.setDeadline(payload.getDeadline());
        offre.setCompetencesRequises(payload.getCompetencesRequises());
        offre.setImage(payload.getImage());
        offre.setStatut(payload.getStatut() != null ? payload.getStatut() : offre.getStatut());

        return ResponseEntity.ok(offreEmploiRepository.save(offre));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOffre(@PathVariable Long id) {
        Recruteur recruteur = getCurrentRecruteur();
        OffreEmploi offre = offreEmploiRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Offre non trouvée"));

        if (offre.getRecruteur() == null || !offre.getRecruteur().getId().equals(recruteur.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Vous ne pouvez supprimer que vos propres offres"));
        }

<<<<<<< HEAD
        offreEmploiRepository.delete(offre);
        return ResponseEntity.noContent().build();
=======
        try {
            // Prevent FK violations: interviews keep history but no longer reference deleted offer.
            List<Entretien> linkedEntretiens = entretienRepository.findByOffreEmploiId(id);
            if (!linkedEntretiens.isEmpty()) {
                linkedEntretiens.forEach(entretien -> entretien.setOffreEmploi(null));
                entretienRepository.saveAll(linkedEntretiens);
            }

            offreEmploiRepository.delete(offre);
            return ResponseEntity.noContent().build();
        } catch (DataIntegrityViolationException ex) {
            // Last-resort fallback: mark offer inactive instead of crashing with 500.
            offre.setStatut("INACTIVE");
            offreEmploiRepository.save(offre);
            return ResponseEntity.ok(Map.of(
                    "archived", true,
                    "message", "Offre archivée car des références existent"
            ));
        }
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
    }

    private Recruteur getCurrentRecruteur() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Utilisateur non authentifie");
        }

        String email = authentication.getName();

        return recruteurRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("Recruteur non trouvé"));
    }
}