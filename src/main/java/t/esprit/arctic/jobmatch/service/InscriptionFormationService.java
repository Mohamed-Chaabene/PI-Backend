package t.esprit.arctic.jobmatch.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import t.esprit.arctic.jobmatch.entity.InscriptionFormation;
import t.esprit.arctic.jobmatch.repository.InscriptionFormationRepository;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InscriptionFormationService {

    private final InscriptionFormationRepository inscriptionRepository;
    private final t.esprit.arctic.jobmatch.repository.CandidatRepository candidatRepository;
    private final t.esprit.arctic.jobmatch.repository.FormationRepository formationRepository;
    private final CertificatService certificatService;

    private static final double SEUIL_CERTIFICAT = 70.0;

    public List<InscriptionFormation> getAll() {
        return inscriptionRepository.findAll();
    }

    public InscriptionFormation getById(Long id) {
        return inscriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Inscription non trouvée avec l'id : " + id));
    }

    @Transactional
    public InscriptionFormation create(InscriptionFormation inscription) {
        t.esprit.arctic.jobmatch.entity.Formation formation = formationRepository.findById(inscription.getFormation().getId())
                .orElseThrow(() -> new RuntimeException("Formation non trouvée avec l'id : " + inscription.getFormation().getId()));

        t.esprit.arctic.jobmatch.entity.Candidat candidat = candidatRepository.findById(inscription.getCandidat().getId())
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé avec l'id : " + inscription.getCandidat().getId()));

        inscription.setFormation(formation);
        inscription.setCandidat(candidat);
        inscription.setDateInscription(new Date());
        inscription.setStatut("EnCours");
        inscription.setProgression(0.0);
        return inscriptionRepository.save(inscription);
    }

    @Transactional
    public InscriptionFormation update(Long id, InscriptionFormation updated) {
        InscriptionFormation existing = getById(id);

        double progression = updated.getProgression();
        existing.setProgression(progression);

        if (progression >= 100.0) {
            existing.setStatut("Terminé");
        } else if (progression == 0.0) {
            existing.setStatut("Abandonné");
        } else {
            existing.setStatut("EnCours");
        }


        return inscriptionRepository.save(existing);
    }

    @Transactional
    public InscriptionFormation mettreAJourApresQuiz(
            Long id, double scoreQuiz) {

        InscriptionFormation existing = getById(id);

        if (scoreQuiz >= SEUIL_CERTIFICAT) {
            existing.setStatut("Terminé");
            inscriptionRepository.save(existing);

            try {
                certificatService.genererAutomatiquement(existing);
            } catch (RuntimeException e) {
            }
        }

        return existing;
    }

    @Transactional
    public InscriptionFormation inscrireAutomatiquement(t.esprit.arctic.jobmatch.entity.Candidat candidat, t.esprit.arctic.jobmatch.entity.Formation formation) {
        if (formation == null) return null;

        // Vérifier si déjà inscrit
        return inscriptionRepository.findByCandidatIdAndFormationId(candidat.getId(), formation.getId())
                .orElseGet(() -> {
                    InscriptionFormation newIns = new InscriptionFormation();
                    newIns.setCandidat(candidat);
                    newIns.setFormation(formation);
                    newIns.setDateInscription(new Date());
                    newIns.setStatut("EnCours");
                    newIns.setProgression(0.0);
                    return inscriptionRepository.save(newIns);
                });
    }

    /**
     * Force la progression d'une formation à 100% (utilisé lors de la validation d'un niveau dans un parcours).
     */
    @Transactional
    public void marquerCommeTerminee(t.esprit.arctic.jobmatch.entity.Candidat candidat, t.esprit.arctic.jobmatch.entity.Formation formation) {
        if (candidat == null || formation == null) return;

        InscriptionFormation ins = inscriptionRepository.findByCandidatIdAndFormationId(candidat.getId(), formation.getId())
                .orElseGet(() -> {
                    InscriptionFormation newIns = new InscriptionFormation();
                    newIns.setCandidat(candidat);
                    newIns.setFormation(formation);
                    newIns.setDateInscription(new Date());
                    return newIns;
                });

        ins.setProgression(100.0);
        ins.setStatut("Terminé");
        inscriptionRepository.save(ins);
        System.out.println("✅ Formation synchronisée à 100% pour " + candidat.getNom() + " sur " + formation.getTitre());
    }


    public void delete(Long id) {
        getById(id);
        inscriptionRepository.deleteById(id);
    }


    @Transactional(readOnly = true)
    public List<InscriptionFormation> getByCandidat(Long candidatId) {
        List<InscriptionFormation> result = inscriptionRepository.findByCandidatId(candidatId);
        if (result.isEmpty()) {
            boolean candidatExiste = candidatRepository.existsById(candidatId);
            if (!candidatExiste) {
                throw new RuntimeException("Candidat non trouvé avec l'id : " + candidatId);
            }
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<InscriptionFormation> getByFormation(Long formationId) {
        return inscriptionRepository.findByFormationId(formationId);
    }
}