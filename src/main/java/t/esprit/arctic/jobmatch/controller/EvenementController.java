package t.esprit.arctic.jobmatch.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import t.esprit.arctic.jobmatch.dto.EvenementRequest;
import t.esprit.arctic.jobmatch.dto.EvenementResponse;
import t.esprit.arctic.jobmatch.service.EvenementService;

import java.util.List;

@RestController
@RequestMapping("/api/evenements")
@RequiredArgsConstructor
public class EvenementController {

    private final EvenementService service;

    // ================= GET ALL =================
    @GetMapping
    public ResponseEntity<List<EvenementResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    // ================= GET BY ID =================
    @GetMapping("/{id}")
    public ResponseEntity<EvenementResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    // GET par organisateur
    @GetMapping("/organisateur/{organisateurId}")
    public ResponseEntity<List<EvenementResponse>> getByOrganisateur(@PathVariable Long organisateurId) {
        return ResponseEntity.ok(service.getByOrganisateur(organisateurId));
    }

    // ================= CREATE / PUBLISH =================
    @PostMapping
    public ResponseEntity<EvenementResponse> publier(@RequestBody EvenementRequest request) {
        return ResponseEntity.ok(service.publier(request));
    }

    // ================= UPDATE / MODIFY =================
    @PutMapping("/{id}")
    public ResponseEntity<EvenementResponse> modifier(
            @PathVariable Long id,
            @RequestBody EvenementRequest request,
            Authentication authentication // récupère le user connecté
    ) {
        return ResponseEntity.ok(service.modifier(id, request, authentication.getName()));
    }

    // ================= DELETE / CANCEL =================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> annuler(
            @PathVariable Long id,
            Authentication authentication // récupère le user connecté
    ) {
        service.annuler(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}