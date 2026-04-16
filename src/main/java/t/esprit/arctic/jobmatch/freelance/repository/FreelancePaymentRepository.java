package t.esprit.arctic.jobmatch.freelance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import t.esprit.arctic.jobmatch.freelance.entity.FreelancePayment;

import java.util.List;

@Repository
public interface FreelancePaymentRepository extends JpaRepository<FreelancePayment, Long> {
    List<FreelancePayment> findByContractId(Long contractId);
}
