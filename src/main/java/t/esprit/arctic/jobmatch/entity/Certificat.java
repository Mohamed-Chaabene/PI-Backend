package t.esprit.arctic.jobmatch.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.Date;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Certificat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le titre est requis")
    @Size(min = 3, max = 150)
    private String titre;

    @Temporal(TemporalType.DATE)
    private Date dateObtention;

    @OneToOne
    @JoinColumn(name = "inscription_id", nullable = false)
    private InscriptionFormation inscription;

    /** Optionnel — lié à un parcours si le certificat est généré via le quiz Expert */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parcours_id")
    private ParcoursFormation parcours;
}