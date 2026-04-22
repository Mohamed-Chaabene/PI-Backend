package t.esprit.arctic.jobmatch.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import t.esprit.arctic.jobmatch.dto.FeedbackMacroDTO;
import t.esprit.arctic.jobmatch.entity.*;
import t.esprit.arctic.jobmatch.repository.*;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final FeedbackMacroRepository macroRepo;
    private final InscriptionParcoursRepository inscriptionRepo;
    private final CertificatService certificatService;
    private final FeedbackAlertService alertService;
    private final NotificationService notificationService;

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
        getById(id); // vérifie l'existence
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

    public List<Feedback> getByParcours(Long parcoursId) {
        return feedbackRepository.findByParcoursId(parcoursId);
    }

    public Double getNoteMoyenne(Long formationId) {
        Double moyenne = feedbackRepository.findNoteMoyenneByFormationId(formationId);
        return moyenne != null ? Math.round(moyenne * 10.0) / 10.0 : 0.0;
    }

    @Transactional
    public void saveMacro(FeedbackMacroDTO dto) {
        InscriptionParcours inscription = inscriptionRepo.findById(dto.getInscriptionId())
                .orElseThrow(() -> new RuntimeException("Inscription non trouvée"));

        // Marquer l'exigence de feedback comme remplie (avant le check d'idempotence pour s'assurer que c'est fait)
        inscription.setEvaluationParcoursRequise(false);
        inscriptionRepo.save(inscription);

        if (macroRepo.existsByInscriptionId(dto.getInscriptionId())) {
            // Supprimer quand même la notification au cas où elle subsisterait
            notificationService.deleteParcoursCompletionNotification(inscription.getCandidat().getId(), dto.getParcoursId());
            return; // Déjà enregistré
        }

        FeedbackMacro macro = FeedbackMacro.builder()
                .inscription(inscription)
                .candidat(inscription.getCandidat())
                .parcours(inscription.getParcours())
                .noteGlobale(dto.getNoteGlobale())
                .progression(dto.getProgression())
                .experienceQuiz(dto.getExperienceQuiz())
                .recommandation(dto.getRecommandation())
                .commentaireLibre(dto.getCommentaireLibre())
                .build();

        macroRepo.save(macro);

        alertService.checkLowRating(dto.getParcoursId(), "MACRO", dto.getNoteGlobale());

        // Supprimer la notification de fin de parcours car le feedback est fait
        notificationService.deleteParcoursCompletionNotification(inscription.getCandidat().getId(), dto.getParcoursId());
    }

    public List<FeedbackMacro> getMacroByParcours(Long parcoursId) {
        return macroRepo.findByParcoursId(parcoursId);
    }
}