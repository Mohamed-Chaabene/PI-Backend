package t.esprit.arctic.jobmatch.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import t.esprit.arctic.jobmatch.entity.Participation;

import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String commentaire;
    private int note; // ex: 1 à 5

    @Temporal(TemporalType.DATE)
    private Date date;

    // ✅ Lié à une participation
    @ManyToOne
    @JoinColumn(name = "participation_id")
    private Participation participation;
}