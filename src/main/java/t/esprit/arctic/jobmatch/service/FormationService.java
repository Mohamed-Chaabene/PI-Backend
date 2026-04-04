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

    // ── CRUD ─────────────────────────────────────────────────────────────────

    public List<Formation> getAll() {
        return formationRepository.findAll();
    }

    // ✅ Liste publique — exclut les formations archivées
    public List<Formation> getAllActives() {
        List<Formation> formations = formationRepository.findByStatutNot("Archivée");
        System.out.println("📚 getAllActives() retourne: " + formations.size() + " formations");
        formations.forEach(f -> System.out.println("  - " + f.getTitre() + " (statut: " + f.getStatut() + ")"));
        return formations;
    }

    // ✅ Liste admin — toutes formations y compris archivées
    public List<Formation> getAllForAdmin() {
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

    // ✅ Archiver une formation (admin)
    public Formation archiver(Long id) {
        Formation formation = getById(id);
        formation.setStatut("Archivée");
        return formationRepository.save(formation);
    }

    // ✅ Désarchiver une formation (admin) — remet en "Disponible"
    public Formation desarchiver(Long id) {
        Formation formation = getById(id);
        formation.setStatut("Disponible");
        return formationRepository.save(formation);
    }

    // ── Filtres ───────────────────────────────────────────────────────────────

    public List<Formation> getByNiveau(String niveau) {
        return formationRepository.findByNiveau(niveau);
    }

    public List<Formation> getByCategorie(String categorie) {
        return formationRepository.findByCategorie(categorie);
    }

    public List<Formation> getArchivees() {
        return formationRepository.findByStatut("Archivée");
    }
}