package t.esprit.arctic.jobmatch.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import t.esprit.arctic.jobmatch.dto.ResultatDTO;
import t.esprit.arctic.jobmatch.entity.*;
import t.esprit.arctic.jobmatch.repository.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResultatService {

    @Autowired
    private ResultatRepository resultatRepository;

    @Autowired
    private EntretienRepository entretienRepository;

    @Autowired
    private ReponseCandidatRepository reponseCandidatRepository;

    @Autowired
    private CandidatRepository candidatRepository;

    public ResultatDTO calculerResultat(Long entretienId) {
        Entretien entretien = entretienRepository.findById(entretienId)
                .orElseThrow(() -> new RuntimeException("Entretien non trouvé"));

        if (entretien.getCandidat() == null) {
            throw new RuntimeException("Entretien de type TEST ne peut pas être calculé sans candidat spécifique");
        }

        return calculerResultat(entretienId, entretien.getCandidat().getId());
    }

    public ResultatDTO calculerResultat(Long entretienId, Long candidatId) {
        Entretien entretien = entretienRepository.findById(entretienId)
                .orElseThrow(() -> new RuntimeException("Entretien non trouvé"));

        Candidat candidat = candidatRepository.findById(candidatId)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé"));

        List<ReponseCandidat> reponses = reponseCandidatRepository
                .findByCandidatAndEntretien(candidat, entretien);

        int totalQuestions = entretien.getQuestions() != null ? entretien.getQuestions().size() : 0;

        // Corriger le scoring QCM vs QCU/VRAI_FAUX
        long bonnesReponses = entretien.getQuestions() == null ? 0 : entretien.getQuestions().stream().filter(question -> {
            List<ReponseCandidat> reponsesQuestion = reponses.stream()
                    .filter(r -> r.getQuestion().getId().equals(question.getId()))
                    .collect(Collectors.toList());

            if (question.getType() == TypeQuestion.QCM) {
                List<Long> reponseIds = reponsesQuestion.stream()
                        .map(r -> r.getChoixSelectionne() != null ? r.getChoixSelectionne().getId() : null)
                        .filter(id -> id != null)
                        .collect(Collectors.toList());

                List<Long> reponsesCorrectesIds = question.getChoix().stream()
                        .filter(Choix::isCorrecte)
                        .map(Choix::getId)
                        .collect(Collectors.toList());

                return reponseIds.size() == reponsesCorrectesIds.size() && reponseIds.containsAll(reponsesCorrectesIds);
            } else {
                if (reponsesQuestion.isEmpty()) {
                    return false;
                }
                return reponsesQuestion.stream().allMatch(ReponseCandidat::isCorrecte);
            }
        }).count();

        double score = totalQuestions > 0 ? (double) bonnesReponses / totalQuestions * 100 : 0;

        // Enregistrer ou mettre à jour le resultat
        Resultat resultat = resultatRepository.findByEntretien(entretien).orElse(new Resultat());
        resultat.setEntretien(entretien);
        resultat.setScore(score);
        resultat.setTotalQuestions(totalQuestions);
        resultat.setBonnesReponses((int) bonnesReponses);
        resultat.setDecision(score >= 70 ? "accepté" : "refusé");
        resultat.setEvaluatedAt(LocalDateTime.now());

        Resultat saved = resultatRepository.save(resultat);
        return convertToDTO(saved);
    }

    public ResultatDTO getResultat(Long entretienId) {
        Entretien entretien = entretienRepository.findById(entretienId)
                .orElseThrow(() -> new RuntimeException("Entretien non trouvé"));
        return resultatRepository.findByEntretien(entretien)
                .map(this::convertToDTO)
                .orElse(null);
    }

    private ResultatDTO convertToDTO(Resultat resultat) {
        return new ResultatDTO(
            resultat.getId(),
            resultat.getEntretien().getId(),
            resultat.getScore(),
            resultat.getTotalQuestions(),
            resultat.getBonnesReponses(),
            resultat.getDecision(),
            resultat.getCommentaire(),
            resultat.getEvaluatedAt()
        );
    }
}
