package t.esprit.arctic.jobmatch.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import t.esprit.arctic.jobmatch.entity.Entretien;
import t.esprit.arctic.jobmatch.entity.CategorieEntretien;
import t.esprit.arctic.jobmatch.entity.Candidat;
import t.esprit.arctic.jobmatch.entity.Recruteur;

<<<<<<< HEAD
import java.util.List;
=======
import java.time.LocalDateTime;
import java.util.List;
import java.util.Collection;
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
import java.util.Optional;

@Repository
public interface EntretienRepository extends JpaRepository<Entretien, Long> {
    List<Entretien> findByCandidat(Candidat candidat);
    List<Entretien> findByCandidatId(Long candidatId);
    List<Entretien> findByRecruteur(Recruteur recruteur);
    List<Entretien> findByCategorie(CategorieEntretien categorie);
    List<Entretien> findByCandidatAndCompleted(Candidat candidat, boolean completed);
<<<<<<< HEAD

    List<Entretien> findByCategorieAndCompleted(CategorieEntretien categorie, boolean completed);
=======
    List<Entretien> findByOffreEmploiId(Long offreId);
    List<Entretien> findByOffreEmploiIdIn(Collection<Long> offreIds);

    List<Entretien> findByCategorieAndCompleted(CategorieEntretien categorie, boolean completed);
    
    // Scheduler methods
    List<Entretien> findByDateEntretienBetweenAndCompleted(LocalDateTime start, LocalDateTime end, boolean completed);
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
}

