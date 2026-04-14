package t.esprit.arctic.jobmatch.entity;

import jakarta.persistence.*;
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

    private String titre;

    private String categorie;

    private String plateforme;

    private String statut;

    private String duree;

    private String niveau;

    // ── Champs contenus enrichis ──────────────────────────────────────────────

    @Column(length = 500)
    private String lienExterne;    // Lien Udemy/Coursera

    // ✅ Playlist YouTube (remplace youtubeId pour les nouvelles formations)
    @Column(length = 100)
    private String playlistId;     // ID playlist YouTube (ex: PLblA84xge2_z...)

    // Gardé pour compatibilité avec les anciennes formations (vidéo simple)
    @Column(length = 100)
    private String youtubeId;

    private Boolean hasEditor;     // Activer l'éditeur StackBlitz

    @Column(length = 500)
    private String stackBlitzUrl;  // Template éditeur

    @Column(length = 500)
    private String writtenUrl;     // Documentation écrite (W3Schools, MDN...)

    @Column(length = 1000)
    private String description;

    @Column(length = 500)
    private String imageUrl;

    // ── Relations ─────────────────────────────────────────────────────────────

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "formation_competence",
            joinColumns = @JoinColumn(name = "formation_id"),
            inverseJoinColumns = @JoinColumn(name = "competence_id")
    )
    @JsonIgnoreProperties({"formations"})
    private List<Competence> competences;

    @OneToMany(mappedBy = "formation", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<InscriptionFormation> inscriptions;
}