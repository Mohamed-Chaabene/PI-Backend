package t.esprit.arctic.jobmatch.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Evenement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;

    @Temporal(TemporalType.DATE)
    private Date date;

    private String lieu;

    private String type;

    // Relation OneToMany avec Participation
    @OneToMany(mappedBy = "evenement", cascade = CascadeType.ALL)
    private List<Participation> participations;

    //  Relation avec OrganisateurEvenement
    @ManyToOne
    @JoinColumn(name = "organisateur_id")
    private OrganisateurEvenement organisateur;

}