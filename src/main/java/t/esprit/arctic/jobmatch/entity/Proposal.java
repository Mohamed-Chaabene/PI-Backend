package t.esprit.arctic.jobmatch.entity;

import jakarta.persistence.*;
import lombok.*;
import t.esprit.arctic.jobmatch.entity.ProposalStatus;
import t.esprit.arctic.jobmatch.entity.Utilisateur;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Proposal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "job_id")
    private t.esprit.arctic.jobmatch.entity.Job job;

    @ManyToOne
    @JoinColumn(name = "freelancer_id")
    private Utilisateur freelancer;

    private String coverLetter;
    private BigDecimal proposedPrice;
    private Integer estimatedDays;

    @Enumerated(EnumType.STRING)
    private ProposalStatus status;
    private LocalDateTime submittedAt = LocalDateTime.now();
}