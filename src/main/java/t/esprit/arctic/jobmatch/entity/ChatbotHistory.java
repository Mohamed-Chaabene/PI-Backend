package t.esprit.arctic.jobmatch.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class ChatbotHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long candidatId;

    @Column(nullable = false)
    private Long formationId;

    @Column(columnDefinition = "TEXT")
<<<<<<< HEAD
    private String historiqueJson; // Stocke la liste JSON des messages de la discussion

    @Column(nullable = true)
    private String sessionId; // UUID de la session (nullable for backward compatibility temporaire)

    @Column(nullable = true)
    private String sessionTitle; // Titre de la conversation généré

    @Column(nullable = true)
    private java.time.LocalDateTime createdAt; // Date de création de la session
=======
    private String historiqueJson;

    @Column(nullable = true)
    private String sessionId;

    @Column(nullable = true)
    private String sessionTitle;

    @Column(nullable = true)
    private java.time.LocalDateTime createdAt;
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0

}
