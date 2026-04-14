package t.esprit.arctic.jobmatch.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

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
    private String prenom;
    private String titre;
    private String email;
    private String telephone;
    private String adresse;

    @Column(columnDefinition = "TEXT")
    private String profil;

    @Column(columnDefinition = "TEXT")
    private String competences; // JSON string

    @Column(columnDefinition = "TEXT")
    private String langues; // JSON string

    @Column(name = "centres_interet", columnDefinition = "TEXT")
    private String centresInteret; // JSON string

    @Column(columnDefinition = "TEXT")
    private String experiences; // JSON string

    @Column(columnDefinition = "TEXT")
    private String formations; // JSON string

    @Column(name = "photo_name")
    private String photoName;

    @Column(name = "photo_data", columnDefinition = "LONGTEXT")
    private String photoData; // Base64

    @Enumerated(EnumType.STRING)
    private TypeDocument type;

    @Column(columnDefinition = "LONGTEXT")
    private String contenu;

    private String template;

    @Column(name = "compatibleats")
    private Boolean compatibleATS;

    @Column(name = "ajouter_photo")
    private Boolean ajouterPhoto = false;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
    }

    @JsonIgnore
    @OneToOne(mappedBy = "document")
    private Candidature candidature;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "candidat_id")
    private Candidat candidat;
}