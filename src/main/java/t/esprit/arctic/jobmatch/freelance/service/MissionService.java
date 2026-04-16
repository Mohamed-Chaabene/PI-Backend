package t.esprit.arctic.jobmatch.freelance.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import t.esprit.arctic.jobmatch.entity.Utilisateur;
import t.esprit.arctic.jobmatch.freelance.dto.MissionDTO;
import t.esprit.arctic.jobmatch.freelance.dto.MissionResponseDTO;
import t.esprit.arctic.jobmatch.freelance.entity.Mission;
import t.esprit.arctic.jobmatch.freelance.entity.MissionStatut;
import t.esprit.arctic.jobmatch.freelance.repository.MissionRepository;
import t.esprit.arctic.jobmatch.repository.UtilisateurRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MissionService {

    private final MissionRepository missionRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final t.esprit.arctic.jobmatch.freelance.repository.CandidatureMissionRepository candidatureRepository;

    @Transactional(readOnly = true)
    public List<MissionResponseDTO> getMissionsOuvertes() {
        return missionRepository.findByStatut(MissionStatut.OUVERTE)
                .stream()
                .map(MissionResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MissionResponseDTO getMissionById(Long id) {
        Mission mission = missionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mission introuvable : " + id));
        return MissionResponseDTO.fromEntity(mission);
    }

    @Transactional
    public MissionResponseDTO creerMission(MissionDTO dto, String email) {
        Utilisateur publisher = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable : " + email));
        Mission mission = new Mission();
        mission.setTitre(dto.getTitre());
        mission.setDescription(dto.getDescription());
        mission.setBudget(dto.getBudget());
        mission.setCompetences(dto.getCompetences());
        mission.setPubliePar(publisher);
        Mission saved = missionRepository.save(mission);
        return MissionResponseDTO.fromEntity(saved);
    }

    @Transactional
    public MissionResponseDTO updateMission(Long id, MissionDTO dto, String email) {
        Mission mission = missionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mission introuvable : " + id));

        // Verify ownership
        if (!mission.getPubliePar().getEmail().equals(email)) {
            throw new RuntimeException("Vous n'êtes pas le propriétaire de cette mission");
        }

        mission.setTitre(dto.getTitre());
        mission.setDescription(dto.getDescription());
        mission.setBudget(dto.getBudget());
        mission.setCompetences(dto.getCompetences());
        Mission saved = missionRepository.save(mission);
        return MissionResponseDTO.fromEntity(saved);
    }

    @Transactional
    public void deleteMission(Long id, String email) {
        Mission mission = missionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mission introuvable : " + id));

        // Verify ownership
        if (!mission.getPubliePar().getEmail().equals(email)) {
            throw new RuntimeException("Vous n'êtes pas le propriétaire de cette mission");
        }

        // Delete candidatures first to prevent foreign key errors
        candidatureRepository.deleteAllByMissionId(mission.getId());
        
        missionRepository.delete(mission);
    }

    @Transactional(readOnly = true)
    public List<MissionResponseDTO> getMissionsParPublisher(String email) {
        Utilisateur publisher = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable : " + email));
        return missionRepository.findByPublieParId(publisher.getId())
                .stream()
                .map(MissionResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }
}