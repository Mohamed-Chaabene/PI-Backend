package t.esprit.arctic.jobmatch.repository;

import org.springframework.data.jpa.repository.JpaRepository;
<<<<<<< HEAD
import org.springframework.stereotype.Repository;
=======
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
import t.esprit.arctic.jobmatch.entity.Candidat;
import t.esprit.arctic.jobmatch.entity.Localisation;

import java.util.List;
import java.util.Optional;

@Repository
public interface CandidatRepository extends JpaRepository<Candidat, Long> {
    Optional<Candidat> findByEmail(String email);
    List<Candidat> findByLocalisation(Localisation localisation);
<<<<<<< HEAD
=======

    // JPQL to delete candidat by user ID (handles cascade)
    @Modifying
    @Transactional
    @Query("DELETE FROM Candidat c WHERE c.id = ?1")
    void deleteCandidatById(Long candidatId);
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
}

