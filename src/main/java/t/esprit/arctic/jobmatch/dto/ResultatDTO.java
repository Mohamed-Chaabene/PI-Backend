package t.esprit.arctic.jobmatch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultatDTO {
    private Long id;
    private Long entretienId;
    private double score;
    private int totalQuestions;
    private int bonnesReponses;
    private String decision;
    private String commentaire;
    private LocalDateTime evaluatedAt;
}

