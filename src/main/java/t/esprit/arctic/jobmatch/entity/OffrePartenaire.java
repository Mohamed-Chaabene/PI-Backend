package t.esprit.arctic.jobmatch.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OffrePartenaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;
    private String description;
    private Date datePublication;

    @Enumerated(EnumType.STRING)
    private TypeOffrePartenaire type;  // EMPLOI ou STAGE

    // Côté * → 1 (plusieurs offres appartiennent à 1 partenaire)
    @ManyToOne
    @JoinColumn(name = "partenaire_id")
    private Partenaire partenaire;
}