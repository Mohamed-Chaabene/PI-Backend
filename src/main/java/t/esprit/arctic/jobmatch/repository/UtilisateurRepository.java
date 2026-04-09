package t.esprit.arctic.jobmatch.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


import t.esprit.arctic.jobmatch.entity.Candidat;

import t.esprit.arctic.jobmatch.entity.Role;
import java.util.List;

import t.esprit.arctic.jobmatch.entity.Utilisateur;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    Optional<Utilisateur> findByEmail(String email);
    List<Utilisateur> findByRole(Role role);

    List<Utilisateur> findByNomContainingIgnoreCase(String nom);

    // Find Candidat by phone number using JPQL (proper inheritance handling)
    @Query("SELECT c FROM Candidat c WHERE c.telephone = ?1")
    Optional<Utilisateur> findByPhoneNumber(String phoneNumber);
}