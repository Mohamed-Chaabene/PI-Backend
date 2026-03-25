package t.esprit.arctic.jobmatch.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@DiscriminatorValue("ORGANISATEUR")
public class OrganisateurEvenement extends Utilisateur {

    private String organisation;
    private String adresse;
    private String descriptionProjet;
}
