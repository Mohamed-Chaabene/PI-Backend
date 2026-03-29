package t.esprit.arctic.jobmatch.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Formation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le titre est requis")
    @Size(min = 3, max = 150)
    private String titre;

    @NotBlank(message = "La catégorie est requise")
    private String categorie;

    @NotBlank(message = "La plateforme est requise")
    private String plateforme;

    @NotBlank(message = "Le statut est requis")
    @Pattern(regexp = "(Disponible|Archivée|Bientôt)", message = "Statut invalide")
    private String statut;

    @NotBlank(message = "La durée est requise")
    private String duree;

    @NotBlank(message = "Le niveau est requis")
    @Pattern(regexp = "(Débutant|Intermédiaire|Avancé|Expert)", message = "Niveau invalide")
    private String niveau;

    // ✅ On expose les compétences sans leurs propres relations
    @ManyToMany
    @JoinTable(
            name = "formation_competence",
            joinColumns = @JoinColumn(name = "formation_id"),
            inverseJoinColumns = @JoinColumn(name = "competence_id")
    )
    @JsonIgnoreProperties({"formations"})
    private List<Competence> competences;

    // ✅ On cache complètement la liste des inscriptions depuis Formation
    // (accessible via /api/inscriptions/formation/{id})
    @OneToMany(mappedBy = "formation", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<InscriptionFormation> inscriptions;
}