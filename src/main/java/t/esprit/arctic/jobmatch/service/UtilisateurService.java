package t.esprit.arctic.jobmatch.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import t.esprit.arctic.jobmatch.entity.Utilisateur;
import t.esprit.arctic.jobmatch.repository.UtilisateurRepository;

import java.util.List;
import java.util.stream.Collectors;

import t.esprit.arctic.jobmatch.dto.UtilisateurSearchDto;

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

    public List<UtilisateurSearchDto> searchByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return List.of();
        }

        return repository.findByNomContainingIgnoreCase(name.trim())
                .stream()
                .map(user -> new UtilisateurSearchDto(
                        user.getId(),
                        user.getNom(),
                        user.getEmail(),
                        user.getRole() == null ? null : user.getRole().name()
                ))
                .collect(Collectors.toList());
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

    // Reset password by phone number
    public void resetPasswordByPhone(String phoneNumber, String newPassword) {
        Utilisateur user = repository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec le numéro: " + phoneNumber));
        
        user.setMotDePasse(passwordEncoder.encode(newPassword));
        repository.save(user);
    }
}