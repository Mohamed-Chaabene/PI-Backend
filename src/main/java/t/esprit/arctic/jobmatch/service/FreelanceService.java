package t.esprit.arctic.jobmatch.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import t.esprit.arctic.jobmatch.dto.JobCreateDTO;
import t.esprit.arctic.jobmatch.entity.Job;
import t.esprit.arctic.jobmatch.entity.JobStatus;
import t.esprit.arctic.jobmatch.entity.JobType;
import t.esprit.arctic.jobmatch.entity.Utilisateur;
import t.esprit.arctic.jobmatch.repository.JobRepository;
import t.esprit.arctic.jobmatch.repository.UtilisateurRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FreelanceService {

    private final JobRepository jobRepository;
    private final UtilisateurRepository userRepository;

    public Job createJob(JobCreateDTO dto, Long clientId) {
        Utilisateur client = userRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        Job job = new Job();
        job.setTitle(dto.getTitle());
        job.setDescription(dto.getDescription());
        job.setType(JobType.valueOf(dto.getType().toUpperCase()));
        job.setBudget(dto.getBudget());
        job.setEstimatedDays(dto.getEstimatedDays());
        job.setClient(client);

        return jobRepository.save(job);
    }

    // ← ADD THIS METHOD
    public List<Job> getAllOpenJobs() {
        return jobRepository.findByStatus(JobStatus.OPEN);
    }
}