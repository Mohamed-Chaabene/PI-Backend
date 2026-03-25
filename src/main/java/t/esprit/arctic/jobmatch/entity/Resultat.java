package t.esprit.arctic.jobmatch.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "resultats")
public class Resultat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entretien_id", nullable = false, unique = true)
    private Entretien entretien;

    @Column(nullable = false)
    private double score;

    @Column(nullable = false)
    private int totalQuestions;

    @Column(nullable = false)
    private int bonnesReponses;

    @Column(nullable = false)
    private String decision;

    @Column(columnDefinition = "TEXT")
    private String commentaire;

    @Column(name = "evaluated_at")
    private LocalDateTime evaluatedAt;

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

    public Entretien getEntretien() {
        return entretien;
    }

    public double getScore() {
        return score;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public int getBonnesReponses() {
        return bonnesReponses;
    }

    public String getDecision() {
        return decision;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public LocalDateTime getEvaluatedAt() {
        return evaluatedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Setters explicites
    public void setEntretien(Entretien entretien) {
        this.entretien = entretien;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public void setBonnesReponses(int bonnesReponses) {
        this.bonnesReponses = bonnesReponses;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public void setEvaluatedAt(LocalDateTime evaluatedAt) {
        this.evaluatedAt = evaluatedAt;
    }
}