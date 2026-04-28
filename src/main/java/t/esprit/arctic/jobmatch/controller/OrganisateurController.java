package t.esprit.arctic.jobmatch.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import t.esprit.arctic.jobmatch.entity.OrganisateurEvenement;
import t.esprit.arctic.jobmatch.service.OrganisateurService;

import java.util.List;

@RestController
@RequestMapping("/api/organisateur")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OrganisateurController {

    private final OrganisateurService organisateurService;

    @GetMapping("/test")
    public String test() {
        return "ORGANISATEUR OK";
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrganisateurEvenement>> getAllOrganisateurs() {
        return ResponseEntity.ok(organisateurService.getAll());
    }

    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteOrganisateur(@PathVariable Long id) {
        organisateurService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/admin/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrganisateurEvenement> toggleStatut(@PathVariable Long id) {
        return ResponseEntity.ok(organisateurService.toggleStatut(id));
    }
}