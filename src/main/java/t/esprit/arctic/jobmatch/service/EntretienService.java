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
    private CandidatRepository candidatRepository;

    @Autowired
    private RecruteurRepository recruteurRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private ResultatRepository resultatRepository;

    public EntretienDTO createEntretien(EntretienCreateDTO dto, Long recruteurId) {
        // Validation métier
        validateEntretienData(dto, recruteurId);

        Recruteur recruteur = recruteurRepository.findById(recruteurId)
                .orElseThrow(() -> new RuntimeException("Recruteur non trouvé"));

        Entretien entretien = new Entretien();
        entretien.setTitre(dto.getTitre());
        entretien.setDateEntretien(dto.getDateEntretien());
        entretien.setCategorie(CategorieEntretien.valueOf(dto.getType() != null ? dto.getType() : 
                                                         (dto.getCategorie() != null ? dto.getCategorie() : "TECHNIQUE")));
        entretien.setRecruteur(recruteur);
        entretien.setDescription(dto.getDescription());
        entretien.setPhoto(dto.getPhoto());

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

    public EntretienDTO updateEntretien(Long id, EntretienCreateDTO dto, Long recruteurId) {
        Entretien entretien = entretienRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entretien non trouvé"));

        // Vérifier que le recruteur est propriétaire de l'entretien
        if (!entretien.getRecruteur().getId().equals(recruteurId)) {
            throw new RuntimeException("Accès non autorisé à cet entretien");
        }

        // Vérifier que l'entretien n'est pas déjà terminé
        if (entretien.isCompleted()) {
            throw new RuntimeException("Impossible de modifier un entretien terminé");
        }

        // Mettre à jour les champs
        if (dto.getTitre() != null && !dto.getTitre().trim().isEmpty()) {
            entretien.setTitre(dto.getTitre());
        }

        if (dto.getDateEntretien() != null) {
            // Vérifier que la date est dans le futur
            if (dto.getDateEntretien().isBefore(java.time.LocalDateTime.now())) {
                throw new IllegalArgumentException("La date de l'entretien doit être dans le futur");
            }
            entretien.setDateEntretien(dto.getDateEntretien());
        }

        if (dto.getDescription() != null) {
            entretien.setDescription(dto.getDescription());
        }

        if (dto.getPhoto() != null) {
            entretien.setPhoto(dto.getPhoto());
        }

        // Gestion du candidat (seulement si ce n'est pas un TEST)
        boolean isTestType = "TEST".equalsIgnoreCase(dto.getType()) || "TEST".equalsIgnoreCase(dto.getCategorie());
        if (!isTestType) {
            if (dto.getCandidatId() != null) {
                Candidat candidat = candidatRepository.findById(dto.getCandidatId())
                        .orElseThrow(() -> new IllegalArgumentException("Candidat non trouvé : " + dto.getCandidatId()));
                entretien.setCandidat(candidat);
            }
        } else {
            entretien.setCandidat(null);
        }

        Entretien saved = entretienRepository.save(entretien);
        return convertToDTO(saved);
    }

    public void deleteEntretien(Long id, Long recruteurId) {
        Entretien entretien = entretienRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entretien non trouvé"));

        // Vérifier que le recruteur est propriétaire de l'entretien
        if (!entretien.getRecruteur().getId().equals(recruteurId)) {
            throw new RuntimeException("Accès non autorisé à cet entretien");
        }

        // Vérifier que l'entretien n'est pas déjà terminé
        if (entretien.isCompleted()) {
            throw new RuntimeException("Impossible de supprimer un entretien terminé");
        }

        entretienRepository.delete(entretien);
    }

    private EntretienDTO convertToDTO(Entretien entretien) {
        EntretienDTO dto = new EntretienDTO();
        dto.setId(entretien.getId());
        dto.setTitre(entretien.getTitre());
        dto.setDateEntretien(entretien.getDateEntretien());
        dto.setType(entretien.getCategorie().toString());
        dto.setDescription(entretien.getDescription());
        dto.setPhoto(entretien.getPhoto());
        dto.setCompleted(entretien.isCompleted());
        dto.setCreatedAt(entretien.getCreatedAt());
        dto.setRecruteurId(entretien.getRecruteur().getId());
        dto.setCandidatId(entretien.getCandidat() != null ? entretien.getCandidat().getId() : null);
        // Les questions peuvent être ajoutées ici si besoin
        return dto;
    }

    private void validateEntretienData(EntretienCreateDTO dto, Long recruteurId) {
        // Vérifier que le recruteur existe
        Recruteur recruteur = recruteurRepository.findById(recruteurId)
                .orElseThrow(() -> new RuntimeException("Recruteur non trouvé"));

        // Vérifier que la date est dans le futur (au moins 1 heure)
        if (dto.getDateEntretien() != null &&
            dto.getDateEntretien().isBefore(java.time.LocalDateTime.now().plusHours(1))) {
            throw new IllegalArgumentException("La date de l'entretien doit être au moins 1 heure dans le futur");
        }

        // Vérifier la longueur du titre
        if (dto.getTitre() != null && dto.getTitre().length() > 255) {
            throw new IllegalArgumentException("Le titre ne peut pas dépasser 255 caractères");
        }

        // Vérifier la longueur de la description
        if (dto.getDescription() != null && dto.getDescription().length() > 1000) {
            throw new IllegalArgumentException("La description ne peut pas dépasser 1000 caractères");
        }

        // Validation spécifique selon le type
        String type = dto.getType() != null ? dto.getType() : dto.getCategorie();
        if (type != null && !"TEST".equalsIgnoreCase(type)) {
            if (dto.getCandidatId() == null) {
                throw new IllegalArgumentException("Un candidat doit être sélectionné pour un entretien de type " + type);
            }
            // Vérifier que le candidat existe
            candidatRepository.findById(dto.getCandidatId())
                    .orElseThrow(() -> new IllegalArgumentException("Candidat non trouvé : " + dto.getCandidatId()));
        }
    }
}
