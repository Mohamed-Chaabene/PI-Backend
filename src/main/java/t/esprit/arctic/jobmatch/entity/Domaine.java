package t.esprit.arctic.jobmatch.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "domaines")
public class Domaine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String nom; // IT, Santé, Finance, etc.

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private boolean actif = true;

    @OneToMany(mappedBy = "domaine", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Question> questions;

    @OneToMany(mappedBy = "domaine", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Entretien> entretiens;
}

