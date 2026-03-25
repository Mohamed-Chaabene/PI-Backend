package t.esprit.arctic.jobmatch.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import t.esprit.arctic.jobmatch.dto.DomaineDTO;
import t.esprit.arctic.jobmatch.entity.Domaine;
import t.esprit.arctic.jobmatch.repository.DomaineRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DomaineService {

    @Autowired
    private DomaineRepository domaineRepository;

    public DomaineDTO createDomaine(DomaineDTO domaineDTO) {
        Domaine domaine = new Domaine();
        domaine.setNom(domaineDTO.getNom());
        domaine.setDescription(domaineDTO.getDescription());
        domaine.setActif(true);
        
        Domaine saved = domaineRepository.save(domaine);
        return convertToDTO(saved);
    }

    public List<DomaineDTO> getAllDomaines() {
        return domaineRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<DomaineDTO> getActiveDomaines() {
        return domaineRepository.findByActifTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public DomaineDTO getDomaine(Long id) {
        return domaineRepository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }

    public DomaineDTO updateDomaine(Long id, DomaineDTO domaineDTO) {
        return domaineRepository.findById(id).map(domaine -> {
            domaine.setNom(domaineDTO.getNom());
            domaine.setDescription(domaineDTO.getDescription());
            domaine.setActif(domaineDTO.isActif());
            Domaine updated = domaineRepository.save(domaine);
            return convertToDTO(updated);
        }).orElse(null);
    }

    public void deleteDomaine(Long id) {
        domaineRepository.deleteById(id);
    }

    private DomaineDTO convertToDTO(Domaine domaine) {
        return new DomaineDTO(
            domaine.getId(),
            domaine.getNom(),
            domaine.getDescription(),
            domaine.isActif()
        );
    }
}

