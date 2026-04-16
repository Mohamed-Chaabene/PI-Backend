package t.esprit.arctic.jobmatch.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@DiscriminatorValue("CLIENT_FREELANCE")
public class ClientFreelance extends Utilisateur {

    private String entreprise;
    private double budget;

    // Setters explicites pour Lombok
    public void setEntreprise(String entreprise) {
        this.entreprise = entreprise;
    }

    public void setBudget(double budget) {
        this.budget = budget;
    }
}
