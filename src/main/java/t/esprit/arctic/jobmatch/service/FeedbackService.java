package t.esprit.arctic.jobmatch.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import t.esprit.arctic.jobmatch.entity.Feedback;
import t.esprit.arctic.jobmatch.repository.FeedbackRepository;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;



    public List<Feedback> getAll() {
        return feedbackRepository.findAll();
    }

    public Feedback getById(Long id) {
        return feedbackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Feedback non trouvé avec l'id : " + id));
    }

    public Feedback create(Feedback feedback) {

        if (feedbackRepository.existsByFormationIdAndCandidatId(
                feedback.getFormation().getId(),
                feedback.getCandidat().getId())) {
            throw new RuntimeException("Vous avez déjà laissé un feedback pour cette formation");
        }
        return feedbackRepository.save(feedback);
    }

    public Feedback update(Long id, Feedback updated) {
        Feedback existing = getById(id);
        existing.setNote(updated.getNote());
        existing.setCommentaire(updated.getCommentaire());
        return feedbackRepository.save(existing);
    }

    public void delete(Long id) {
        getById(id);
        feedbackRepository.deleteById(id);
    }


    public List<Feedback> getByFormation(Long formationId) {
        return feedbackRepository.findByFormationId(formationId);
    }

    public List<Feedback> getByCandidat(Long candidatId) {
        return feedbackRepository.findByCandidatId(candidatId);
    }

    public List<Feedback> getByCandidatAndFormation(Long candidatId, Long formationId) {
        return feedbackRepository.findByFormationIdAndCandidatId(formationId, candidatId);
    }

    public Double getNoteMoyenne(Long formationId) {
        Double moyenne = feedbackRepository.findNoteMoyenneByFormationId(formationId);
        return moyenne != null ? Math.round(moyenne * 10.0) / 10.0 : 0.0;
    }
}