package t.esprit.arctic.jobmatch.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import t.esprit.arctic.jobmatch.entity.OrganisateurEvenement;
import t.esprit.arctic.jobmatch.exception.ResourceNotFoundException;
import t.esprit.arctic.jobmatch.repository.OrganisateurEvenementRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrganisateurService {

    private final OrganisateurEvenementRepository organisateurRepository;

    public List<OrganisateurEvenement> getAll() {
        return organisateurRepository.findAll();
    }

    public void delete(Long id) {
        organisateurRepository.deleteById(id);
    }

    public OrganisateurEvenement toggleStatut(Long id) {
        OrganisateurEvenement org = organisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organisateur non trouvé"));
        org.setActif(!org.isActif());
        return organisateurRepository.save(org);
    }
}