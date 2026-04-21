package t.esprit.arctic.jobmatch.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class FeedbackMicro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer note;
    private String clarite;
    private String difficulte;

    @Column(length = 1000)
    private String commentaire;

    @Enumerated(EnumType.STRING)
    private NiveauOrdre niveau;

    @ManyToOne
    @JoinColumn(name = "inscription_id")
    private InscriptionParcours inscription;

    @ManyToOne
    @JoinColumn(name = "candidat_id")
    private Candidat candidat;

    @ManyToOne
    @JoinColumn(name = "parcours_id")
    private ParcoursFormation parcours;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = new Date();
    }
}
