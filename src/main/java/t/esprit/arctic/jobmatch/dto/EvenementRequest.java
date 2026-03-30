package t.esprit.arctic.jobmatch.dto;

import lombok.Data;
import java.util.Date;

@Data
public class EvenementRequest {
    private String titre;
    private Date date;
    private String lieu;
    private String type;
    private Long organisateurId;
}