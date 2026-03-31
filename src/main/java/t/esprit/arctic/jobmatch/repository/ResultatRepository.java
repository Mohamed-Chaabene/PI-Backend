package t.esprit.arctic.jobmatch.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import t.esprit.arctic.jobmatch.entity.Resultat;
import t.esprit.arctic.jobmatch.entity.Entretien;

import java.util.Optional;

@Repository
public interface ResultatRepository extends JpaRepository<Resultat, Long> {
    Optional<Resultat> findByEntretien(Entretien entretien);
}

