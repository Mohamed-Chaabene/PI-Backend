package t.esprit.arctic.jobmatch.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;

    @Enumerated(EnumType.STRING)
    private TypeDocument type;

    @Column(columnDefinition = "LONGTEXT")
    private String contenu;

    private String template;

    @Column(name = "compatibleats")
    private Boolean compatibleATS;

    @Column(name = "ajouter_photo")
    private Boolean ajouterPhoto = false;

    @JsonIgnore
    @OneToOne(mappedBy = "document")
    private Candidature candidature;
}