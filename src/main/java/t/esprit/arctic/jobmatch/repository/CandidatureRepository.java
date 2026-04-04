package t.esprit.arctic.jobmatch.repository;

import t.esprit.arctic.jobmatch.entity.Candidature;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CandidatureRepository extends JpaRepository<Candidature, Long> {

    // Trouver les candidatures d'un candidat
    List<Candidature> findByCandidatId(Long candidatId);

    // Trouver les candidatures par statut
    List<Candidature> findByStatut(String statut);

    // Trier par date du plus récent au plus ancien
    List<Candidature> findAllByOrderByDateEnvoiDesc();
}

//Le repository me permet d’interagir avec la base de données sans écrire manuellement les requêtes SQL