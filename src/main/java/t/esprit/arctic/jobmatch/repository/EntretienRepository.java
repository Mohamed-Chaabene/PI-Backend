package t.esprit.arctic.jobmatch.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import t.esprit.arctic.jobmatch.entity.Entretien;
import t.esprit.arctic.jobmatch.entity.CategorieEntretien;
import t.esprit.arctic.jobmatch.entity.Candidat;
import t.esprit.arctic.jobmatch.entity.Recruteur;

import java.util.List;
import java.util.Optional;

@Repository
public interface EntretienRepository extends JpaRepository<Entretien, Long> {
    List<Entretien> findByCandidat(Candidat candidat);
    List<Entretien> findByRecruteur(Recruteur recruteur);
    List<Entretien> findByCategorie(CategorieEntretien categorie);
    List<Entretien> findByCandidatAndCompleted(Candidat candidat, boolean completed);
}

