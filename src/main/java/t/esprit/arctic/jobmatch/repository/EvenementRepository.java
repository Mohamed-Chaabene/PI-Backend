package t.esprit.arctic.jobmatch.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import t.esprit.arctic.jobmatch.entity.Evenement;
import java.util.List;

public interface EvenementRepository extends JpaRepository<Evenement, Long> {
    List<Evenement> findByType(String type);
    List<Evenement> findByLieu(String lieu);
    List<Evenement> findByOrganisateurId(Long organisateurId);
}