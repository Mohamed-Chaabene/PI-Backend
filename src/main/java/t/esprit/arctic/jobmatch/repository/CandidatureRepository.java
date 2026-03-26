package t.esprit.arctic.jobmatch.repository;

import t.esprit.arctic.jobmatch.entity.Candidature;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CandidatureRepository extends JpaRepository<Candidature, Long> {

    // Trouver les candidatures d'un candidat
    List<Candidature> findByCandidatId(Long candidatId);

    // Trouver les candidatures par statut
    List<Candidature> findByStatut(String statut);

    // Rechercher par entreprise (contient, ignore la casse)
    List<Candidature> findByEntrepriseContainingIgnoreCase(String entreprise);

    // Trier par date du plus récent au plus ancien
    List<Candidature> findAllByOrderByDateEnvoiDesc();
}