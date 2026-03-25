package t.esprit.arctic.jobmatch.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import t.esprit.arctic.jobmatch.dto.EntretienDTO;
import t.esprit.arctic.jobmatch.dto.EntretienCreateDTO;
import t.esprit.arctic.jobmatch.entity.*;
import t.esprit.arctic.jobmatch.repository.*;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EntretienService {

    @Autowired
    private EntretienRepository entretienRepository;

    @Autowired
    private DomaineRepository domaineRepository;

    @Autowired
    private CandidatRepository candidatRepository;

    @Autowired
    private RecruteurRepository recruteurRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private ResultatRepository resultatRepository;

    public EntretienDTO createEntretien(EntretienCreateDTO dto, Long recruteurId) {
        Recruteur recruteur = recruteurRepository.findById(recruteurId)
                .orElseThrow(() -> new RuntimeException("Recruteur non trouvé"));

        Entretien entretien = new Entretien();
        entretien.setTitre(dto.getTitre());
        entretien.setDateEntretien(dto.getDateEntretien());
        entretien.setCategorie(CategorieEntretien.valueOf(dto.getType() != null ? dto.getType() : 
                                                         (dto.getCategorie() != null ? dto.getCategorie() : "TECHNIQUE")));
        entretien.setRecruteur(recruteur);
        entretien.setDescription(dto.getDescription());

        boolean isTestType = "TEST".equalsIgnoreCase(dto.getType()) || "TEST".equalsIgnoreCase(dto.getCategorie());

        if (!isTestType) {
            if (dto.getCandidatId() == null) {
                throw new IllegalArgumentException("Pour un entretien non TEST, un candidat doit être sélectionné.");
            }
            Candidat candidat = candidatRepository.findById(dto.getCandidatId())
                    .orElseThrow(() -> new IllegalArgumentException("Candidat non trouvé : " + dto.getCandidatId()));
            entretien.setCandidat(candidat);
        } else {
            entretien.setCandidat(null);
        }

        entretien.setDomaine(null);   // Pas de domaine à la création
        entretien.setCompleted(false);

        Entretien saved = entretienRepository.save(entretien);
        return convertToDTO(saved);
    }

    public List<EntretienDTO> getAllEntretiens() {
        return entretienRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<EntretienDTO> getEntretiensByRecruteur(Long recruteurId) {
        Recruteur recruteur = recruteurRepository.findById(recruteurId)
                .orElseThrow(() -> new RuntimeException("Recruteur non trouvé"));
        return entretienRepository.findByRecruteur(recruteur).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<EntretienDTO> getEntretiensByCandidat(Long candidatId) {
        Candidat candidat = candidatRepository.findById(candidatId)
                .orElseThrow(() -> new RuntimeException("Candidat non trouvé"));
        return entretienRepository.findByCandidat(candidat).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public EntretienDTO getEntretien(Long id) {
        return entretienRepository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }

    public void markAsCompleted(Long id) {
        entretienRepository.findById(id).ifPresent(entretien -> {
            entretien.setCompleted(true);
            entretienRepository.save(entretien);
        });
    }

    private EntretienDTO convertToDTO(Entretien entretien) {
        EntretienDTO dto = new EntretienDTO();
        dto.setId(entretien.getId());
        dto.setTitre(entretien.getTitre());
        dto.setDateEntretien(entretien.getDateEntretien());
        dto.setType(entretien.getCategorie().toString());
        dto.setDescription(entretien.getDescription());
        dto.setCompleted(entretien.isCompleted());
        dto.setCreatedAt(entretien.getCreatedAt());
        dto.setRecruteurId(entretien.getRecruteur().getId());
        dto.setCandidatId(entretien.getCandidat() != null ? entretien.getCandidat().getId() : null);
        // Les questions peuvent être ajoutées ici si besoin
        return dto;
    }
}
