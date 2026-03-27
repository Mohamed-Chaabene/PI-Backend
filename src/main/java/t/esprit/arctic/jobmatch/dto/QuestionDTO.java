package t.esprit.arctic.jobmatch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDTO {
    private Long id;

    @NotBlank(message = "Le contenu de la question est obligatoire")
    @Size(min = 10, max = 1000, message = "Le contenu doit contenir entre 10 et 1000 caractères")
    private String contenu;

    @NotBlank(message = "Le type de question est obligatoire")
    @Pattern(regexp = "QCM|QCU|VRAI_FAUX",
             message = "Le type doit être : QCM, QCU ou VRAI_FAUX")
    private String type;

    @NotBlank(message = "Le niveau est obligatoire")
    @Pattern(regexp = "DEBUTANT|INTERMEDIAIRE|AVANCE|EXPERT",
             message = "Le niveau doit être : DEBUTANT, INTERMEDIAIRE, AVANCE ou EXPERT")
    private String niveau;

    @NotBlank(message = "Le domaine est obligatoire")
    private String domaine;

    private List<ChoixDTO> choix;

    @Min(value = 1, message = "L'ordre doit être supérieur à 0")
    private int ordre;

    private boolean actif = true;

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
