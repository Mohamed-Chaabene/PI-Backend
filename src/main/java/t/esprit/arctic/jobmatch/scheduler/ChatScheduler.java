package t.esprit.arctic.jobmatch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import t.esprit.arctic.jobmatch.entity.Evenement;
import t.esprit.arctic.jobmatch.repository.EvenementRepository;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatScheduler {

    private final EvenementRepository evenementRepository;

    // S'exécute toutes les minutes
    @Scheduled(fixedRate = 60000)
    public void gererChats() {
        LocalDateTime maintenant = LocalDateTime.now();
        List<Evenement> evenements = evenementRepository.findAll();

        for (Evenement e : evenements) {
            if (e.getDateHeure() == null) continue;

            LocalDateTime debut = e.getDateHeure();
            LocalDateTime ouvertureChat = debut.minusHours(24); // 24h avant
            LocalDateTime fermetureChat = debut.plusHours(3);   // ferme 3h après le début

            // Ouvre le chat si on est dans la fenêtre [debut-24h, debut+3h]
            boolean doitEtreOuvert = maintenant.isAfter(ouvertureChat)
                    && maintenant.isBefore(fermetureChat);

            if (doitEtreOuvert && !e.isChatOuvert()) {
                e.setChatOuvert(true);
                evenementRepository.save(e);
                log.info("Chat ouvert pour l'événement : {}", e.getTitre());
            } else if (!doitEtreOuvert && e.isChatOuvert()) {
                e.setChatOuvert(false);
                evenementRepository.save(e);
                log.info("Chat fermé pour l'événement : {}", e.getTitre());
            }
        }
    }
}