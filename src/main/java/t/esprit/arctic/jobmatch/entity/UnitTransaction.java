package t.esprit.arctic.jobmatch.entity.freelance;

import jakarta.persistence.*;
import lombok.*;
import t.esprit.arctic.jobmatch.entity.UnitTransactionType;
import t.esprit.arctic.jobmatch.entity.Utilisateur;

import java.time.LocalDateTime;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Table(name = "unit_transactions")
public class UnitTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Utilisateur user;

    private int amount;

    @Enumerated(EnumType.STRING)
    private UnitTransactionType type; // REWARD_SIGNUP, REWARD_DAILY, SPENT_APPLY, REFERRAL, etc.

    private String description;

    private LocalDateTime createdAt = LocalDateTime.now();
}