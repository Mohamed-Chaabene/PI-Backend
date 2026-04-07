package t.esprit.arctic.jobmatch.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import t.esprit.arctic.jobmatch.dto.FeedbackEventRequest;
import t.esprit.arctic.jobmatch.dto.FeedbackEventResponse;
import t.esprit.arctic.jobmatch.dto.OrganisateurReputationResponse;
import t.esprit.arctic.jobmatch.entity.FeedbackEvent;
import t.esprit.arctic.jobmatch.entity.Participation;
import t.esprit.arctic.jobmatch.repository.FeedbackEventRepository;
import t.esprit.arctic.jobmatch.repository.ParticipationRepository;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedbackEventService {

    private final FeedbackEventRepository repository;
    private final ParticipationRepository participationRepository;

    public List<FeedbackEventResponse> getAll() {
        return repository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public FeedbackEventResponse getById(Long id) {
        return toResponse(repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Feedback non trouvé : " + id)));
    }

    public List<FeedbackEventResponse> getByParticipation(Long participationId) {
        return repository.findByParticipationId(participationId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<FeedbackEventResponse> getByEvenement(Long evenementId) {
        return repository.findByEvenementId(evenementId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Double getNoteMoyenne(Long evenementId) {
        Double moyenne = repository.findNoteMoyenneByEvenementId(evenementId);
        return moyenne != null ? Math.round(moyenne * 10.0) / 10.0 : 0.0;
    }

    public FeedbackEventResponse create(FeedbackEventRequest request) {
        Participation participation = participationRepository
                .findById(request.getParticipationId())
                .orElseThrow(() -> new RuntimeException("Participation non trouvée"));

        // Vérifie que la participation est CONFIRMEE
        if (!"CONFIRME".equals(participation.getStatut())) {
            throw new RuntimeException("Vous devez être confirmé pour laisser un feedback");
        }

        // ← Vérifie que la date de l'événement est passée
        Date dateEvenement = participation.getEvenement().getDate();
        if (dateEvenement != null && dateEvenement.after(new Date())) {
            throw new RuntimeException("Vous ne pouvez pas laisser un feedback avant la date de l'événement");
        }

        // Vérifie qu'un feedback n'existe pas déjà
        if (repository.existsByParticipationId(request.getParticipationId())) {
            throw new RuntimeException("Vous avez déjà laissé un feedback pour cet événement");
        }

        FeedbackEvent f = FeedbackEvent.builder()
                .commentaire(request.getCommentaire())
                .note(request.getNote())
                .date(new Date())
                .participation(participation)
                .build();

        return toResponse(repository.save(f));
    }

    public FeedbackEventResponse update(Long id, FeedbackEventRequest request) {
        FeedbackEvent f = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Feedback non trouvé : " + id));
        f.setCommentaire(request.getCommentaire());
        f.setNote(request.getNote());
        return toResponse(repository.save(f));
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Feedback non trouvé : " + id);
        }
        repository.deleteById(id);
    }

    private FeedbackEventResponse toResponse(FeedbackEvent f) {
        String titreEvenement = f.getParticipation() != null
                ? f.getParticipation().getEvenement().getTitre()
                : null;
        return new FeedbackEventResponse(
                f.getId(),
                f.getCommentaire(),
                f.getNote(),
                f.getDate(),
                f.getParticipation() != null ? f.getParticipation().getId() : null,
                titreEvenement
        );
    }

    public OrganisateurReputationResponse getReputation(
            Long organisateurId,
            String nomOrganisateur,
            String type,
            String titre) {

        // ── Niveau 1 : réputation globale ────────────────────────────────────────
        // On récupère tous les feedbacks de cet organisateur
        List<FeedbackEvent> tousLesFeedbacks =
                repository.findByOrganisateurId(organisateurId);

        // On calcule la moyenne avec les streams Java — plus lisible qu'une @Query AVG
        double noteMoyenneGlobale = tousLesFeedbacks.stream()
                .mapToInt(FeedbackEvent::getNote)
                .average()
                .orElse(0.0);
        noteMoyenneGlobale = Math.round(noteMoyenneGlobale * 10.0) / 10.0;
        long totalFeedbacks = tousLesFeedbacks.size();

        // On attribue le badge selon la note
        String badgeReputation = calculerBadge(noteMoyenneGlobale, totalFeedbacks);

        // ── Niveau 2 : réputation par type ───────────────────────────────────────
        // Ex: si l'événement actuel est un WORKSHOP, on regarde tous ses workshops passés
        List<FeedbackEvent> feedbacksParType =
                repository.findByOrganisateurIdAndType(organisateurId, type);

        double noteMoyenneParType = feedbacksParType.stream()
                .mapToInt(FeedbackEvent::getNote)
                .average()
                .orElse(0.0);
        noteMoyenneParType = Math.round(noteMoyenneParType * 10.0) / 10.0;
        long totalFeedbacksParType = feedbacksParType.size();

        String badgeType = calculerBadgeType(
                noteMoyenneParType, totalFeedbacksParType, type
        );

        // ── Niveau 3 : réputation par titre ──────────────────────────────────────
        // Ex: si l'événement s'appelle "Job Fair Tunis", on regarde les éditions passées
        List<FeedbackEvent> feedbacksParTitre =
                repository.findByOrganisateurIdAndTitre(organisateurId, titre);

        double noteMoyenneTitrePasse = feedbacksParTitre.stream()
                .mapToInt(FeedbackEvent::getNote)
                .average()
                .orElse(0.0);
        noteMoyenneTitrePasse = Math.round(noteMoyenneTitrePasse * 10.0) / 10.0;

        String badgeTitre = calculerBadgeTitre(
                noteMoyenneTitrePasse, (long) feedbacksParTitre.size()
        );

        return new OrganisateurReputationResponse(
                organisateurId,
                nomOrganisateur,
                noteMoyenneGlobale,
                totalFeedbacks,
                badgeReputation,
                noteMoyenneParType,
                totalFeedbacksParType,
                badgeType,
                noteMoyenneTitrePasse,
                badgeTitre
        );
    }

    // Badge global — basé sur la note moyenne toutes catégories confondues
    private String calculerBadge(double note, long total) {
        if (total == 0) return "NOUVEAU";
        if (note >= 4.5) return "EXCELLENT";
        if (note >= 3.5) return "RECOMMANDE";
        if (note >= 2.5) return "MOYEN";
        return "PEU_RECOMMANDE";
    }

    // Badge par type — ajoute le type au badge pour plus de précision
    private String calculerBadgeType(double note, long total, String type) {
        if (total == 0) return "PREMIER_" + type;
        if (note >= 4.5) return "EXCELLENT_" + type;
        if (note >= 3.5) return "BON_" + type;
        if (note >= 2.5) return "MOYEN_" + type;
        return "MAUVAIS_" + type;
    }

    // Badge par titre — indique si les éditions précédentes étaient appréciées
    private String calculerBadgeTitre(double note, long total) {
        if (total == 0) return "NOUVEAU";
        if (note >= 4.5) return "TRES_APPRECIE";
        if (note >= 3.5) return "BIEN_NOTE";
        if (note >= 2.5) return "MOYEN";
        return "MAUVAIS_RETOUR";
    }
}