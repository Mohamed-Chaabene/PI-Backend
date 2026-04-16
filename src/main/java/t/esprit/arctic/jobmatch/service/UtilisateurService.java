package t.esprit.arctic.jobmatch.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
<<<<<<< HEAD
import t.esprit.arctic.jobmatch.entity.Utilisateur;
import t.esprit.arctic.jobmatch.repository.UtilisateurRepository;
=======
import org.springframework.transaction.annotation.Transactional;
import t.esprit.arctic.jobmatch.entity.Candidat;
import t.esprit.arctic.jobmatch.entity.Utilisateur;
import t.esprit.arctic.jobmatch.repository.CandidatRepository;
import t.esprit.arctic.jobmatch.repository.RechercheHistoriqueRepository;
import t.esprit.arctic.jobmatch.repository.UtilisateurRepository;
import jakarta.persistence.EntityManager;
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0

import java.util.List;
import java.util.stream.Collectors;

import t.esprit.arctic.jobmatch.dto.UtilisateurSearchDto;

@Service
@RequiredArgsConstructor
public class UtilisateurService {

    private final UtilisateurRepository repository;
<<<<<<< HEAD
=======
    private final CandidatRepository candidatRepository;
    private final RechercheHistoriqueRepository rechercheHistoriqueRepository;
    private final EntityManager entityManager;
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
    private final PasswordEncoder passwordEncoder;

    public Utilisateur register(Utilisateur user) {
        if (repository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Un compte avec cet email existe déjà");
        }
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
        
        if (!user.getEmail().equals(updatedUser.getEmail()) && repository.findByEmail(updatedUser.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Un compte avec cet email existe déjà");
        }
        
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
<<<<<<< HEAD
=======

    
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
}