package t.esprit.arctic.jobmatch.repository;

import org.springframework.data.jpa.repository.JpaRepository;
<<<<<<< HEAD
import t.esprit.arctic.jobmatch.entity.InscriptionFormation;
import java.util.List;

public interface InscriptionFormationRepository extends JpaRepository<InscriptionFormation, Long> {
    List<InscriptionFormation> findByCandidatId(Long candidatId);
    List<InscriptionFormation> findByFormationId(Long formationId);
    List<InscriptionFormation> findByStatut(String statut);
=======
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import t.esprit.arctic.jobmatch.entity.InscriptionFormation;
import java.util.List;
import java.util.Optional;

public interface InscriptionFormationRepository
        extends JpaRepository<InscriptionFormation, Long> {

    //  JOIN FETCH sur formation, candidat
    @Query("""
        SELECT DISTINCT i FROM InscriptionFormation i
        LEFT JOIN FETCH i.formation f
        LEFT JOIN FETCH f.competences
        LEFT JOIN FETCH i.candidat c
        WHERE i.candidat.id = :candidatId
    """)
    List<InscriptionFormation> findByCandidatId(@Param("candidatId") Long candidatId);

    @Query("""
        SELECT DISTINCT i FROM InscriptionFormation i
        LEFT JOIN FETCH i.formation f
        LEFT JOIN FETCH f.competences
        LEFT JOIN FETCH i.candidat c
        WHERE i.formation.id = :formationId
    """)
    List<InscriptionFormation> findByFormationId(@Param("formationId") Long formationId);

    @Query("""
        SELECT i FROM InscriptionFormation i
        LEFT JOIN FETCH i.formation f
        LEFT JOIN FETCH i.candidat c
        WHERE i.candidat.id = :candidatId
        AND i.formation.id = :formationId
    """)
    Optional<InscriptionFormation> findByCandidatIdAndFormationId(
            @Param("candidatId") Long candidatId,
            @Param("formationId") Long formationId);
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
}