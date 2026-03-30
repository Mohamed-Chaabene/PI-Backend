package t.esprit.arctic.jobmatch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.Date;

@Data
@AllArgsConstructor
public class ParticipationResponse {
    private Long id;
    private Date dateInscription;
    private String statut;
    private Long evenementId;
    private String titreEvenement;
}