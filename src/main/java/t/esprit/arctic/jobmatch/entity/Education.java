package t.esprit.arctic.jobmatch.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Education {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "candidat_id", nullable = false)
    private Candidat candidat;

    private String niveauEtude;

    private String domain;

    private String institution;

    private String lienPortfolio;

    private String startDate;

    private String endDate;

    @Column(columnDefinition = "LONGTEXT")
    private String description;
}
