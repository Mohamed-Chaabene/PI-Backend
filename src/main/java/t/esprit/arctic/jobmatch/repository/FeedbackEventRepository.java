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
}