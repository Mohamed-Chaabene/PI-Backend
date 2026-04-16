package t.esprit.arctic.jobmatch.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import t.esprit.arctic.jobmatch.dto.EvenementRequest;
import t.esprit.arctic.jobmatch.dto.EvenementResponse;
import t.esprit.arctic.jobmatch.dto.EvenementStatsResponse;
<<<<<<< HEAD
import t.esprit.arctic.jobmatch.service.EvenementService;
=======
import t.esprit.arctic.jobmatch.entity.Evenement;
import t.esprit.arctic.jobmatch.entity.Participation;
import t.esprit.arctic.jobmatch.service.EvenementService;
import t.esprit.arctic.jobmatch.service.ParticipationService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0

import java.util.List;

@RestController
@RequestMapping("/api/evenements")
@RequiredArgsConstructor
public class EvenementController {

    private final EvenementService service;
<<<<<<< HEAD

    // ================= GET ALL =================
=======
    private final ParticipationService participationService;


>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
    @GetMapping
    public ResponseEntity<List<EvenementResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

<<<<<<< HEAD
    // ================= GET BY ID =================
=======
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
    @GetMapping("/{id}")
    public ResponseEntity<EvenementResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

<<<<<<< HEAD
    // GET par organisateur
=======
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
    @GetMapping("/organisateur/{organisateurId}")
    public ResponseEntity<List<EvenementResponse>> getByOrganisateur(@PathVariable Long organisateurId) {
        return ResponseEntity.ok(service.getByOrganisateur(organisateurId));
    }

<<<<<<< HEAD
    // ================= CREATE / PUBLISH =================
=======
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
    @PostMapping
    public ResponseEntity<EvenementResponse> publier(@RequestBody EvenementRequest request) {
        return ResponseEntity.ok(service.publier(request));
    }

<<<<<<< HEAD
    // ================= UPDATE / MODIFY =================
=======
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
    @PutMapping("/{id}")
    public ResponseEntity<EvenementResponse> modifier(
            @PathVariable Long id,
            @RequestBody EvenementRequest request,
            Authentication authentication // récupère le user connecté
    ) {
        return ResponseEntity.ok(service.modifier(id, request, authentication.getName()));
    }

<<<<<<< HEAD
    // ================= DELETE / CANCEL =================
=======

>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> annuler(
            @PathVariable Long id,
            Authentication authentication // récupère le user connecté
    ) {
        service.annuler(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

<<<<<<< HEAD
    // DELETE /api/evenements/admin/{id} → pour l'admin
=======

>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Void> annulerAdmin(@PathVariable Long id) {
        service.annulerAdmin(id);
        return ResponseEntity.noContent().build();
    }

<<<<<<< HEAD
    //  GET /api/evenements/stats?mois=4&annee=2026&organisateurId=152
// Retourne les statistiques d'un organisateur pour un mois
=======

>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
    @GetMapping("/stats")
    public ResponseEntity<EvenementStatsResponse> getStats(
            @RequestParam int mois,
            @RequestParam int annee,
            @RequestParam Long organisateurId) {
        return ResponseEntity.ok(service.getStats(mois, annee, organisateurId));
    }
<<<<<<< HEAD
=======


    @GetMapping("/export-ics/confirmed/{candidatId}")
    public ResponseEntity<byte[]> exportConfirmed(@PathVariable Long candidatId) {


        List<Participation> participations = participationService.findConfirmedByCandidatId(candidatId , "CONFIRME");


        StringBuilder ics = new StringBuilder();
        ics.append("BEGIN:VCALENDAR\r\n")
                .append("VERSION:2.0\r\n")
                .append("PRODID:-//JobMatch//FR\r\n");


        for (Participation p : participations) {
            Evenement ev = p.getEvenement();
            ics.append("BEGIN:VEVENT\r\n")
                    .append("UID:").append(ev.getId()).append("@jobmatch\r\n")
                    .append("SUMMARY:").append(ev.getTitre()).append("\r\n")
                    .append("DESCRIPTION:").append(ev.getType() != null ? ev.getType() : "").append("\r\n")
                    .append("LOCATION:").append(ev.getLieu() != null ? ev.getLieu() : "").append("\r\n")
                    .append("DTSTART:").append(formatIcsDate(ev.getDateHeure())).append("\r\n")
                    .append("DTEND:").append(formatIcsDate(ev.getDateHeure().plusHours(2))).append("\r\n")
                    .append("END:VEVENT\r\n");
        }


        ics.append("END:VCALENDAR\r\n");


        byte[] bytes = ics.toString().getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"mes-evenements-confirmes.ics\"")
                .contentType(MediaType.parseMediaType("text/calendar; charset=utf-8"))
                .body(bytes);
    }

    private String formatIcsDate(LocalDateTime dt) {
        return dt.format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss"));
    }
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
}