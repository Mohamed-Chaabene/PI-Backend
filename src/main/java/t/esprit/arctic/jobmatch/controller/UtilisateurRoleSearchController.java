package t.esprit.arctic.jobmatch.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import t.esprit.arctic.jobmatch.entity.Role;
import t.esprit.arctic.jobmatch.entity.Utilisateur;
import t.esprit.arctic.jobmatch.service.UtilisateurRoleSearchService;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/search/utilisateurs")
@RequiredArgsConstructor
public class UtilisateurRoleSearchController {

    private final UtilisateurRoleSearchService service;

    public record UtilisateurSearchResult(Long id, String nom, String email, String role) {}

    @GetMapping("/role")
    public List<Utilisateur> searchByRole(@RequestParam String role) {
        return service.findByRoleName(role);
    }

    @GetMapping("/roles")
    public List<Utilisateur> searchByRoles(@RequestParam String roles) {
        List<String> roleNames = Arrays.stream(roles.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        return service.findByRoleNames(roleNames);
    }

    @GetMapping("/nom")
    public List<UtilisateurSearchResult> searchByName(@RequestParam String nom) {
        return service.findByNameOrEmail(nom).stream()
                .filter(user -> user.getRole() != Role.ADMIN)
                .map(user -> new UtilisateurSearchResult(
                        user.getId(),
                        user.getNom(),
                        user.getEmail(),
                        user.getRole() != null ? user.getRole().name() : null
                ))
                .collect(Collectors.toList());
    }
}
