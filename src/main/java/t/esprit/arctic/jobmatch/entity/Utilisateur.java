package t.esprit.arctic.jobmatch.entity;

<<<<<<< HEAD
=======
import com.fasterxml.jackson.annotation.JsonIgnore;
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
public class Utilisateur {

        @Id
        @GeneratedValue
        private Long id;

        private String nom;

        @Column(unique = true)
        private String email;

        private String motDePasse;

        @Enumerated(EnumType.STRING)
        private Role role;

<<<<<<< HEAD
=======
        @JsonIgnore
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
        private LocalDateTime dateCreation;

        private boolean actif;

        @Column(columnDefinition = "TEXT")
        private String followers;

        // Getters explicites pour Lombok
        public String getEmail() {
                return email;
        }

        public String getMotDePasse() {
                return motDePasse;
        }

        public Role getRole() {
                return role;
        }

        public Long getId() {
                return id;
        }
}