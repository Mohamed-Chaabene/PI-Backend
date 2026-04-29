package t.esprit.arctic.jobmatch.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import t.esprit.arctic.jobmatch.entity.Utilisateur;
import t.esprit.arctic.jobmatch.entity.IdentityVerificationStatus;
import t.esprit.arctic.jobmatch.repository.UtilisateurRepository;

import java.util.List;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

import t.esprit.arctic.jobmatch.dto.UtilisateurSearchDto;

@Service
@RequiredArgsConstructor
public class UtilisateurService {

    private final UtilisateurRepository repository;
    private final PasswordEncoder passwordEncoder;

    public Utilisateur register(Utilisateur user) {
        if (repository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Un compte avec cet email existe déjà");
        }
        user.setMotDePasse(passwordEncoder.encode(user.getMotDePasse()));
        user.setEmailVerificationToken(UUID.randomUUID().toString());
        user.setEmailVerificationExpiry(LocalDateTime.now().plusDays(2));
        user.setEmailVerified(false);
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

    public Utilisateur getByEmail(String email) {
        return repository.findByEmail(email).orElseThrow();
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

    public boolean verifyEmailToken(String token) {
        if (token == null || token.isBlank()) return false;
        Utilisateur user = repository.findAll().stream()
                .filter(u -> token.equals(u.getEmailVerificationToken()))
                .findFirst()
                .orElse(null);
        if (user == null) return false;
        if (user.getEmailVerificationExpiry() != null && user.getEmailVerificationExpiry().isBefore(LocalDateTime.now())) {
            return false;
        }
        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationExpiry(null);
        repository.save(user);
        return true;
    }

    public String resendVerification(String email) {
        Utilisateur user = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        user.setEmailVerificationToken(UUID.randomUUID().toString());
        user.setEmailVerificationExpiry(LocalDateTime.now().plusDays(2));
        repository.save(user);
        return user.getEmailVerificationToken();
    }

    public void updateIdentityStatus(Long userId, IdentityVerificationStatus status) {
        Utilisateur user = getById(userId);
        user.setIdentityVerificationStatus(status);
        repository.save(user);
    }
}