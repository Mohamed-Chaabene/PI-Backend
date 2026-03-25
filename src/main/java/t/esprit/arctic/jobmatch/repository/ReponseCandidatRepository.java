package t.esprit.arctic.jobmatch.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import t.esprit.arctic.jobmatch.entity.ReponseCandidat;
import t.esprit.arctic.jobmatch.entity.Candidat;
import t.esprit.arctic.jobmatch.entity.Entretien;

import java.util.List;

@Repository
public interface ReponseCandidatRepository extends JpaRepository<ReponseCandidat, Long> {
    List<ReponseCandidat> findByCandidatAndEntretien(Candidat candidat, Entretien entretien);
    List<ReponseCandidat> findByCandidat(Candidat candidat);
}

