package t.esprit.arctic.jobmatch.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import t.esprit.arctic.jobmatch.entity.Utilisateur;
import t.esprit.arctic.jobmatch.repository.UtilisateurRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UtilisateurService {

    private final UtilisateurRepository repository;
    private final PasswordEncoder passwordEncoder;

    public Utilisateur register(Utilisateur user) {
        user.setMotDePasse(passwordEncoder.encode(user.getMotDePasse()));
        return repository.save(user);
    }

    public List<Utilisateur> getAll() {
        return repository.findAll();
    }

    public Utilisateur getById(Long id) {
        return repository.findById(id).orElseThrow();
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Utilisateur update(Long id, Utilisateur updatedUser) {
        Utilisateur user = getById(id);
        user.setNom(updatedUser.getNom());
        user.setEmail(updatedUser.getEmail());
        if (updatedUser.getMotDePasse() != null && !updatedUser.getMotDePasse().isEmpty()) {
            user.setMotDePasse(passwordEncoder.encode(updatedUser.getMotDePasse()));
        }
        user.setRole(updatedUser.getRole());
        return repository.save(user);
    }
}