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

    public List<InscriptionFormation> getAll() {
        return inscriptionRepository.findAll();
    }

    public InscriptionFormation getById(Long id) {
        return inscriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inscription non trouvée avec l'id : " + id));
    }

    public InscriptionFormation create(InscriptionFormation inscription) {
        inscription.setDateInscription(new Date());
        inscription.setStatut("EnCours");
        inscription.setProgression(0.0);
        return inscriptionRepository.save(inscription);
    }

    public InscriptionFormation update(Long id, InscriptionFormation updated) {
        InscriptionFormation existing = getById(id);
        existing.setProgression(updated.getProgression());

        // Génération automatique du certificat si progression = 100%
        if (updated.getProgression() >= 100.0) {
            existing.setStatut("Terminé");
            certificatService.genererAutomatiquement(existing);
        } else if (updated.getProgression() == 0.0) {
            existing.setStatut("Abandonné");
        } else {
            existing.setStatut("EnCours");
        }

        return inscriptionRepository.save(existing);
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