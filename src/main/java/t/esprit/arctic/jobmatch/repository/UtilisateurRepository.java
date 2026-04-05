package t.esprit.arctic.jobmatch.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

import t.esprit.arctic.jobmatch.entity.Role;
import t.esprit.arctic.jobmatch.entity.Utilisateur;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    Optional<Utilisateur> findByEmail(String email);

    Optional<Utilisateur> findByEmailIgnoreCase(String email);

    List<Utilisateur> findByNomIgnoreCase(String nom);

    List<Utilisateur> findByNomStartingWithIgnoreCase(String nom);

    List<Utilisateur> findByNomContainingIgnoreCase(String nom);

    List<Utilisateur> findByRole(Role role);

    List<Utilisateur> findByRoleIn(List<Role> roles);

}