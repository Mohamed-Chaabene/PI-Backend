package t.esprit.arctic.jobmatch.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import t.esprit.arctic.jobmatch.entity.Formation;
import t.esprit.arctic.jobmatch.repository.FormationRepository;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FormationService {

    private final FormationRepository formationRepository;

    public List<Formation> getAll() {
        return formationRepository.findAll();
    }

    public Formation getById(Long id) {
        return formationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Formation non trouvée avec l'id : " + id));
    }

    public Formation create(Formation formation) {
        return formationRepository.save(formation);
    }

    public Formation update(Long id, Formation updated) {
        Formation existing = getById(id);
        existing.setTitre(updated.getTitre());
        existing.setCategorie(updated.getCategorie());
        existing.setPlateforme(updated.getPlateforme());
        existing.setStatut(updated.getStatut());
        existing.setDuree(updated.getDuree());
        existing.setNiveau(updated.getNiveau());
        existing.setCompetences(updated.getCompetences());
        return formationRepository.save(existing);
    }

    public void delete(Long id) {
        getById(id);
        formationRepository.deleteById(id);
    }

    public List<Formation> getByNiveau(String niveau) {
        return formationRepository.findByNiveau(niveau);
    }

    public List<Formation> getByCategorie(String categorie) {
        return formationRepository.findByCategorie(categorie);
    }
}