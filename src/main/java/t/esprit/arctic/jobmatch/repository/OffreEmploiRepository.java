package t.esprit.arctic.jobmatch.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import t.esprit.arctic.jobmatch.entity.OffreEmploi;
import java.util.List;

public interface OffreEmploiRepository extends JpaRepository<OffreEmploi, Long> {
    // You can add custom query methods here if needed
    List<OffreEmploi> findByTitreContainingIgnoreCase(String titre);
    List<OffreEmploi> findByStatut(String statut);
    List<OffreEmploi> findByRecruteurIdOrderByDatePublicationDesc(Long recruteurId);
    List<OffreEmploi> findByRecruteurEmailIgnoreCaseOrderByDatePublicationDesc(String email);
    List<OffreEmploi> findByEntrepriseIgnoreCaseOrderByDatePublicationDesc(String entreprise);
}