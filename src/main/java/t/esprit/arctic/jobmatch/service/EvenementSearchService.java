package t.esprit.arctic.jobmatch.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import t.esprit.arctic.jobmatch.dto.EvenementResponse;
import t.esprit.arctic.jobmatch.dto.EvenementSearchDTO;
import t.esprit.arctic.jobmatch.entity.Candidat;
import t.esprit.arctic.jobmatch.entity.Evenement;
import t.esprit.arctic.jobmatch.entity.RechercheHistorique;
import t.esprit.arctic.jobmatch.repository.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EvenementSearchService {

    private final EvenementRepository evenementRepository;
    private final ParticipationRepository participationRepository;
    private final FeedbackEventRepository feedbackEventRepository;
    private final RechercheHistoriqueRepository rechercheHistoriqueRepository;
    private final CandidatRepository candidatRepository;

    // ─────────────────────────────────────────────────────────────────
    // RECHERCHE PRINCIPALE
    // Analyse la requête et scanne titre + lieu + type + date simultanément
    // Sauvegarde aussi le terme dans l'historique du candidat
    // ─────────────────────────────────────────────────────────────────
    @Transactional
    public EvenementSearchDTO rechercher(String terme, Long candidatId) {

        // 1. Récupère tous les événements de la base
        List<Evenement> tous = evenementRepository.findAll();

        // 2. Filtre selon la requête (multi-champs)
        List<EvenementResponse> resultats = tous.stream()
                .filter(ev -> matchRecherche(ev, terme))
                .map(this::toResponse)
                .collect(Collectors.toList());

        // 3. Sauvegarde la recherche dans l'historique si terme non vide
        if (terme != null && !terme.isBlank() && candidatId != null) {
            sauvegarderHistorique(terme.trim(), candidatId);
        }

        // 4. Calcule les suggestions personnalisées
        List<EvenementResponse> suggestions = getSuggestions(candidatId);

        // 5. Récupère l'historique des dernières recherches
        List<String> historique = getHistorique(candidatId);

        return EvenementSearchDTO.builder()
                .resultats(resultats)
                .suggestions(suggestions)
                .historiqueRecherches(historique)
                .totalResultats(resultats.size())
                .build();
    }

    // ─────────────────────────────────────────────────────────────────
    // MOTEUR DE MATCHING MULTICRITÈRES
    // Une requête "Job Tunis 2025" va chercher dans tous les champs
    // On découpe la requête en mots et chaque mot est cherché partout
    // ─────────────────────────────────────────────────────────────────
    private boolean matchRecherche(Evenement ev, String terme) {
        if (terme == null || terme.isBlank()) return true; // pas de filtre → tout retourner

        // Normalise : minuscules + sans accents pour comparaison insensible
        String q = normaliser(terme);

        // Chaque mot de la requête doit matcher au moins un champ
        String[] mots = q.split("\\s+");
        for (String mot : mots) {
            boolean motTrouve =
                    normaliser(ev.getTitre()).contains(mot) ||
                            normaliser(ev.getLieu()).contains(mot) ||
                            normaliser(ev.getType()).contains(mot) ||
                            // Recherche dans l'année/mois de la date (ex: "2025" ou "janvier")
                            (ev.getDateHeure() != null &&
                                    String.valueOf(ev.getDateHeure().getYear()).contains(mot));

            if (!motTrouve) return false; // tous les mots doivent matcher
        }
        return true;
    }

    // ─────────────────────────────────────────────────────────────────
    // SUGGESTIONS PERSONNALISÉES
    // Basées sur :
    //   - Les types d'événements où le candidat a participé (historique)
    //   - Les types les mieux notés dans ses feedbacks
    // Retourne des événements futurs de ces types préférés
    // ─────────────────────────────────────────────────────────────────
    public List<EvenementResponse> getSuggestions(Long candidatId) {
        if (candidatId == null) return List.of();

        // Étape A : types favoris depuis les participations passées
        List<String> typesFavorisParticipation =
                participationRepository.findTypeFavoriByCandidat(candidatId);

        // Étape B : types favoris depuis les feedbacks (notes élevées)
        List<String> typesFavorisFeedback =
                feedbackEventRepository.findTypesFavorisParCandidat(candidatId);

        // Étape C : union des deux listes (sans doublon, les participations ont priorité)
        Set<String> typesCibles = new LinkedHashSet<>();
        typesCibles.addAll(typesFavorisParticipation);
        typesCibles.addAll(typesFavorisFeedback);

        if (typesCibles.isEmpty()) return List.of();

        // Étape D : récupère les événements futurs correspondant à ces types
        // et exclut ceux où le candidat est déjà inscrit
        LocalDateTime maintenant = LocalDateTime.now();

        return evenementRepository.findAll().stream()
                .filter(ev -> ev.getDateHeure() != null && ev.getDateHeure().isAfter(maintenant))
                .filter(ev -> typesCibles.contains(ev.getType()))
                .filter(ev -> !participationRepository
                        .existsByCandidatIdAndEvenementId(candidatId, ev.getId()))
                .limit(5) // max 5 suggestions
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────
    // HISTORIQUE DES RECHERCHES
    // Retourne les 5 derniers termes recherchés par le candidat
    // ─────────────────────────────────────────────────────────────────
    public List<String> getHistorique(Long candidatId) {
        if (candidatId == null) return List.of();

        return rechercheHistoriqueRepository
                .findByCandidatIdOrderByDateRechercheDesc(candidatId)
                .stream()
                .map(RechercheHistorique::getTerme)
                .limit(5)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────
    // SAUVEGARDE HISTORIQUE
    // Si le terme existe déjà → on supprime et recrée (refresh de la date)
    // Limite à 10 entrées max par candidat
    // ─────────────────────────────────────────────────────────────────

    private void sauvegarderHistorique(String terme, Long candidatId) {
        Candidat candidat = candidatRepository.findById(candidatId).orElse(null);
        if (candidat == null) return;

        // Supprime l'ancien si existant (pour remonter en tête de liste)
        if (rechercheHistoriqueRepository.existsByCandidatIdAndTerme(candidatId, terme)) {
            rechercheHistoriqueRepository.deleteByCandidatIdAndTerme(candidatId, terme);
        }

        // Limite à 10 entrées : supprime les plus anciennes
        List<RechercheHistorique> historique =
                rechercheHistoriqueRepository.findByCandidatIdOrderByDateRechercheDesc(candidatId);
        if (historique.size() >= 10) {
            // Supprime les entrées au-delà de 9 (les plus anciennes)
            historique.subList(9, historique.size())
                    .forEach(rechercheHistoriqueRepository::delete);
        }

        // Sauvegarde la nouvelle entrée
        rechercheHistoriqueRepository.save(
                RechercheHistorique.builder()
                        .terme(terme)
                        .dateRecherche(LocalDateTime.now())
                        .candidat(candidat)
                        .build()
        );
    }

    // ─────────────────────────────────────────────────────────────────
    // UTILITAIRE : normalise un texte (minuscules, sans accents)
    // Permet une comparaison insensible à la casse et aux accents
    // ─────────────────────────────────────────────────────────────────
    private String normaliser(String texte) {
        if (texte == null) return "";
        return java.text.Normalizer
                .normalize(texte, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase();
    }

    // Mapper Evenement → EvenementResponse
    private EvenementResponse toResponse(Evenement ev) {
        return new EvenementResponse(
                ev.getId(),
                ev.getTitre(),
                ev.getDateHeure(),
                ev.getLieu(),
                ev.getType(),
                ev.getOrganisateur() != null ? ev.getOrganisateur().getId() : null,
                ev.getOrganisateur() != null ? ev.getOrganisateur().getNom() : null,
                ev.isChatOuvert()
        );
    }
}