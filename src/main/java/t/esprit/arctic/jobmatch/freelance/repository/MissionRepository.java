package t.esprit.arctic.jobmatch.freelance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import t.esprit.arctic.jobmatch.freelance.entity.Mission;
import t.esprit.arctic.jobmatch.freelance.entity.MissionStatut;

import java.util.List;

public interface MissionRepository extends JpaRepository<Mission, Long> {
    List<Mission> findByStatut(MissionStatut statut);
    List<Mission> findByPublieParId(Long userId);
}