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
<<<<<<< HEAD
    private final CertificatService certificatService;

    // Seuil minimum pour obtenir le certificat
=======
    private final t.esprit.arctic.jobmatch.repository.CandidatRepository candidatRepository;
    private final t.esprit.arctic.jobmatch.repository.FormationRepository formationRepository;
    private final CertificatService certificatService;

>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
    private static final double SEUIL_CERTIFICAT = 70.0;

    public List<InscriptionFormation> getAll() {
        return inscriptionRepository.findAll();
    }

    public InscriptionFormation getById(Long id) {
        return inscriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Inscription non trouvée avec l'id : " + id));
    }

<<<<<<< HEAD
    public InscriptionFormation create(InscriptionFormation inscription) {
=======
    @Transactional
    public InscriptionFormation create(InscriptionFormation inscription) {
        t.esprit.arctic.jobmatch.entity.Formation formation = formationRepository.findById(inscription.getFormation().getId())
                .orElseThrow(() -> new RuntimeException("Formation non trouvée avec l'id : " + inscription.getFormation().getId()));

        t.esprit.arctic.jobmatch.entity.Candidat candidat = candidatRepository.findById(inscription.getCandidat().getId())
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé avec l'id : " + inscription.getCandidat().getId()));

        inscription.setFormation(formation);
        inscription.setCandidat(candidat);
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
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

<<<<<<< HEAD
        // ✅ FIX : statut mis à jour selon progression
=======
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
        if (progression >= 100.0) {
            existing.setStatut("Terminé");
        } else if (progression == 0.0) {
            existing.setStatut("Abandonné");
        } else {
            existing.setStatut("EnCours");
        }

<<<<<<< HEAD
        // ✅ FIX PRINCIPAL : certificat généré uniquement si score quiz >= 70%
        // (géré côté quiz, pas ici — on ne génère plus le certificat à 100% de progression)
=======
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0

        return inscriptionRepository.save(existing);
    }

<<<<<<< HEAD
    // ✅ NOUVELLE méthode appelée après le quiz final
=======
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
    @Transactional
    public InscriptionFormation mettreAJourApresQuiz(
            Long id, double scoreQuiz) {

        InscriptionFormation existing = getById(id);

<<<<<<< HEAD
        // Générer le certificat seulement si score >= 70%
=======
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
        if (scoreQuiz >= SEUIL_CERTIFICAT) {
            existing.setStatut("Terminé");
            inscriptionRepository.save(existing);

<<<<<<< HEAD
            // Générer le certificat si pas déjà existant
            try {
                certificatService.genererAutomatiquement(existing);
            } catch (RuntimeException e) {
                // Certificat déjà existant → ignorer
=======
            try {
                certificatService.genererAutomatiquement(existing);
            } catch (RuntimeException e) {
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
            }
        }

        return existing;
    }

    public void delete(Long id) {
        getById(id);
        inscriptionRepository.deleteById(id);
    }

<<<<<<< HEAD
    @Transactional(readOnly = true)
    public List<InscriptionFormation> getByCandidat(Long candidatId) {
        return inscriptionRepository.findByCandidatId(candidatId);
=======

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
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
    }

    @Transactional(readOnly = true)
    public List<InscriptionFormation> getByFormation(Long formationId) {
        return inscriptionRepository.findByFormationId(formationId);
    }
}