package t.esprit.arctic.jobmatch.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Min(value = 1, message = "La note doit être entre 1 et 5")
    @Max(value = 5, message = "La note doit être entre 1 et 5")
    @NotNull(message = "La note est requise")
    private Integer note;

    @NotBlank(message = "Le commentaire est requis")
    @Size(min = 5, max = 1000, message = "Le commentaire doit contenir entre 5 et 1000 caractères")
    private String commentaire;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreation;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateModification;

    @Temporal(TemporalType.TIMESTAMP)
    private Date date;

    @ManyToOne
    @JoinColumn(name = "formation_id", nullable = false)
    @JsonIgnoreProperties({"inscriptions", "competences"})
    private Formation formation;

    @ManyToOne
    @JoinColumn(name = "candidat_id", nullable = false)
    @JsonIgnoreProperties({"motDePasse", "inscriptions", "candidatures", "competences", "educations", "backgrounds"})
    private Candidat candidat;

    @ManyToOne
    @JoinColumn(name = "participation_id")
    private Participation participation;

    @PrePersist
    public void prePersist() {
        this.dateCreation    = new Date();
        this.dateModification = new Date();
    }

    @PreUpdate
    public void preUpdate() {
        this.dateModification = new Date();
    }
}