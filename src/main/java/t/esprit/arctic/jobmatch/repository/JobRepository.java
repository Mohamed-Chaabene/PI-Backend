package t.esprit.arctic.jobmatch.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import t.esprit.arctic.jobmatch.entity.Job;           // ← Correct import (no .freelance)
import t.esprit.arctic.jobmatch.entity.JobStatus;     // ← Correct import (no .freelance)
import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {

    List<Job> findByStatus(JobStatus status);
    List<Job> findByClientId(Long clientId);
}