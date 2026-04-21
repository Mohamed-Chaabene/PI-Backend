package t.esprit.arctic.jobmatch.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import t.esprit.arctic.jobmatch.entity.FeedbackMicro;

public interface FeedbackMicroRepository extends JpaRepository<FeedbackMicro, Long> {
}
