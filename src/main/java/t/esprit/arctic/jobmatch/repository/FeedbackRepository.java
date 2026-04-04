package t.esprit.arctic.jobmatch.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import t.esprit.arctic.jobmatch.entity.Feedback;
import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    // Tous les feedbacks d'une formation
    List<Feedback> findByFormationId(Long formationId);

    // Tous les feedbacks d'un candidat
    List<Feedback> findByCandidatId(Long candidatId);
    
    // Tous les feedbacks d'une participation
    List<Feedback> findByParticipationId(Long participationId);

    // Feedbacks d'un candidat pour une formation précise
    List<Feedback> findByFormationIdAndCandidatId(Long formationId, Long candidatId);

    // Note moyenne d'une formation
    @Query("SELECT AVG(f.note) FROM Feedback f WHERE f.formation.id = :formationId")
    Double findNoteMoyenneByFormationId(@Param("formationId") Long formationId);

    // Vérifier si un candidat a déjà laissé un feedback sur une formation
    boolean existsByFormationIdAndCandidatId(Long formationId, Long candidatId);
}