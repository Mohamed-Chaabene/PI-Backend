package t.esprit.arctic.jobmatch.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "reponses_candidat")
public class ReponseCandidat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "choix_id", nullable = true)
    private Choix choixSelectionne;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidat_id", nullable = false)
    private Candidat candidat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entretien_id", nullable = false)
    private Entretien entretien;

    @Column(nullable = false)
    private boolean correcte;

    @Column(name = "answered_at", nullable = false, updatable = false)
    private LocalDateTime answeredAt;

    @PrePersist
    protected void onCreate() {
        answeredAt = LocalDateTime.now();
    }

    // Getters explicites
    public Long getId() {
        return id;
    }

    public Question getQuestion() {
        return question;
    }

    public Choix getChoixSelectionne() {
        return choixSelectionne;
    }

    public Candidat getCandidat() {
        return candidat;
    }

    public Entretien getEntretien() {
        return entretien;
    }

    public boolean isCorrecte() {
        return correcte;
    }

    public LocalDateTime getAnsweredAt() {
        return answeredAt;
    }

    // Setters explicites
    public void setQuestion(Question question) {
        this.question = question;
    }

    public void setChoixSelectionne(Choix choixSelectionne) {
        this.choixSelectionne = choixSelectionne;
    }

    public void setCandidat(Candidat candidat) {
        this.candidat = candidat;
    }

    public void setEntretien(Entretien entretien) {
        this.entretien = entretien;
    }

    public void setCorrecte(boolean correcte) {
        this.correcte = correcte;
    }
}