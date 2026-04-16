package t.esprit.arctic.jobmatch.freelance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import t.esprit.arctic.jobmatch.freelance.entity.CandidatureMission;

import java.util.List;

public interface CandidatureMissionRepository extends JpaRepository<CandidatureMission, Long> {
    List<CandidatureMission> findByMissionId(Long missionId);
    List<CandidatureMission> findByCandidatId(Long candidatId);
    boolean existsByMissionIdAndCandidatId(Long missionId, Long candidatId);
    void deleteAllByMissionId(Long missionId);
}