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
    private final CertificatService certificatService;

    // Seuil minimum pour obtenir le certificat
    private static final double SEUIL_CERTIFICAT = 70.0;

    public List<InscriptionFormation> getAll() {
        return inscriptionRepository.findAll();
    }

    public InscriptionFormation getById(Long id) {
        return inscriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Inscription non trouvée avec l'id : " + id));
    }

    public InscriptionFormation create(InscriptionFormation inscription) {
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

        // ✅ FIX : statut mis à jour selon progression
        if (progression >= 100.0) {
            existing.setStatut("Terminé");
        } else if (progression == 0.0) {
            existing.setStatut("Abandonné");
        } else {
            existing.setStatut("EnCours");
        }

        // ✅ FIX PRINCIPAL : certificat généré uniquement si score quiz >= 70%
        // (géré côté quiz, pas ici — on ne génère plus le certificat à 100% de progression)

        return inscriptionRepository.save(existing);
    }

    // ✅ NOUVELLE méthode appelée après le quiz final
    @Transactional
    public InscriptionFormation mettreAJourApresQuiz(
            Long id, double scoreQuiz) {

        InscriptionFormation existing = getById(id);

        // Générer le certificat seulement si score >= 70%
        if (scoreQuiz >= SEUIL_CERTIFICAT) {
            existing.setStatut("Terminé");
            inscriptionRepository.save(existing);

            // Générer le certificat si pas déjà existant
            try {
                certificatService.genererAutomatiquement(existing);
            } catch (RuntimeException e) {
                // Certificat déjà existant → ignorer
            }
        }

        return existing;
    }

    public void delete(Long id) {
        getById(id);
        inscriptionRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<InscriptionFormation> getByCandidat(Long candidatId) {
        return inscriptionRepository.findByCandidatId(candidatId);
    }

    @Transactional(readOnly = true)
    public List<InscriptionFormation> getByFormation(Long formationId) {
        return inscriptionRepository.findByFormationId(formationId);
    }
}