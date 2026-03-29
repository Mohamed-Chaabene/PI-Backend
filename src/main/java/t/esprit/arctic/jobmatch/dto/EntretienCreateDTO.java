package t.esprit.arctic.jobmatch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntretienCreateDTO {
    @Size(max = 255, message = "Le titre ne peut pas dépasser 255 caractères")
    private String titre;

    @NotNull(message = "La date de l'entretien est obligatoire")
    @Future(message = "La date de l'entretien doit être dans le futur")
    private LocalDateTime dateEntretien;

    @NotBlank(message = "Le type d'entretien est obligatoire")
    @Pattern(regexp = "TECHNIQUE|RH|MANAGERIAL|FINAL|PRESELECTION|TEST",
             message = "Le type doit être : TECHNIQUE, RH, MANAGERIAL, FINAL, PRESELECTION ou TEST")
    private String type;

    @NotBlank(message = "La description est obligatoire")
    @Size(min = 10, max = 1000, message = "La description doit contenir entre 10 et 1000 caractères")
    private String description;

    @Pattern(regexp = "TECHNIQUE|RH|MANAGERIAL|FINAL|PRESELECTION|TEST",
             message = "La catégorie doit être : TECHNIQUE, RH, MANAGERIAL, FINAL, PRESELECTION ou TEST")
    private String categorie;

    private Long candidatId;

    @Size(max = 500, message = "L'URL de la photo ne peut pas dépasser 500 caractères")
    private String photo;

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

    public String getPhoto() {
        return photo;
    }
}

