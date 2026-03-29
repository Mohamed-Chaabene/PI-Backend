package t.esprit.arctic.jobmatch.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
<<<<<<< HEAD
import t.esprit.arctic.jobmatch.entity.Candidat;
import t.esprit.arctic.jobmatch.entity.Localisation;
import t.esprit.arctic.jobmatch.repository.CandidatRepository;
=======
import t.esprit.arctic.jobmatch.entity.Localisation;
>>>>>>> origin/Entre_tien
import t.esprit.arctic.jobmatch.repository.LocalisationRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocalisationService {

    private final LocalisationRepository repository;
<<<<<<< HEAD
    private final CandidatRepository candidatRepository;
=======
>>>>>>> origin/Entre_tien

    public Localisation create(Localisation localisation) {
        return repository.save(localisation);
    }

    public List<Localisation> getAll() {
        return repository.findAll();
    }

    public Localisation getById(Long id) {
        return repository.findById(id).orElseThrow(() -> 
            new RuntimeException("Localisation not found with id: " + id));
    }

    public Localisation update(Long id, Localisation localisationDetails) {
        Localisation localisation = getById(id);
        localisation.setPays(localisationDetails.getPays());
        localisation.setVille(localisationDetails.getVille());
        localisation.setLongitude(localisationDetails.getLongitude());
        localisation.setLatitude(localisationDetails.getLatitude());
        return repository.save(localisation);
    }

    public void delete(Long id) {
<<<<<<< HEAD
        Localisation localisation = getById(id);
        
        // Find all candidats referencing this localisation
        List<Candidat> candidats = candidatRepository.findByLocalisation(localisation);
        
        // Clear the localisation reference from all candidats
        for (Candidat candidat : candidats) {
            candidat.setLocalisation(null);
            candidatRepository.save(candidat);
        }
        
        // Now delete the localisation
=======
>>>>>>> origin/Entre_tien
        repository.deleteById(id);
    }
}

