package t.esprit.arctic.jobmatch.entity;

import jakarta.persistence.Entity;
import lombok.*;

@Entity
@Getter
@Setter

public class Admin extends Utilisateur {

    private String niveauAcces;

}