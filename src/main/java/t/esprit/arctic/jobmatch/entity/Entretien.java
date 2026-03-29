package t.esprit.arctic.jobmatch.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "entretiens")
public class Entretien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime dateEntretien;

    @Column(length = 255)
    private String titre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategorieEntretien categorie;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private DomaineType domaine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidat_id", nullable = true)
    private Candidat candidat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruteur_id", nullable = false)
    private Recruteur recruteur;

    @OneToMany(mappedBy = "entretien", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Question> questions;

    @OneToOne(mappedBy = "entretien", cascade = CascadeType.ALL, orphanRemoval = true)
    private Resultat resultat;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 500, nullable = true)
    private String photo;

    private boolean completed = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Getters explicites
    public Long getId() {
        return id;
    }

    public LocalDateTime getDateEntretien() {
        return dateEntretien;
    }

    public String getTitre() {
        return titre;
    }

    public CategorieEntretien getCategorie() {
        return categorie;
    }

    public DomaineType getDomaine() {
        return domaine;
    }

    public Candidat getCandidat() {
        return candidat;
    }

    public Recruteur getRecruteur() {
        return recruteur;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public Resultat getResultat() {
        return resultat;
    }

    public String getDescription() {
        return description;
    }

    public String getPhoto() {
        return photo;
    }

    public boolean isCompleted() {
        return completed;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Setters explicites
    public void setTitre(String titre) {
        this.titre = titre;
    }

    public void setDomaine(DomaineType domaine) {
        this.domaine = domaine;
    }

    public void setCandidat(Candidat candidat) {
        this.candidat = candidat;
    }

    public void setRecruteur(Recruteur recruteur) {
        this.recruteur = recruteur;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
