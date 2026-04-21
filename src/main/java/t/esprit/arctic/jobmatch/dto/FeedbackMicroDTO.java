package t.esprit.arctic.jobmatch.dto;

import lombok.*;
import t.esprit.arctic.jobmatch.entity.NiveauOrdre;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackMicroDTO {
    private Long inscriptionId;
    private Long parcoursId;
    private Long candidatId;
    private Integer note;
    private String clarite;
    private String difficulte;
    private String commentaire;
    private NiveauOrdre niveau;
}
