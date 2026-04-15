package t.esprit.arctic.jobmatch.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import t.esprit.arctic.jobmatch.entity.FeedbackEvent;
import java.util.List;

public interface FeedbackEventRepository extends JpaRepository<FeedbackEvent, Long> {

    // Feedbacks d'une participation
    List<FeedbackEvent> findByParticipationId(Long participationId);

    // Feedbacks d'un événement via participation
    @Query("SELECT f FROM FeedbackEvent f WHERE f.participation.evenement.id = :evenementId")
    List<FeedbackEvent> findByEvenementId(@Param("evenementId") Long evenementId);

    // Vérifier si candidat a déjà laissé un feedback pour cette participation
    boolean existsByParticipationId(Long participationId);

    // Note moyenne d'un événement
    @Query("SELECT AVG(f.note) FROM FeedbackEvent f WHERE f.participation.evenement.id = :evenementId")
    Double findNoteMoyenneByEvenementId(@Param("evenementId") Long evenementId);

    // Tous les feedbacks d'un organisateur — données brutes
    @Query("""
    SELECT f FROM FeedbackEvent f
    JOIN f.participation p
    JOIN p.evenement e
    WHERE e.organisateur.id = :organisateurId
""")
    List<FeedbackEvent> findByOrganisateurId(@Param("organisateurId") Long organisateurId);

    // Tous les feedbacks d'un organisateur par type
    @Query("""
    SELECT f FROM FeedbackEvent f
    JOIN f.participation p
    JOIN p.evenement e
    WHERE e.organisateur.id = :organisateurId
    AND e.type = :type
""")
    List<FeedbackEvent> findByOrganisateurIdAndType(
            @Param("organisateurId") Long organisateurId,
            @Param("type") String type
    );

    // Tous les feedbacks d'un organisateur par titre
    @Query("""
    SELECT f FROM FeedbackEvent f
    JOIN f.participation p
    JOIN p.evenement e
    WHERE e.organisateur.id = :organisateurId
    AND e.titre = :titre
""")
    List<FeedbackEvent> findByOrganisateurIdAndTitre(
            @Param("organisateurId") Long organisateurId,
            @Param("titre") String titre
    );

    // À ajouter dans FeedbackEventRepository.java

    // Retourne les types d'événements les mieux notés par un candidat
// (note moyenne >= 4), triés du mieux noté au moins bien noté
    @Query("""
    SELECT e.type FROM FeedbackEvent f
    JOIN f.participation p
    JOIN p.evenement e
    WHERE p.candidat.id = :candidatId
    GROUP BY e.type
    HAVING AVG(f.note) >= 4
    ORDER BY AVG(f.note) DESC
""")
    List<String> findTypesFavorisParCandidat(@Param("candidatId") Long candidatId);
}