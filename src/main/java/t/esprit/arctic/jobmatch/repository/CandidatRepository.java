package t.esprit.arctic.jobmatch.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import t.esprit.arctic.jobmatch.entity.Candidat;
<<<<<<< HEAD
import t.esprit.arctic.jobmatch.entity.Localisation;

import java.util.List;
=======

>>>>>>> origin/Entre_tien
import java.util.Optional;

@Repository
public interface CandidatRepository extends JpaRepository<Candidat, Long> {
    Optional<Candidat> findByEmail(String email);
<<<<<<< HEAD
    List<Candidat> findByLocalisation(Localisation localisation);
=======
>>>>>>> origin/Entre_tien
}

