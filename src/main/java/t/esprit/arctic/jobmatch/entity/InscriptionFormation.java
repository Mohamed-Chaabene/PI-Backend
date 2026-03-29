package t.esprit.arctic.jobmatch.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class InscriptionFormation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Temporal(TemporalType.DATE)
    private Date dateInscription;

    @NotBlank(message = "Le statut est requis")
    @Pattern(regexp = "(EnCours|Terminé|Abandonné)", message = "Statut invalide")
    private String statut;

    @Min(value = 0, message = "La progression ne peut pas être négative")
    @Max(value = 100, message = "La progression ne peut pas dépasser 100")
    private Double progression;

    // ✅ On expose la formation SANS sa liste d'inscriptions (évite la boucle)
    @ManyToOne
    @JoinColumn(name = "formation_id", nullable = false)
    @JsonIgnoreProperties({"inscriptions", "competences"})
    private Formation formation;

    // ✅ On expose le candidat SANS ses données sensibles ni ses relations circulaires
    @ManyToOne
    @JoinColumn(name = "candidat_id", nullable = false)
    @JsonIgnoreProperties({
            "motDePasse", "inscriptions", "candidatures",
            "competences", "cv", "localisation"
    })
    private Candidat candidat;

    // ✅ Déjà correct — on cache le certificat pour éviter la boucle certificat→inscription
    @OneToOne(mappedBy = "inscription", cascade = CascadeType.ALL)
    @JsonIgnore
    private Certificat certificat;
}