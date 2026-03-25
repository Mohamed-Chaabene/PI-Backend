package t.esprit.arctic.jobmatch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntretienDTO {
    private Long id;
    private String titre;
    private LocalDateTime dateEntretien;
    private String type;
    private Long recruteurId;
    private Long candidatId;
    private String description;
    private boolean completed;
    private LocalDateTime createdAt;
    private List<QuestionDTO> questions;
}
