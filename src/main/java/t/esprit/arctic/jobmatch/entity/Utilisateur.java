package t.esprit.arctic.jobmatch.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

        @JsonIgnore
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