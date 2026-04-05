package t.esprit.arctic.jobmatch.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import t.esprit.arctic.jobmatch.entity.Role;
import t.esprit.arctic.jobmatch.entity.Utilisateur;
import t.esprit.arctic.jobmatch.repository.UtilisateurRepository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UtilisateurRoleSearchService {

    private final UtilisateurRepository repository;

    public List<Utilisateur> findByRole(Role role) {
        if (role == null) {
            return Collections.emptyList();
        }
        return repository.findByRole(role);
    }

    public List<Utilisateur> findByRoleName(String roleName) {
        if (roleName == null || roleName.isBlank()) {
            return Collections.emptyList();
        }
        Role role = Role.valueOf(roleName.trim().toUpperCase());
        return repository.findByRole(role);
    }

    public List<Utilisateur> findByRoleNames(List<String> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) {
            return Collections.emptyList();
        }
        List<Role> roles = roleNames.stream()
                .filter(name -> name != null && !name.isBlank())
                .map(String::trim)
                .map(String::toUpperCase)
                .map(Role::valueOf)
                .collect(Collectors.toList());
        if (roles.isEmpty()) {
            return Collections.emptyList();
        }
        return repository.findByRoleIn(roles);
    }

    public List<Utilisateur> findByNameOrEmail(String query) {
        if (query == null || query.isBlank()) {
            return Collections.emptyList();
        }
        String normalized = query.trim();

        if (normalized.contains("@")) {
            return repository.findByEmailIgnoreCase(normalized)
                    .map(List::of)
                    .orElse(Collections.emptyList());
        }

        List<Utilisateur> exact = repository.findByNomIgnoreCase(normalized);
        if (!exact.isEmpty()) {
            return exact;
        }

        List<Utilisateur> startsWith = repository.findByNomStartingWithIgnoreCase(normalized);
        if (!startsWith.isEmpty()) {
            return startsWith;
        }

        return repository.findByNomContainingIgnoreCase(normalized);
    }
}
