package t.esprit.arctic.jobmatch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntretienCreateDTO {
    private String titre;
    private LocalDateTime dateEntretien;
    private String type;
    private String description;
    private String categorie;
    private Long candidatId;

    // Getters explicites
    public String getTitre() {
        return titre;
    }

    public LocalDateTime getDateEntretien() {
        return dateEntretien;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public String getCategorie() {
        return categorie;
    }

    public Long getCandidatId() {
        return candidatId;
    }
}

