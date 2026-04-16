package t.esprit.arctic.jobmatch.freelance.dto;

import lombok.*;
import t.esprit.arctic.jobmatch.freelance.entity.Mission;
import t.esprit.arctic.jobmatch.freelance.entity.MissionStatut;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MissionResponseDTO {

    private Long id;
    private String titre;
    private String description;
    private Double budget;
    private List<String> competences;
    private MissionStatut statut;
    private String postedByNom;
    private LocalDateTime dateCreation;

    /**
     * Convert a Mission entity to a response DTO.
     * This avoids LazyInitializationException by extracting all needed data
     * while the Hibernate session is still open (inside @Transactional).
     */
    public static MissionResponseDTO fromEntity(Mission m) {
        return MissionResponseDTO.builder()
                .id(m.getId())
                .titre(m.getTitre())
                .description(m.getDescription())
                .budget(m.getBudget())
                .competences(m.getCompetences())
                .statut(m.getStatut())
                .postedByNom(m.getPubliePar() != null ? m.getPubliePar().getNom() : null)
                .dateCreation(m.getDateCreation())
                .build();
    }
}
