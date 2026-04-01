package t.esprit.arctic.jobmatch.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import t.esprit.arctic.jobmatch.dto.FeedbackRequest;
import t.esprit.arctic.jobmatch.dto.FeedbackResponse;
import t.esprit.arctic.jobmatch.entity.Feedback;
import t.esprit.arctic.jobmatch.entity.Participation;
import t.esprit.arctic.jobmatch.repository.FeedbackRepository;
import t.esprit.arctic.jobmatch.repository.ParticipationRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedbackEventService {

    private final FeedbackRepository repository;
    private final ParticipationRepository participationRepository;

    // GET ALL
    public List<FeedbackResponse> getAll() {
        return repository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // GET BY ID
    public FeedbackResponse getById(Long id) {
        return toResponse(repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Feedback non trouvé : " + id)));
    }

    // GET BY PARTICIPATION
    public List<FeedbackResponse> getByParticipation(Long participationId) {
        return repository.findByParticipationId(participationId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // CREATE
    public FeedbackResponse create(FeedbackRequest request) {
        Participation participation = participationRepository
                .findById(request.getParticipationId())
                .orElseThrow(() -> new RuntimeException("Participation non trouvée"));

        Feedback f = Feedback.builder()
                .commentaire(request.getCommentaire())
                .note(request.getNote())
                .date(request.getDate())
                .participation(participation)
                .build();

        return toResponse(repository.save(f));
    }

    // UPDATE
    public FeedbackResponse update(Long id, FeedbackRequest request) {
        Feedback f = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Feedback non trouvé : " + id));

        f.setCommentaire(request.getCommentaire());
        f.setNote(request.getNote());
        f.setDate(request.getDate());

        return toResponse(repository.save(f));
    }

    // DELETE
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Feedback non trouvé : " + id);
        }
        repository.deleteById(id);
    }

    // Mapper
    private FeedbackResponse toResponse(Feedback f) {
        Long participationId = f.getParticipation() != null ? f.getParticipation().getId() : null;
        return new FeedbackResponse(
                f.getId(),
                f.getCommentaire(),
                f.getNote(),
                f.getDate(),
                participationId,
                null
        );
    }
}