package t.esprit.arctic.jobmatch.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import t.esprit.arctic.jobmatch.dto.ParticipationRequest;
import t.esprit.arctic.jobmatch.dto.ParticipationResponse;
import t.esprit.arctic.jobmatch.entity.Evenement;
import t.esprit.arctic.jobmatch.entity.Participation;
import t.esprit.arctic.jobmatch.repository.EvenementRepository;
import t.esprit.arctic.jobmatch.repository.ParticipationRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParticipationService {

    private final ParticipationRepository repository;
    private final EvenementRepository evenementRepository;

    // GET ALL
    public List<ParticipationResponse> getAll() {
        return repository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // GET BY ID
    public ParticipationResponse getById(Long id) {
        return toResponse(repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Participation non trouvée : " + id)));
    }

    // confirmer() → CREATE
    public ParticipationResponse confirmer(ParticipationRequest request) {

        Evenement evenement = evenementRepository.findById(request.getEvenementId())
                .orElseThrow(() -> new RuntimeException("Événement non trouvé"));

        Participation p = Participation.builder()
                .dateInscription(request.getDateInscription())
                .statut("CONFIRME")
                .evenement(evenement) // ✅
                .build();

        return toResponse(repository.save(p));
    }

    // annuler() → UPDATE statut
    public ParticipationResponse annuler(Long id) {
        Participation p = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Participation non trouvée : " + id));
        p.setStatut("ANNULE");
        return toResponse(repository.save(p));
    }

    // ✅ GET participations par événement
    public List<ParticipationResponse> getByEvenement(Long evenementId) {
        return repository.findByEvenementId(evenementId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    // Mapper
    private ParticipationResponse toResponse(Participation p) {
        return new ParticipationResponse(
                p.getId(),
                p.getDateInscription(),
                p.getStatut(),
                p.getEvenement().getId(),
                p.getEvenement().getTitre()
        );
    }
}