package t.esprit.arctic.jobmatch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReponseDTO {
    private Long id;
    private Long questionId;
    private Long choixId;
    private Long candidatId;
    private Long entretienId;
    private boolean correcte;

    // Getters explicites
    public Long getQuestionId() {
        return questionId;
    }

    public Long getCandidatId() {
        return candidatId;
    }

    public Long getChoixId() {
        return choixId;
    }

    public Long getEntretienId() {
        return entretienId;
    }

    public boolean isCorrecte() {
        return correcte;
    }
}
