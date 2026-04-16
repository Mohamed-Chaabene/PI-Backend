package t.esprit.arctic.jobmatch.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import t.esprit.arctic.jobmatch.entity.Notification;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);
    long countByUserIdAndIsReadFalse(Long userId);
<<<<<<< HEAD
=======
    
    // Scheduler methods for idempotence checks
    boolean existsByEntretienIdAndTypeAndIsReadFalse(Long entretienId, String type);
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
}
