package t.esprit.arctic.jobmatch.freelance.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import t.esprit.arctic.jobmatch.entity.Utilisateur;
import t.esprit.arctic.jobmatch.freelance.dto.CandidatureResponseDTO;
import t.esprit.arctic.jobmatch.freelance.entity.CandidatureMission;
import t.esprit.arctic.jobmatch.freelance.entity.Mission;
import t.esprit.arctic.jobmatch.freelance.repository.CandidatureMissionRepository;
import t.esprit.arctic.jobmatch.freelance.repository.MissionRepository;
import t.esprit.arctic.jobmatch.repository.UtilisateurRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class freelanceCandidatureService {

    private final CandidatureMissionRepository candidatureRepo;
    private final MissionRepository missionRepository;
    private final UtilisateurRepository utilisateurRepository;

    @Transactional
    public CandidatureResponseDTO postuler(Long missionId, String email) {
        Utilisateur candidat = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new RuntimeException("Mission introuvable"));

        if (candidatureRepo.existsByMissionIdAndCandidatId(missionId, candidat.getId())) {
            throw new RuntimeException("Vous avez déjà postulé à cette mission");
        }

        CandidatureMission c = new CandidatureMission();
        c.setMission(mission);
        c.setCandidat(candidat);
        CandidatureMission saved = candidatureRepo.save(c);
        return toDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<CandidatureResponseDTO> getCandidaturesDeMission(Long missionId) {
        return candidatureRepo.findByMissionId(missionId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CandidatureResponseDTO> mesCandidatures(String email) {
        Utilisateur candidat = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        return candidatureRepo.findByCandidatId(candidat.getId()).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public CandidatureResponseDTO accepterCandidature(Long candidatureId) {
        CandidatureMission c = candidatureRepo.findById(candidatureId)
                .orElseThrow(() -> new RuntimeException("Candidature introuvable"));
        c.setStatut(t.esprit.arctic.jobmatch.freelance.entity.CandidatureStatut.ACCEPTEE);
        return toDTO(candidatureRepo.save(c));
    }

    @Transactional
    public CandidatureResponseDTO rejeterCandidature(Long candidatureId) {
        CandidatureMission c = candidatureRepo.findById(candidatureId)
                .orElseThrow(() -> new RuntimeException("Candidature introuvable"));
        c.setStatut(t.esprit.arctic.jobmatch.freelance.entity.CandidatureStatut.REJETEE);
        return toDTO(candidatureRepo.save(c));
    }

    private CandidatureResponseDTO toDTO(CandidatureMission c) {
        return CandidatureResponseDTO.builder()
                .id(c.getId())
                .missionId(c.getMission().getId())
                .missionTitre(c.getMission().getTitre())
                .utilisateurId(c.getCandidat().getId())
                .utilisateurNom(c.getCandidat().getNom())
                .statut(c.getStatut().name())
                .datePostulation(c.getDatePostulation() != null ? c.getDatePostulation().toString() : null)
                .build();
    }
}