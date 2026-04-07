package t.esprit.arctic.jobmatch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.Date;

@Data
@AllArgsConstructor
public class FeedbackEventResponse {
    private Long id;
    private String commentaire;
    private int note;
    private Date date;
    private Long participationId;
    private String titreEvenement;
}