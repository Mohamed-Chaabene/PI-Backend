package t.esprit.arctic.jobmatch.freelance.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import t.esprit.arctic.jobmatch.entity.Utilisateur;
import t.esprit.arctic.jobmatch.freelance.dto.FreelanceStatsDTO;
import t.esprit.arctic.jobmatch.freelance.entity.CandidatureMission;
import t.esprit.arctic.jobmatch.freelance.entity.CandidatureStatut;
import t.esprit.arctic.jobmatch.freelance.entity.Mission;
import t.esprit.arctic.jobmatch.freelance.entity.MissionStatut;
import t.esprit.arctic.jobmatch.freelance.repository.CandidatureMissionRepository;
import t.esprit.arctic.jobmatch.freelance.repository.MissionRepository;
import t.esprit.arctic.jobmatch.repository.UtilisateurRepository;

import java.util.List;

/**
 * Analytics service computing live KPI data for both freelancer and client dashboards.
 */
@Service
@RequiredArgsConstructor
public class FreelanceAnalyticsService {

    private final CandidatureMissionRepository candidatureRepository;
    private final MissionRepository missionRepository;
    private final UtilisateurRepository utilisateurRepository;

    // ────────────────────────────────────────────────────────────────────
    //  Freelancer Stats
    // ────────────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public FreelanceStatsDTO getFreelancerStats(String email) {
        Utilisateur user = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable : " + email));

        List<CandidatureMission> candidatures = candidatureRepository.findByCandidatId(user.getId());

        int total = candidatures.size();
        int accepted = (int) candidatures.stream()
                .filter(c -> c.getStatut() == CandidatureStatut.ACCEPTEE).count();
        int rejected = (int) candidatures.stream()
                .filter(c -> c.getStatut() == CandidatureStatut.REJETEE).count();
        int pending = (int) candidatures.stream()
                .filter(c -> c.getStatut() == CandidatureStatut.EN_ATTENTE).count();

        int approvalPercent = total > 0 ? (int) Math.round((accepted * 100.0) / total) : 0;
        int points = accepted * 10;

        String level;
        if (points >= 100) level = "Expert";
        else if (points >= 40) level = "Intermédiaire";
        else level = "Débutant";

        // Earnings = sum of budgets from missions where I was accepted
        double earnings = candidatures.stream()
                .filter(c -> c.getStatut() == CandidatureStatut.ACCEPTEE)
                .mapToDouble(c -> c.getMission().getBudget() != null ? c.getMission().getBudget() : 0)
                .sum();

        return FreelanceStatsDTO.builder()
                .totalCandidatures(total)
                .acceptedCandidatures(accepted)
                .rejectedCandidatures(rejected)
                .pendingCandidatures(pending)
                .approvalPercent(approvalPercent)
                .freelancerPoints(points)
                .freelancerLevel(level)
                .totalEarnings(earnings)
                .build();
    }

    // ────────────────────────────────────────────────────────────────────
    //  Client Stats
    // ────────────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public FreelanceStatsDTO getClientStats(String email) {
        Utilisateur user = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable : " + email));

        List<Mission> missions = missionRepository.findByPublieParId(user.getId());

        int totalMissions = missions.size();
        int openMissions = (int) missions.stream()
                .filter(m -> m.getStatut() == MissionStatut.OUVERTE).count();

        // Aggregate candidatures across all owned missions
        int totalIncoming = 0;
        int pendingIncoming = 0;
        for (Mission m : missions) {
            List<CandidatureMission> candidatures = candidatureRepository.findByMissionId(m.getId());
            totalIncoming += candidatures.size();
            pendingIncoming += candidatures.stream()
                    .filter(c -> c.getStatut() == CandidatureStatut.EN_ATTENTE).count();
        }

        double totalBudget = missions.stream()
                .mapToDouble(m -> m.getBudget() != null ? m.getBudget() : 0)
                .sum();

        return FreelanceStatsDTO.builder()
                .totalMissionsPosted(totalMissions)
                .openMissions(openMissions)
                .totalIncomingCandidatures(totalIncoming)
                .pendingIncomingCandidatures(pendingIncoming)
                .totalBudgetAllocated(totalBudget)
                .build();
    }
}
