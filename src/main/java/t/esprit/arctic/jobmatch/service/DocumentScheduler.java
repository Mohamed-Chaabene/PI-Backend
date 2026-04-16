package t.esprit.arctic.jobmatch.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import t.esprit.arctic.jobmatch.entity.Document;
import t.esprit.arctic.jobmatch.repository.DocumentRepository;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DocumentScheduler {

    private final DocumentRepository documentRepository;


    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void nettoyerDocumentsAnciens() {
        System.out.println(" [SCHEDULER] Nettoyage des documents anciens - " + LocalDateTime.now());

        LocalDateTime dateLimite = LocalDateTime.now().minusDays(30);
        List<Document> documentsAnciens = documentRepository.findByUpdatedAtBefore(dateLimite);

        int compteur = 0;
        for (Document doc : documentsAnciens) {
            System.out.println("Suppression: " + doc.getNom());
            documentRepository.delete(doc);
            compteur++;
        }

        System.out.println(" Nettoyage terminé - " + compteur + " document(s) supprimé(s)");
    }



     // Tâche 2: Met à jour les scores ATS des CV S'exécute tous les jours à 03:00

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void mettreAJourScoresATS() {
        System.out.println(" [SCHEDULER] Mise à jour scores ATS - " + LocalDateTime.now());

        try {
            List<Document> cvDocuments = documentRepository.findByType("CV");

            int compteur = 0;
            for (Document cv : cvDocuments) {
                int scoreATS = calculerScoreATS(cv.getContenu());
                cv.setScoreATS(scoreATS);
                documentRepository.save(cv);
                compteur++;
                System.out.println(cv.getNom() + " - Score ATS: " + scoreATS + "%");
            }

            System.out.println( compteur + " CV(s) mis à jour");

        } catch (Exception e) {
            System.err.println(" Erreur mise à jour scores: " + e.getMessage());
        }
    }


    // @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void testScheduler() {
        System.out.println(" [TEST] Scheduler fonctionne - " + LocalDateTime.now());
        long count = documentRepository.count();
        System.out.println(" Nombre total de documents: " + count);
    }

    private int calculerScoreATS(String contenuHTML) {
        if (contenuHTML == null || contenuHTML.isEmpty()) {
            return 0;
        }

        String contenuTexte = contenuHTML.replaceAll("<[^>]*>", " ").toLowerCase();

        int score = 0;
        if (contenuTexte.contains("expérience") || contenuTexte.contains("experience")) score += 20;
        if (contenuTexte.contains("compétence") || contenuTexte.contains("competence")) score += 20;
        if (contenuTexte.contains("formation") || contenuTexte.contains("education")) score += 20;
        if (contenuTexte.contains("diplôme") || contenuTexte.contains("diplome")) score += 20;
        if (contenuTexte.contains("projet") || contenuTexte.contains("project")) score += 20;

        return score;
    }



}