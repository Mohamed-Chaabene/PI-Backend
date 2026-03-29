package t.esprit.arctic.jobmatch.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import t.esprit.arctic.jobmatch.entity.Formation;
import java.util.List;

public interface FormationRepository extends JpaRepository<Formation, Long> {
    List<Formation> findByNiveau(String niveau);
    List<Formation> findByCategorie(String categorie);
    List<Formation> findByStatut(String statut);
}