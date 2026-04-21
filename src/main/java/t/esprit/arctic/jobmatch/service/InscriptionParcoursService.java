package t.esprit.arctic.jobmatch.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import t.esprit.arctic.jobmatch.entity.*;
import t.esprit.arctic.jobmatch.repository.CandidatRepository;
import t.esprit.arctic.jobmatch.repository.InscriptionParcoursRepository;
import t.esprit.arctic.jobmatch.repository.ParcoursFormationRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InscriptionParcoursService {

    private final InscriptionParcoursRepository inscriptionParcoursRepository;
    private final ParcoursFormationRepository parcoursRepository;
    private final CandidatRepository candidatRepository;
    private final InscriptionFormationService inscriptionFormationService;

    /**
     * Inscrit un candidat à un parcours. Le niveau initial est DEBUTANT.
     */
    @Transactional
    public InscriptionParcours inscrire(Long candidatId, Long parcoursId) {
        // Vérifier si déjà inscrit, si oui on retourne l'existant (idempotence)
        Optional<InscriptionParcours> existing = inscriptionParcoursRepository.findByCandidatIdAndParcoursId(candidatId, parcoursId);
        if (existing.isPresent()) {
            return existing.get();
        }

        Candidat candidat = candidatRepository.findById(candidatId)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé : " + candidatId));
        ParcoursFormation parcours = parcoursRepository.findById(parcoursId)
                .orElseThrow(() -> new RuntimeException("Parcours non trouvé : " + parcoursId));

        InscriptionParcours inscription = new InscriptionParcours();
        inscription.setCandidat(candidat);
        inscription.setParcours(parcours);
        inscription.setNiveauActuel(NiveauOrdre.DEBUTANT);
        inscription.setDateInscription(new Date());
        inscription.setStatut("EN_COURS");

        InscriptionParcours saved = inscriptionParcoursRepository.save(inscription);

        // Inscription automatique au premier niveau (DEBUTANT)
        Formation beginnerLevel = parcours.getNiveauDebutant();
        if (beginnerLevel != null) {
            inscriptionFormationService.inscrireAutomatiquement(candidat, beginnerLevel);
        }

        return saved;
    }

    /**
     * Retourne toutes les inscriptions parcours d'un candidat.
     */
    @Transactional(readOnly = true)
    public List<InscriptionParcours> getMesInscriptions(Long candidatId) {
        return inscriptionParcoursRepository.findByCandidatId(candidatId);
    }

    /**
     * Retourne l'inscription spécifique d'un candidat à un parcours.
     */
    @Transactional
    public InscriptionParcours getInscription(Long candidatId, Long parcoursId) {
        InscriptionParcours ins = inscriptionParcoursRepository.findByCandidatIdAndParcoursId(candidatId, parcoursId)
                .orElseThrow(() -> new RuntimeException(
                        "Inscription non trouvée pour candidat " + candidatId + " et parcours " + parcoursId));
        syncProgression(ins);
        return ins;
    }

    /**
     * Retourne l'inscription par son ID.
     */
    @Transactional
    public InscriptionParcours getById(Long id) {
        InscriptionParcours ins = inscriptionParcoursRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inscription parcours non trouvée : " + id));
        syncProgression(ins);
        return ins;
    }

    /**
     * Synchronise la progression des formations individuelles avec l'état du parcours.
     * Si un niveau est validé dans le parcours, sa formation doit être à 100%.
     */
    @Transactional
    public void syncProgression(InscriptionParcours ins) {
        if (ins == null || ins.getParcours() == null) return;

        ParcoursFormation p = ins.getParcours();
        Candidat c = ins.getCandidat();
        NiveauOrdre actuel = ins.getNiveauActuel();
        boolean estTermine = "TERMINE".equals(ins.getStatut());

        // Liste des niveaux à synchroniser (ceux qui sont sensés être terminés)
        for (NiveauOrdre niv : NiveauOrdre.values()) {
            boolean doitEtreFini = estTermine || niv.ordinal() < actuel.ordinal();
            
            if (doitEtreFini) {
                Formation f = p.getFormationParNiveau(niv);
                if (f != null) {
                    inscriptionFormationService.marquerCommeTerminee(c, f);
                }
            }
        }
    }

    /**
     * Retourne la Formation correspondant au niveau actuel du candidat.
     */
    @Transactional(readOnly = true)
    public Formation getFormationActuelle(Long inscriptionId) {
        InscriptionParcours inscription = getById(inscriptionId);
        ParcoursFormation parcours = inscription.getParcours();
        return parcours.getFormationParNiveau(inscription.getNiveauActuel());
    }
}

