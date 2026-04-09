package t.esprit.arctic.jobmatch.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EvenementRequest {

    @NotBlank(message = "Le titre est obligatoire")
    @Size(min = 3, max = 100, message = "Le titre doit contenir entre 3 et 100 caractères")
    private String titre;

    // ✅ LocalDate au lieu de Date — compatible @FutureOrPresent et avec le builder Evenement
    @NotNull(message = "La date est obligatoire")
    @FutureOrPresent(message = "La date ne peut pas être dans le passé")
    private LocalDate date;

    @NotBlank(message = "Le lieu est obligatoire")
    @Size(min = 2, message = "Le lieu doit contenir au moins 2 caractères")
    private String lieu;

    @NotBlank(message = "Le type est obligatoire")
    @Pattern(
            regexp = "JOB_FAIR|WORKSHOP|CONFERENCE|NETWORKING",
            message = "Type invalide : JOB_FAIR, WORKSHOP, CONFERENCE ou NETWORKING"
    )
    private String type;

    @NotNull(message = "L'organisateur est obligatoire")
    private Long organisateurId;
}