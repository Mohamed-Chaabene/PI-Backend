package t.esprit.arctic.jobmatch.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import t.esprit.arctic.jobmatch.dto.CandidatListDto;
import t.esprit.arctic.jobmatch.entity.Background;
import t.esprit.arctic.jobmatch.entity.Candidat;
import t.esprit.arctic.jobmatch.entity.Education;
import t.esprit.arctic.jobmatch.exception.ResourceNotFoundException;
import t.esprit.arctic.jobmatch.repository.BackgroundRepository;
import t.esprit.arctic.jobmatch.repository.CandidatRepository;
import t.esprit.arctic.jobmatch.repository.EducationRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CandidatService {

    private final CandidatRepository repository;
    private final EducationRepository educationRepository;
    private final BackgroundRepository backgroundRepository;

    public Candidat create(Candidat candidat) {
        // Clear background and education lists since they are stored as concatenated strings
        // in backgroundExpertise and niveauEtude fields respectively
        if (candidat.getBackgrounds() != null) {
            candidat.getBackgrounds().clear();
        }
        if (candidat.getEducations() != null) {
            candidat.getEducations().clear();
        }
        return repository.save(candidat);
    }

    @Transactional(readOnly = true)
    public List<CandidatListDto> getAll() {
        return repository.findAllProjected();
    }

    @Transactional(readOnly = true)
    public Candidat getById(Long id) {
        Candidat c = repository.findById(id).orElseThrow(() ->
            new ResourceNotFoundException("Candidat not found with id: " + id));
        if (c.getEducations() != null) {
            c.getEducations().size();
        }
        if (c.getBackgrounds() != null) {
            c.getBackgrounds().size();
        }
        if (c.getCompetences() != null) {
            c.getCompetences().size();
        }
        return c;
    }

    public Candidat update(Long id, Candidat candidatDetails) {
        Candidat candidat = getById(id);
        
        // Update only fields that are provided (not null)
        if (candidatDetails.getNom() != null) {
            candidat.setNom(candidatDetails.getNom());
        }
        if (candidatDetails.getPrenom() != null) {
            candidat.setPrenom(candidatDetails.getPrenom());
        }
        if (candidatDetails.getEmail() != null) {
            candidat.setEmail(candidatDetails.getEmail());
        }
        if (candidatDetails.getTelephone() != null) {
            candidat.setTelephone(candidatDetails.getTelephone());
        }
        if (candidatDetails.getDescription() != null) {
            candidat.setDescription(candidatDetails.getDescription());
        }
        if (candidatDetails.getCv() != null) {
            candidat.setCv(candidatDetails.getCv());
        }
        if (candidatDetails.getLienPortfolio() != null) {
            candidat.setLienPortfolio(candidatDetails.getLienPortfolio());
        }
        if (candidatDetails.getNiveauEtude() != null) {
            candidat.setNiveauEtude(candidatDetails.getNiveauEtude());
        }
        if (candidatDetails.getCompetences() != null && !candidatDetails.getCompetences().isEmpty()) {
            candidat.setCompetences(candidatDetails.getCompetences());
        }
        if (candidatDetails.getBackgroundExpertise() != null) {
            candidat.setBackgroundExpertise(candidatDetails.getBackgroundExpertise());
        }
        if (candidatDetails.getPassionAndGoals() != null) {
            candidat.setPassionAndGoals(candidatDetails.getPassionAndGoals());
        }
        
        // Update localisation only if provided
        if (candidatDetails.getLocalisation() != null) {
            candidat.setLocalisation(candidatDetails.getLocalisation());
        }
        
        // Clear education and background lists since they are stored as concatenated strings
        // in niveauEtude and backgroundExpertise fields respectively
        if (candidat.getEducations() != null) {
            candidat.getEducations().clear();
        }
        if (candidat.getBackgrounds() != null) {
            candidat.getBackgrounds().clear();
        }
        
        return repository.save(candidat);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Candidat findByEmail(String email) {
        Candidat c = repository.findByEmail(email).orElseThrow(() ->
            new ResourceNotFoundException("Candidat not found with email: " + email));
        if (c.getEducations() != null) {
            c.getEducations().size();
        }
        if (c.getBackgrounds() != null) {
            c.getBackgrounds().size();
        }
        if (c.getCompetences() != null) {
            c.getCompetences().size();
        }
        return c;
    }
}

