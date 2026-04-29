package t.esprit.arctic.jobmatch.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import t.esprit.arctic.jobmatch.dto.QuizGenerationRequest;
import t.esprit.arctic.jobmatch.entity.Formation;
import t.esprit.arctic.jobmatch.entity.InscriptionFormation;
import t.esprit.arctic.jobmatch.entity.InscriptionParcours;
import t.esprit.arctic.jobmatch.entity.NiveauOrdre;
import t.esprit.arctic.jobmatch.repository.FormationRepository;
import t.esprit.arctic.jobmatch.repository.InscriptionFormationRepository;
import t.esprit.arctic.jobmatch.repository.InscriptionParcoursRepository;
import t.esprit.arctic.jobmatch.repository.QuizNiveauRepository;
import t.esprit.arctic.jobmatch.service.FormationService;
import t.esprit.arctic.jobmatch.service.ParcoursFormationService;
import t.esprit.arctic.jobmatch.service.QuizNiveauService;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FormationScheduler {

    private final FormationService formationService;
    private final ParcoursFormationService parcoursService;
    private final FormationRepository formationRepo;
    private final InscriptionParcoursRepository inscriptionParcoursRepo;
    private final InscriptionFormationRepository inscriptionFormationRepo;
    private final QuizNiveauRepository quizNiveauRepo;
    private final QuizNiveauService quizNiveauService;

    @Scheduled(cron = "1 * * * * *")
    public void calculerScoresEtBadges() {
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║  Scheduler formations — démarrage   ║");
        System.out.println("╚══════════════════════════════════════╝");

        var result = formationService.refreshScoresEtBadges();
        parcoursService.refreshGlobalStats();

        System.out.println("══ Résumé ══════════════════════════════");
        System.out.println("  Formations mises à jour : " + result.get("miseAJour"));
        System.out.println("  Total traité            : " + result.get("total"));
        System.out.println("  Parcours mis à jour     : OK");
        System.out.println("════════════════════════════════════════");
    }

    @Scheduled(cron = "0 0 8 * * MON")
    public void relancerFormationsSansBadge() {
        var avecBadge = formationRepo.findByStatutAndBadgeIsNotNull("Disponible");
        System.out.println("Formations avec badge actif : " + avecBadge.size());

        long nbTopNotes   = formationRepo.countByStatutAndBadge("Disponible", "Top noté");
        long nbTendance   = formationRepo.countByStatutAndBadge("Disponible", "Tendance");
        long nbPopulaires = formationRepo.countByStatutAndBadge("Disponible", "Populaire");
        long nbBienNotes  = formationRepo.countByStatutAndBadge("Disponible", "Bien noté");

        System.out.println("  Top noté    : " + nbTopNotes);
        System.out.println("  Tendance    : " + nbTendance);
        System.out.println("  Populaires  : " + nbPopulaires);
        System.out.println("  Bien noté   : " + nbBienNotes);
    }

    @Scheduled(fixedRate = 60000) // Toutes les minutes
    public void gererQuizzNiveauAutomatiques() {
        List<InscriptionParcours> activeInscriptions = inscriptionParcoursRepo.findByStatut("EN_COURS");

        for (InscriptionParcours ip : activeInscriptions) {
            NiveauOrdre niveau = ip.getNiveauActuel();

            // RÈGLE : Pas de scheduler pour le niveau EXPERT
            if (niveau == NiveauOrdre.EXPERT) continue;

            Formation formationNiveau = ip.getParcours().getFormationParNiveau(niveau);
            if (formationNiveau == null) continue;

            // Vérifier la progression du candidat sur cette formation précise
            var optInf = inscriptionFormationRepo.findByCandidatIdAndFormationIdAndParcoursId(
                    ip.getCandidat().getId(),
                    formationNiveau.getId(),
                    ip.getParcours().getId()
            );

            if (optInf.isPresent()) {
                InscriptionFormation inf = optInf.get();
                
                // Si la formation est terminée (100%)
                if (inf.getProgression() != null && inf.getProgression() >= 100) {
                    
                    // Et si aucun quiz n'a encore été généré pour ce niveau
                    if (!quizNiveauRepo.existsByInscriptionParcoursIdAndNiveau(ip.getId(), niveau)) {
                        System.out.println("🤖 [SCHEDULER] Progression 100% détectée pour " + ip.getCandidat().getNom() 
                                + " au niveau " + niveau + ". Génération automatique du quiz...");
                        
                        try {
                            quizNiveauService.genererQuiz(QuizGenerationRequest.builder()
                                    .inscriptionParcoursId(ip.getId())
                                    .niveau(niveau)
                                    .titreFormation(formationNiveau.getTitre())
                                    .nombreQuestions(10)
                                    .build());
                            System.out.println("✅ [SCHEDULER] Quiz généré avec succès pour " + ip.getCandidat().getNom());
                        } catch (Exception e) {
                            System.err.println("❌ [SCHEDULER] Erreur génération quiz auto : " + e.getMessage());
                        }
                    }
                }
            }
        }
    }
}