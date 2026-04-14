package t.esprit.arctic.jobmatch.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.pusher.rest.Pusher;
import t.esprit.arctic.jobmatch.dto.ChatMessageRequest;
import t.esprit.arctic.jobmatch.dto.ChatMessageResponse;
import t.esprit.arctic.jobmatch.entity.*;
import t.esprit.arctic.jobmatch.repository.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final EvenementRepository evenementRepository;
    private final CandidatRepository candidatRepository;
    private final ParticipationRepository participationRepository;
    private final Pusher pusher;

    // Envoie un message dans le chat
    public ChatMessageResponse envoyer(ChatMessageRequest request) {

        Evenement evenement = evenementRepository.findById(request.getEvenementId())
                .orElseThrow(() -> new RuntimeException("Événement non trouvé"));

        if (!evenement.isChatOuvert()) {
            throw new RuntimeException("Le chat n'est pas encore ouvert");
        }

        // Vérifie si c'est l'organisateur
        boolean estOrganisateur = evenement.getOrganisateur() != null
                && evenement.getOrganisateur().getId().equals(request.getCandidatId());

        // Vérifie si c'est un candidat confirmé
        boolean estConfirme = participationRepository
                .existsByCandidatIdAndEvenementIdAndStatut(
                        request.getCandidatId(),
                        request.getEvenementId(),
                        "CONFIRME"
                );

        if (!estOrganisateur && !estConfirme) {
            throw new RuntimeException("Accès refusé");
        }

        // ← Récupère le nom selon le rôle
        String nomExpediteur;
        Candidat candidat = null;

        if (estOrganisateur) {
            // C'est l'organisateur — récupère son nom
            nomExpediteur = evenement.getOrganisateur().getNom();
        } else {
            // C'est un candidat — récupère depuis la table candidat
            candidat = candidatRepository.findById(request.getCandidatId())
                    .orElseThrow(() -> new RuntimeException("Candidat non trouvé"));
            nomExpediteur = candidat.getNom() + " " + candidat.getPrenom();
        }

        ChatMessage message = ChatMessage.builder()
                .contenu(request.getContenu())
                .envoyeA(LocalDateTime.now())
                .nomExpediteur(nomExpediteur)          // ← nom correct selon le rôle
                .evenement(evenement)
                .candidat(candidat)                    // ← null si organisateur, c'est OK
                .build();

        ChatMessage saved = chatMessageRepository.save(message);
        ChatMessageResponse response = toResponse(saved);

        pusher.trigger(
                "chat-evenement-" + request.getEvenementId(),
                "nouveau-message",
                response
        );

        return response;
    }

    // Récupère l'historique des messages d'un événement
    public List<ChatMessageResponse> getMessages(Long evenementId, Long candidatId) {

        Evenement evenement = evenementRepository.findById(evenementId)
                .orElseThrow(() -> new RuntimeException("Événement non trouvé"));

        if (!evenement.isChatOuvert()) {
            throw new RuntimeException("Le chat n'est pas ouvert");
        }

        // ← Vérifie si c'est l'organisateur de l'événement
        boolean estOrganisateur = evenement.getOrganisateur() != null
                && evenement.getOrganisateur().getId().equals(candidatId);

        // ← Vérifie si c'est un candidat confirmé
        boolean estConfirme = participationRepository
                .existsByCandidatIdAndEvenementIdAndStatut(candidatId, evenementId, "CONFIRME");

        // ← Autorise si organisateur OU candidat confirmé
        if (!estOrganisateur && !estConfirme) {
            throw new RuntimeException("Accès refusé");
        }

        return chatMessageRepository
                .findByEvenementIdOrderByEnvoyeAAsc(evenementId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public boolean isChatOuvert(Long evenementId) {
        return evenementRepository.findById(evenementId)
                .map(Evenement::isChatOuvert)
                .orElse(false);
    }

    // Mapper
    private ChatMessageResponse toResponse(ChatMessage m) {
        return new ChatMessageResponse(
                m.getId(),
                m.getContenu(),
                m.getEnvoyeA(),
                m.getNomExpediteur(),
                m.getEvenement().getId(),
                m.getCandidat() != null ? m.getCandidat().getId() : null
        );
    }
}