package t.esprit.arctic.jobmatch.entity;

import jakarta.persistence.*;
import lombok.*;
import t.esprit.arctic.jobmatch.entity.Utilisateur;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class InvoiceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "freelancer_id")
    private Utilisateur freelancer;

    @ManyToOne
    @JoinColumn(name = "job_id")
    private t.esprit.arctic.jobmatch.entity.Job job;

    private BigDecimal amount;
    private BigDecimal tvaAmount;        // 19%
    private BigDecimal stampDuty = BigDecimal.valueOf(1); // 1 TND
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private InvoiceStatus status = InvoiceStatus.PENDING;

    private LocalDateTime requestedAt = LocalDateTime.now();
}