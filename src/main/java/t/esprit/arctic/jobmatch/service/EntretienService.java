package t.esprit.arctic.jobmatch.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import t.esprit.arctic.jobmatch.dto.EntretienDTO;
import t.esprit.arctic.jobmatch.dto.EntretienCreateDTO;
import t.esprit.arctic.jobmatch.dto.EntretienTestPublicDto;
import t.esprit.arctic.jobmatch.entity.*;
import t.esprit.arctic.jobmatch.repository.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class EntretienService {

    @Autowired
    private EntretienRepository entretienRepository;

    @Autowired
    private CandidatRepository candidatRepository;

    @Autowired
    private RecruteurRepository recruteurRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Transactional
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
        if (isTestType) {
            entretien.setSeuilReussite(null);
        } else {
            entretien.setSeuilReussite(dto.getSeuilReussite() != null ? dto.getSeuilReussite() : 70);
        }

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

        if (dto.getDomaine() == null || dto.getDomaine().trim().isEmpty()) {
            throw new IllegalArgumentException("Le domaine de l'entretien est obligatoire");
        }
        entretien.setDomaine(DomaineType.fromString(dto.getDomaine()));
        entretien.setCompleted(false);

        Entretien saved = entretienRepository.save(entretien);
        return convertToDTO(saved);
    }

    public List<EntretienTestPublicDto> getPublicTestEntretiens() {
        return entretienRepository.findByCategorieAndCompleted(CategorieEntretien.TEST, false).stream()
                .sorted(Comparator.comparing(Entretien::getDateEntretien, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(this::toPublicTestDto)
                .collect(Collectors.toList());
    }

    private EntretienTestPublicDto toPublicTestDto(Entretien e) {
        EntretienTestPublicDto d = new EntretienTestPublicDto();
        d.setId(e.getId());
        d.setTitre(e.getTitre());
        d.setDescription(e.getDescription());
        if (e.getDomaine() != null) {
            d.setDomaine(e.getDomaine().name());
            d.setDomaineLabel(e.getDomaine().getLabel());
        }
        d.setDateEntretien(e.getDateEntretien());
        d.setPhoto(e.getPhoto());
        return d;
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
        // Return empty list when candidate id is unknown instead of throwing 500 upstream.
        return entretienRepository.findByCandidatId(candidatId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public EntretienDTO getEntretien(Long id) {
        return entretienRepository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }

    @Transactional
    public void markAsCompleted(Long id) {
        entretienRepository.findById(id).ifPresent(entretien -> {
            entretien.setCompleted(true);
            entretienRepository.save(entretien);
        });
    }

    @Transactional
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

        boolean isTestType = "TEST".equalsIgnoreCase(dto.getType()) || "TEST".equalsIgnoreCase(dto.getCategorie());
        if (isTestType) {
            entretien.setSeuilReussite(null);
        } else if (dto.getSeuilReussite() != null) {
            entretien.setSeuilReussite(dto.getSeuilReussite());
        }

        if (dto.getDomaine() != null && !dto.getDomaine().trim().isEmpty()) {
            entretien.setDomaine(DomaineType.fromString(dto.getDomaine()));
        }

        // Gestion du candidat (seulement si ce n'est pas un TEST)
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

    @Transactional
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

    @Transactional
    public EntretienDTO updateScore(Long entretienId, Double score) {
        Entretien entretien = entretienRepository.findById(entretienId)
                .orElseThrow(() -> new RuntimeException("Entretien non trouvé"));

        if (score < 0 || score > 100) {
            throw new IllegalArgumentException("Le score doit être entre 0 et 100");
        }

        entretien.setScore(score);
        Integer seuil = entretien.getSeuilReussite();
        if (seuil == null) {
            entretien.setDecision(String.format(java.util.Locale.FRANCE, "Score : %.0f %% (test général)", score));
        } else {
            entretien.setDecision(score >= seuil ? "accepté" : "refusé");
        }
        entretien.setEvaluatedAt(LocalDateTime.now());

        Entretien saved = entretienRepository.save(entretien);
        return convertToDTO(saved);
    }

    private EntretienDTO convertToDTO(Entretien entretien) {
        EntretienDTO dto = new EntretienDTO();
        dto.setId(entretien.getId());
        dto.setTitre(entretien.getTitre());
        dto.setDateEntretien(entretien.getDateEntretien());
        dto.setType(entretien.getCategorie().toString());
        dto.setDescription(entretien.getDescription());
        dto.setPhoto(entretien.getPhoto());
        dto.setDomaine(entretien.getDomaine() != null ? entretien.getDomaine().name() : null);
        dto.setCompleted(entretien.isCompleted());
        dto.setSeuilReussite(entretien.getSeuilReussite());
        dto.setCreatedAt(entretien.getCreatedAt());
        dto.setRecruteurId(entretien.getRecruteur().getId());
        dto.setCandidatId(entretien.getCandidat() != null ? entretien.getCandidat().getId() : null);
        dto.setScore(entretien.getScore());
        dto.setTotalQuestions(entretien.getTotalQuestions());
        dto.setBonnesReponses(entretien.getBonnesReponses());
        dto.setDecision(entretien.getDecision());
        dto.setCommentaire(entretien.getCommentaire());
        dto.setEvaluatedAt(entretien.getEvaluatedAt());
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
            if (dto.getSeuilReussite() == null) {
                throw new IllegalArgumentException("Le seuil de réussite est obligatoire pour ce type d'entretien");
            }
        }


    }


    }
