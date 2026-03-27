package t.esprit.arctic.jobmatch.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import t.esprit.arctic.jobmatch.entity.Competence;

@Repository
public interface CompetenceRepository extends JpaRepository<Competence, Long> {
}

