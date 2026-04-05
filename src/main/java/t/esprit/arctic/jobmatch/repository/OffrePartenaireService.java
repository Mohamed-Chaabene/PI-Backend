package t.esprit.arctic.jobmatch.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Date;

import t.esprit.arctic.jobmatch.entity.OffrePartenaire;
import t.esprit.arctic.jobmatch.entity.TypeOffrePartenaire;
import t.esprit.arctic.jobmatch.repository.OffrePartenaireRepository;
import t.esprit.arctic.jobmatch.repository.PartenaireRepository;

@Service
@RequiredArgsConstructor
public class OffrePartenaireService {

    private final OffrePartenaireRepository offreRepo;
    private final PartenaireRepository partenaireRepo;

    public List<OffrePartenaire> getAll() {
        return offreRepo.findAll();
    }

    public OffrePartenaire getById(Long id) {
        return offreRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Offre non trouvée"));
    }

    public OffrePartenaire create(OffrePartenaire o) {
        o.setDatePublication(new Date());
        return offreRepo.save(o);
    }

    public OffrePartenaire update(Long id, OffrePartenaire o) {
        // Récupérer l'offre existante
        OffrePartenaire existing = offreRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Offre non trouvée"));

        // Mettre à jour seulement les champs modifiés
        existing.setTitre(o.getTitre());
        existing.setDescription(o.getDescription());
        existing.setType(o.getType());

        // Garder le partenaire existant
        return offreRepo.save(existing);
    }

    public void delete(Long id) {
        offreRepo.deleteById(id);
    }

    public List<OffrePartenaire> getByPartenaire(Long partenaireId) {
        return offreRepo.findByPartenaireId(partenaireId);
    }

    public List<OffrePartenaire> getByType(TypeOffrePartenaire type) {
        return offreRepo.findByType(type);
    }
}
