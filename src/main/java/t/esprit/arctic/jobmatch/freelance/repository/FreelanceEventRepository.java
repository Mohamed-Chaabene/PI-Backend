package t.esprit.arctic.jobmatch.freelance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import t.esprit.arctic.jobmatch.freelance.entity.FreelanceEvent;

import java.time.LocalDateTime;
import java.util.List;

public interface FreelanceEventRepository extends JpaRepository<FreelanceEvent, Long> {

    @Query("SELECT e FROM FreelanceEvent e WHERE e.organizer.id = :userId OR e.participant.id = :userId ORDER BY e.startDate ASC")
    List<FreelanceEvent> findAllByUserId(@Param("userId") Long userId);

    @Query("SELECT e FROM FreelanceEvent e WHERE (e.organizer.id = :userId OR e.participant.id = :userId) " +
           "AND e.startDate >= :start AND e.endDate <= :end ORDER BY e.startDate ASC")
    List<FreelanceEvent> findByUserIdAndDateRange(@Param("userId") Long userId,
                                                   @Param("start") LocalDateTime start,
                                                   @Param("end") LocalDateTime end);

    List<FreelanceEvent> findByMissionId(Long missionId);

    void deleteAllByMissionId(Long missionId);
}
