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
    private Long candidatId;
    private String nomCandidat;
    private String qrCode;
<<<<<<< HEAD
=======
    private boolean chatOuvert;
    private Boolean certificateGenerated;
    private String certificateUrl;
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
}