package t.esprit.arctic.jobmatch.controller;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import t.esprit.arctic.jobmatch.dto.CandidatStatsResponse;
import t.esprit.arctic.jobmatch.dto.ParticipationRequest;
import t.esprit.arctic.jobmatch.dto.ParticipationResponse;
<<<<<<< HEAD
import t.esprit.arctic.jobmatch.service.ParticipationService;

=======
import t.esprit.arctic.jobmatch.entity.Evenement;
import t.esprit.arctic.jobmatch.entity.Participation;
import t.esprit.arctic.jobmatch.service.ParticipationService;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0

import java.util.List;

@RestController
@RequestMapping("/api/participations")
@RequiredArgsConstructor
public class ParticipationController {

    private final ParticipationService service;


    @PostMapping
    public ResponseEntity<ParticipationResponse> confirmer(
            @RequestBody ParticipationRequest request) {
        return ResponseEntity.ok(service.confirmer(request));
    }
<<<<<<< HEAD
    //  Organisateur accepte
=======

>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
    @PutMapping("/{id}/accepter")
    public ResponseEntity<ParticipationResponse> accepter(@PathVariable Long id) {
        return ResponseEntity.ok(service.accepter(id));
    }

<<<<<<< HEAD
    //  Organisateur refuse
=======

>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
    @PutMapping("/{id}/refuser")
    public ResponseEntity<ParticipationResponse> refuser(@PathVariable Long id) {
        return ResponseEntity.ok(service.refuser(id));
    }
<<<<<<< HEAD
    // ✅ Annuler participation
=======

>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
    @PutMapping("/{id}/annuler")
    public ResponseEntity<ParticipationResponse> annuler(@PathVariable Long id) {
        return ResponseEntity.ok(service.annuler(id));
    }

<<<<<<< HEAD
    //  Demandes EN_ATTENTE par événement
=======

>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
    @GetMapping("/evenement/{evenementId}/demandes")
    public ResponseEntity<List<ParticipationResponse>> getDemandes(
            @PathVariable Long evenementId) {
        return ResponseEntity.ok(service.getDemandesByEvenement(evenementId));
    }

<<<<<<< HEAD
    // Participations CONFIRMEES par événement
=======

>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
    @GetMapping("/evenement/{evenementId}/confirmees")
    public ResponseEntity<List<ParticipationResponse>> getConfirmees(
            @PathVariable Long evenementId) {
        return ResponseEntity.ok(service.getConfirmeesByEvenement(evenementId));
    }

<<<<<<< HEAD
    //  Toutes demandes pour un organisateur
=======

>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
    @GetMapping("/organisateur/{organisateurId}/demandes")
    public ResponseEntity<List<ParticipationResponse>> getDemandesByOrganisateur(
            @PathVariable Long organisateurId) {
        return ResponseEntity.ok(service.getDemandesByOrganisateur(organisateurId));
    }

<<<<<<< HEAD
    //  Organisateur voit les participations d'un événement
=======

>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
    @GetMapping("/evenement/{evenementId}")
    public ResponseEntity<List<ParticipationResponse>> getByEvenement(
            @PathVariable Long evenementId) {
        return ResponseEntity.ok(service.getByEvenement(evenementId));
    }

<<<<<<< HEAD
    //  Candidat voit ses participations
=======

>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
    @GetMapping("/candidat/{candidatId}")
    public ResponseEntity<List<ParticipationResponse>> getByCandidat(
            @PathVariable Long candidatId) {
        return ResponseEntity.ok(service.getByCandidat(candidatId));
    }

<<<<<<< HEAD
    //  GET /api/participations/stats/candidat/{candidatId}
=======

>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
    @GetMapping("/stats/candidat/{candidatId}")
    public ResponseEntity<CandidatStatsResponse> getStatsByCandidat(
            @PathVariable Long candidatId) {
        return ResponseEntity.ok(service.getStatsByCandidat(candidatId));
    }

    @GetMapping("/{id}/qrcode")
    @PreAuthorize("hasAnyAuthority('CANDIDAT', 'ORGANISATEUR')")
    public ResponseEntity<String> getQRCode(@PathVariable Long id) {
        String qr = service.getQRCode(id);
        return ResponseEntity.ok(qr);
    }
<<<<<<< HEAD
=======

    @GetMapping("/confirmed/{candidatId}")
    public ResponseEntity<List<Map<String, Object>>> getConfirmed(@PathVariable Long candidatId) {
        List<Participation> participations = service.findConfirmedByCandidatId(candidatId, "CONFIRME");

        List<Map<String, Object>> result = participations.stream().map(p -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", p.getId());
            map.put("statut", p.getStatut());


            Evenement ev = p.getEvenement();
            Map<String, Object> evMap = new HashMap<>();
            evMap.put("id", ev.getId());
            evMap.put("titre", ev.getTitre());
            evMap.put("dateHeure", ev.getDateHeure().toString());
            evMap.put("lieu", ev.getLieu());
            evMap.put("type", ev.getType());


            map.put("evenement", evMap);

            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
}