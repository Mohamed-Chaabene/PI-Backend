package t.esprit.arctic.jobmatch.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import t.esprit.arctic.jobmatch.dto.CandidatListDto;
import t.esprit.arctic.jobmatch.entity.Candidat;

import java.util.List;
import java.util.Optional;

@Repository
public interface CandidatRepository extends JpaRepository<Candidat, Long> {

    @Query("SELECT new t.esprit.arctic.jobmatch.dto.CandidatListDto(c.id, c.nom, c.prenom, c.email, c.telephone) FROM Candidat c")
    List<CandidatListDto> findAllProjected();

    Optional<Candidat> findByEmail(String email);
}

