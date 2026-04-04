package t.esprit.arctic.jobmatch.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import t.esprit.arctic.jobmatch.entity.Proposal;
import t.esprit.arctic.jobmatch.entity.ProposalStatus;
import java.util.List;

public interface ProposalRepository extends JpaRepository<Proposal, Long> {

    List<Proposal> findByFreelancerId(Long freelancerId);

    List<Proposal> findByJobId(Long jobId);

    List<Proposal> findByStatus(ProposalStatus status);
}