package t.esprit.arctic.jobmatch.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "jobs")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    private JobType type;

    private BigDecimal budget;

    private Integer estimatedDays;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Utilisateur client;

    @Enumerated(EnumType.STRING)
    private JobStatus status = JobStatus.OPEN;

    private LocalDateTime createdAt = LocalDateTime.now();
}