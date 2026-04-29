package t.esprit.arctic.jobmatch.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import t.esprit.arctic.jobmatch.entity.NiveauOrdre;
import t.esprit.arctic.jobmatch.entity.QuizNiveau;

import java.util.List;
import java.util.Optional;

public interface QuizNiveauRepository extends JpaRepository<QuizNiveau, Long> {

    Optional<QuizNiveau> findTopByInscriptionParcoursIdAndNiveauOrderByTentativeDesc(
            Long inscriptionParcoursId, NiveauOrdre niveau);

    List<QuizNiveau> findByInscriptionParcoursIdOrderByDateTentativeDesc(Long inscriptionParcoursId);

    boolean existsByInscriptionParcoursIdAndNiveauAndReussiTrue(
            Long inscriptionParcoursId, NiveauOrdre niveau);

    int countByInscriptionParcoursIdAndNiveau(Long inscriptionParcoursId, NiveauOrdre niveau);

    boolean existsByInscriptionParcoursIdAndNiveau(Long inscriptionParcoursId, NiveauOrdre niveau);

    Optional<QuizNiveau> findFirstByInscriptionParcoursIdAndNiveauAndReussiTrueOrderByDateTentativeDesc(
            Long inscriptionParcoursId, NiveauOrdre niveau);
}
