package t.esprit.arctic.jobmatch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.Date;

@Data
@AllArgsConstructor
public class FeedbackResponse {
    private Long id;
    private String commentaire;
    private int note;
    private Date date;
    private Long participationId;
}