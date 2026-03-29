package t.esprit.arctic.jobmatch.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import t.esprit.arctic.jobmatch.entity.InscriptionFormation;
import java.util.List;

public interface InscriptionFormationRepository extends JpaRepository<InscriptionFormation, Long> {
    List<InscriptionFormation> findByCandidatId(Long candidatId);
    List<InscriptionFormation> findByFormationId(Long formationId);
    List<InscriptionFormation> findByStatut(String statut);
}