package t.esprit.arctic.jobmatch.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Entity
@Table(name = "candidatures")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Candidature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateEnvoi;

    private String statut;

    @Column(columnDefinition = "TEXT")
    private String lettreGeneree;

    private String nomComplet;
    private String email;
    private String telephone;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String formation;

    @Column(columnDefinition = "TEXT")
    private String experience;

    @Column(columnDefinition = "TEXT")
    private String competences;

    @Column(columnDefinition = "TEXT")
    private String lettreMotivation;

    private String dateDisponibilite;
    private String preavis;
    private Boolean acceptContact;
    private Boolean acceptRGPD;

    @ManyToOne
    @JoinColumn(name = "candidat_id")
    private Candidat candidat;

    @OneToOne
    @JoinColumn(name = "document_id")
    private Document document;

    @ManyToOne
    @JoinColumn(name = "offre_id")
    private OffreEmploi offreEmploi;

    @Column(name = "score_entretien")
    private Double scoreEntretien;

    @Column(name = "total_questions_entretien")
    private Integer totalQuestionsEntretien;

    @Column(name = "bonnes_reponses_entretien")
    private Integer bonnesReponsesEntretien;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_evaluation_entretien")
    private Date dateEvaluationEntretien;
}