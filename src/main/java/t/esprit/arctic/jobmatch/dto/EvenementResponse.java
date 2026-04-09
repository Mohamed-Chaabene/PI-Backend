package t.esprit.arctic.jobmatch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EvenementResponse {
    private Long id;
    private String titre;
    private LocalDate date;
    private String lieu;
    private String type;
    private Long organisateurId;
    private String nomOrganisateur;
}