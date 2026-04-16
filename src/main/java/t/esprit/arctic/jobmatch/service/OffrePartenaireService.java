package t.esprit.arctic.jobmatch.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.Date;
import java.util.Calendar;

import t.esprit.arctic.jobmatch.entity.OffrePartenaire;
import t.esprit.arctic.jobmatch.entity.TypeOffrePartenaire;
import t.esprit.arctic.jobmatch.entity.TypePartenaire;
import t.esprit.arctic.jobmatch.entity.Partenaire;
import t.esprit.arctic.jobmatch.repository.OffrePartenaireRepository;
import t.esprit.arctic.jobmatch.repository.PartenaireRepository;
<<<<<<< HEAD
=======
import t.esprit.arctic.jobmatch.dto.ActivityEvent;        // ← NOUVEAU
import t.esprit.arctic.jobmatch.service.WebSocketService; // ← NOUVEAU
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0

@Service
@RequiredArgsConstructor
public class OffrePartenaireService {

    private final OffrePartenaireRepository offreRepo;
    private final PartenaireRepository partenaireRepo;
<<<<<<< HEAD
=======
    private final WebSocketService webSocketService;
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0

    public List<OffrePartenaire> getAll() {
        return offreRepo.findAll();
    }

    public OffrePartenaire getById(Long id) {
        return offreRepo.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Offre non trouvée"));
    }

<<<<<<< HEAD
    public OffrePartenaire create(OffrePartenaire o) {
        o.setDatePublication(new Date());
        return offreRepo.save(o);
    }

=======

    public OffrePartenaire create(OffrePartenaire o) {
        o.setDatePublication(new Date());
        OffrePartenaire saved = offreRepo.save(o);
        System.out.println(" Push WebSocket : " + saved.getTitre());

        Partenaire p  = saved.getPartenaire();
        String nom    = p != null ? p.getNom() : "Partenaire";
        String type   = saved.getType() != null
                ? saved.getType().name() : "EMPLOI";

        webSocketService.sendActivity(new ActivityEvent(
                "NOUVELLE_OFFRE",
                nom,
                type,
                nom + " a publié une offre " + type.toLowerCase(),
                "",
                type.equals("EMPLOI") ? "💼" : "🎓"
        ));

        return saved;
    }


>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
    public OffrePartenaire update(Long id, OffrePartenaire o) {
        OffrePartenaire existing = offreRepo.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Offre non trouvée"));
        existing.setTitre(o.getTitre());
        existing.setDescription(o.getDescription());
        existing.setType(o.getType());
        return offreRepo.save(existing);
    }

    public void delete(Long id) {
        offreRepo.deleteById(id);
    }

<<<<<<< HEAD
    public List<OffrePartenaire> getByPartenaire(
            Long partenaireId) {
        return offreRepo.findByPartenaireId(partenaireId);
    }

    public List<OffrePartenaire> getByType(
            TypeOffrePartenaire type) {
        return offreRepo.findByType(type);
    }

    public List<OffrePartenaire> searchByKeyword(
            String keyword) {
=======
    public List<OffrePartenaire> getByPartenaire(Long partenaireId) {
        return offreRepo.findByPartenaireId(partenaireId);
    }

    public List<OffrePartenaire> getByType(TypeOffrePartenaire type) {
        return offreRepo.findByType(type);
    }

    public List<OffrePartenaire> searchByKeyword(String keyword) {
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
        if (keyword == null || keyword.trim().isEmpty()) {
            return offreRepo.findAll();
        }
        return offreRepo.searchByKeyword(keyword.trim());
    }

<<<<<<< HEAD

=======
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
    @Transactional
    public String predictNextOffreType(Long partenaireId) {

        List<OffrePartenaire> offres =
                offreRepo.findByPartenaireId(partenaireId);

<<<<<<< HEAD
        // Pas d'offres → EMPLOI par défaut
=======
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
        if (offres == null || offres.isEmpty()) {
            return "EMPLOI (50%)";
        }

<<<<<<< HEAD

        long nbEmploi = offres.stream()
                .filter(o -> o.getType()
                        == TypeOffrePartenaire.EMPLOI)
                .count();

        long nbStage = offres.stream()
                .filter(o -> o.getType()
                        == TypeOffrePartenaire.STAGE)
=======
        long nbEmploi = offres.stream()
                .filter(o -> o.getType() == TypeOffrePartenaire.EMPLOI)
                .count();

        long nbStage = offres.stream()
                .filter(o -> o.getType() == TypeOffrePartenaire.STAGE)
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
                .count();

        long total = nbEmploi + nbStage;

<<<<<<< HEAD

        double probEmploi = (double) nbEmploi / total;
        double probStage  = (double) nbStage  / total;


        int moisActuel = Calendar.getInstance()
                .get(Calendar.MONTH) + 1;

        // Juin-Septembre → période de stage
        boolean periodStage =
                (moisActuel >= 6 && moisActuel <= 9);
=======
        double probEmploi = (double) nbEmploi / total;
        double probStage  = (double) nbStage  / total;

        int moisActuel = Calendar.getInstance().get(Calendar.MONTH) + 1;
        boolean periodStage = (moisActuel >= 6 && moisActuel <= 9);
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0

        if (periodStage) {
            probStage  *= 1.5;
        } else {
            probEmploi *= 1.5;
        }

<<<<<<< HEAD

=======
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
        Partenaire partenaire = partenaireRepo
                .findById(partenaireId).orElse(null);

        if (partenaire != null) {
<<<<<<< HEAD
            if (partenaire.getType()
                    == TypePartenaire.UNIVERSITE) {
                // Université → plus de stages
                probStage *= 1.8;
            } else {
                // Entreprise → plus d'emplois
=======
            if (partenaire.getType() == TypePartenaire.UNIVERSITE) {
                probStage  *= 1.8;
            } else {
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
                probEmploi *= 1.8;
            }
        }

<<<<<<< HEAD

=======
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
        List<OffrePartenaire> dernieres = offres.stream()
                .sorted((a, b) -> {
                    if (a.getDatePublication() == null) return 1;
                    if (b.getDatePublication() == null) return -1;
                    return b.getDatePublication()
                            .compareTo(a.getDatePublication());
                })
                .limit(3)
                .collect(java.util.stream.Collectors.toList());

        for (OffrePartenaire o : dernieres) {
            if (o.getType() == TypeOffrePartenaire.EMPLOI) {
                probEmploi *= 1.3;
            } else {
<<<<<<< HEAD
                probStage *= 1.3;
            }
        }


        double totalProb = probEmploi + probStage;
        int confidenceEmploi =
                (int)((probEmploi / totalProb) * 100);
        int confidenceStage =
                (int)((probStage / totalProb) * 100);
=======
                probStage  *= 1.3;
            }
        }

        double totalProb     = probEmploi + probStage;
        int confidenceEmploi = (int)((probEmploi / totalProb) * 100);
        int confidenceStage  = (int)((probStage  / totalProb) * 100);
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0

        if (probEmploi >= probStage) {
            return "EMPLOI (" + confidenceEmploi + "%)";
        } else {
<<<<<<< HEAD
            return "STAGE (" + confidenceStage + "%)";
        }
    }


=======
            return "STAGE ("  + confidenceStage  + "%)";
        }
    }

>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
    public OffrePartenaire toggleEpingle(Long offreId) {
        OffrePartenaire offre = getById(offreId);
        offre.setEpinglee(!offre.isEpinglee());
        return offreRepo.save(offre);
    }

<<<<<<< HEAD

    public List<OffrePartenaire> getByPartenaireTriees(
            Long partenaireId) {
        return offreRepo.findByPartenaireId(partenaireId)
                .stream()
                .sorted((a, b) -> {
                    if (a.isEpinglee() && !b.isEpinglee()) return -1;
                    if (!a.isEpinglee() && b.isEpinglee()) return 1;
=======
    public List<OffrePartenaire> getByPartenaireTriees(Long partenaireId) {
        return offreRepo.findByPartenaireId(partenaireId)
                .stream()
                .sorted((a, b) -> {
                    if (a.isEpinglee() && !b.isEpinglee())  return -1;
                    if (!a.isEpinglee() && b.isEpinglee())  return 1;
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
                    return 0;
                })
                .collect(java.util.stream.Collectors.toList());
    }
}