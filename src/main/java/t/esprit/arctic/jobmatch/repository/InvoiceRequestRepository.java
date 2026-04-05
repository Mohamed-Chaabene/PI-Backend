package t.esprit.arctic.jobmatch.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import t.esprit.arctic.jobmatch.entity.InvoiceRequest;
import t.esprit.arctic.jobmatch.entity.InvoiceStatus;
import java.util.List;

public interface InvoiceRequestRepository extends JpaRepository<InvoiceRequest, Long> {
    List<InvoiceRequest> findByFreelancerId(Long freelancerId);
    List<InvoiceRequest> findByStatus(InvoiceStatus status);
}