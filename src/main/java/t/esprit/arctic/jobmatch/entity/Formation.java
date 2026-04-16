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
<<<<<<< HEAD

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
=======
    private String categorie;
    private String plateforme;
    private String statut;
    private String duree;
    private String niveau;

    @Column(length = 500)
    private String lienExterne;

    @Column(length = 100)
    private String playlistId;

    @Column(length = 100)
    private String youtubeId;

    private Boolean hasEditor;

    @Column(length = 500)
    private String stackBlitzUrl;

    @Column(length = 500)
    private String writtenUrl;
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0

    @Column(length = 1000)
    private String description;

    @Column(length = 500)
    private String imageUrl;

<<<<<<< HEAD
    // ── Relations ─────────────────────────────────────────────────────────────

    @ManyToMany(fetch = FetchType.EAGER)
=======
    @ManyToMany(fetch = FetchType.LAZY)
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
    @JoinTable(
            name = "formation_competence",
            joinColumns = @JoinColumn(name = "formation_id"),
            inverseJoinColumns = @JoinColumn(name = "competence_id")
    )
<<<<<<< HEAD
    @JsonIgnoreProperties({"formations"})
    private List<Competence> competences;

    @OneToMany(mappedBy = "formation", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<InscriptionFormation> inscriptions;
=======
    @JsonIgnore
    private List<Competence> competences;

    @OneToMany(mappedBy = "formation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<InscriptionFormation> inscriptions;

    @Column(name = "score_popularite")
    private Double scorePopularite = 0.0;

    @Column(name = "badge", length = 50)
    private String badge;

    @Column(name = "total_inscrits")
    private Integer totalInscrits = 0;

    @Column(name = "note_moyenne")
    private Double noteMoyenne = 0.0;

    @Column(name = "taux_completion")
    private Double tauxCompletion = 0.0;
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
}