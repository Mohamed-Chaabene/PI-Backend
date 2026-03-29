package t.esprit.arctic.jobmatch.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import t.esprit.arctic.jobmatch.dto.ReponseDTO;
import t.esprit.arctic.jobmatch.entity.*;
import t.esprit.arctic.jobmatch.repository.*;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReponseCandidatService {

    @Autowired
    private ReponseCandidatRepository reponseCandidatRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private CandidatRepository candidatRepository;

    @Autowired
    private EntretienRepository entretienRepository;

    public ReponseDTO submitReponse(ReponseDTO dto) {
        Question question = questionRepository.findById(dto.getQuestionId())
                .orElseThrow(() -> new RuntimeException("Question non trouvée"));
        Candidat candidat = candidatRepository.findById(dto.getCandidatId())
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé"));
        Entretien entretien = entretienRepository.findById(dto.getEntretienId())
                .orElseThrow(() -> new RuntimeException("Entretien non trouvé"));

        Choix choix = null;
        if (dto.getChoixId() != null) {
            choix = question.getChoix().stream()
                    .filter(c -> c.getId().equals(dto.getChoixId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Choix non trouvé"));
        }

        ReponseCandidat reponse = new ReponseCandidat();
        reponse.setQuestion(question);
        reponse.setChoixSelectionne(choix);
        reponse.setCandidat(candidat);
        reponse.setEntretien(entretien);
        reponse.setCorrecte(choix != null && choix.isCorrecte());

        ReponseCandidat saved = reponseCandidatRepository.save(reponse);
        return convertToDTO(saved);
    }

    public List<ReponseDTO> submitReponses(List<ReponseDTO> reponseDTOs, Long entretienId) {
        return reponseDTOs.stream().map(dto -> {
            if (dto.getEntretienId() == null || !dto.getEntretienId().equals(entretienId)) {
                throw new IllegalArgumentException("EntretienId manquant ou invalide pour la réponse");
            }
            return submitReponse(dto);
        }).collect(Collectors.toList());
    }

    public List<ReponseDTO> getReponsesByEntretien(Long candidatId, Long entretienId) {
        Candidat candidat = candidatRepository.findById(candidatId)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé"));
        Entretien entretien = entretienRepository.findById(entretienId)
                .orElseThrow(() -> new RuntimeException("Entretien non trouvé"));

        return reponseCandidatRepository.findByCandidatAndEntretien(candidat, entretien)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private ReponseDTO convertToDTO(ReponseCandidat reponse) {
        return new ReponseDTO(
            reponse.getId(),
            reponse.getQuestion().getId(),
            reponse.getChoixSelectionne() != null ? reponse.getChoixSelectionne().getId() : null,
            reponse.getCandidat().getId(),
            reponse.getEntretien().getId(),
            reponse.isCorrecte()
        );
    }
}

