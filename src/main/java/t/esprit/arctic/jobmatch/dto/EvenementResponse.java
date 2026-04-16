package t.esprit.arctic.jobmatch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
<<<<<<< HEAD
=======
import java.time.LocalDateTime;
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EvenementResponse {
    private Long id;
    private String titre;
<<<<<<< HEAD
    private LocalDate date;
=======
    private LocalDateTime dateHeure;
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
    private String lieu;
    private String type;
    private Long organisateurId;
    private String nomOrganisateur;
<<<<<<< HEAD
=======
    private boolean chatOuvert;
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
}