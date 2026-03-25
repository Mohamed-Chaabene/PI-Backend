package t.esprit.arctic.jobmatch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDTO {
    private Long id;
    private String contenu;
    private String type;
    private String niveau;
    private String domaine;
    private List<ChoixDTO> choix;
    private int ordre;
    private boolean actif;

    // Getters explicites
    public Long getId() {
        return id;
    }

    public String getContenu() {
        return contenu;
    }

    public String getType() {
        return type;
    }

    public String getNiveau() {
        return niveau;
    }

    public String getDomaine() {
        return domaine;
    }

    public List<ChoixDTO> getChoix() {
        return choix;
    }

    public int getOrdre() {
        return ordre;
    }

    public boolean isActif() {
        return actif;
    }
}
