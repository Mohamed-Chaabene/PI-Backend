package t.esprit.arctic.jobmatch.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import t.esprit.arctic.jobmatch.dto.ResultatDTO;
import t.esprit.arctic.jobmatch.entity.*;
import t.esprit.arctic.jobmatch.repository.*;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ResultatService {

    @Autowired
    private ResultatRepository resultatRepository;

    @Autowired
    private EntretienRepository entretienRepository;

    @Autowired
    private ReponseCandidatRepository reponseCandidatRepository;

    public ResultatDTO calculerResultat(Long entretienId) {
        Entretien entretien = entretienRepository.findById(entretienId)
                .orElseThrow(() -> new RuntimeException("Entretien non trouvé"));

        List<ReponseCandidat> reponses = reponseCandidatRepository
                .findByCandidatAndEntretien(entretien.getCandidat(), entretien);

        int totalQuestions = entretien.getQuestions().size();
        int bonnesReponses = (int) reponses.stream().filter(r -> r.isCorrecte()).count();
        double score = totalQuestions > 0 ? (double) bonnesReponses / totalQuestions * 100 : 0;

        Resultat resultat = new Resultat();
        resultat.setEntretien(entretien);
        resultat.setScore(score);
        resultat.setTotalQuestions(totalQuestions);
        resultat.setBonnesReponses(bonnesReponses);
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
