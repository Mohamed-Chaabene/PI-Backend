package t.esprit.arctic.jobmatch.entity;

import jakarta.persistence.*;
        import lombok.*;
        import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class VideoProgression {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long inscriptionId;

    @Column(nullable = false)
    private Long candidatId;

    @Column(nullable = false)
    private Long formationId;

    @Column(nullable = false)
    private String videoId;      // YouTube video ID

    private boolean vuComplete;  // vidéo vue jusqu'à la fin
    private boolean quizReussi;  // quiz validé après la vidéo

    private LocalDateTime dateVue;
    private LocalDateTime dateQuiz;

    private int scoreQuiz;       // score obtenu au quiz (0-100)
}