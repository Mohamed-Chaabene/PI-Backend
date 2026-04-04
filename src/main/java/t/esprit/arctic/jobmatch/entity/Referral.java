package t.esprit.arctic.jobmatch.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
public class Referral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code; // e.g. 86PTHJK5

    @ManyToOne
    @JoinColumn(name = "referrer_id")
    private Utilisateur referrer;

    private int unitsEarned = 0;

    private LocalDateTime createdAt = LocalDateTime.now();
}