package t.esprit.arctic.jobmatch.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import t.esprit.arctic.jobmatch.entity.Participation;

import java.time.LocalDate;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String commentaire;
<<<<<<< HEAD
    private int note; // ex: 1 à 5
=======
    private int note;
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0

    @Temporal(TemporalType.DATE)
    private LocalDate date;

<<<<<<< HEAD
    // ✅ Lié à une participation
=======

>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
    @ManyToOne
    @JoinColumn(name = "participation_id")
    private Participation participation;
}