package t.esprit.arctic.jobmatch.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import t.esprit.arctic.jobmatch.entity.freelance.UnitTransaction;
import t.esprit.arctic.jobmatch.entity.Utilisateur;
import java.util.List;

public interface UnitTransactionRepository extends JpaRepository<UnitTransaction, Long> {
    List<UnitTransaction> findByUserOrderByCreatedAtDesc(Utilisateur user);
}