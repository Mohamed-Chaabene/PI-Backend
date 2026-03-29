package t.esprit.arctic.jobmatch.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import t.esprit.arctic.jobmatch.entity.DomaineType;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/domaines")
@CrossOrigin(origins = "*")
public class DomainController {

    @GetMapping
    public ResponseEntity<List<DomainResponse>> getAllDomaines() {
        List<DomainResponse> domaines = new ArrayList<>();
        DomaineType[] values = DomaineType.values();
        for (int i = 0; i < values.length; i++) {
            domaines.add(new DomainResponse(i + 1, values[i].toString()));
        }
        System.out.println("✅ Retour des domaines: " + domaines.size() + " domaines");
        return ResponseEntity.ok(domaines);
    }

    // Inner class for response
    public static class DomainResponse {
        @JsonProperty("id")
        private Integer id;
        
        @JsonProperty("nom")
        private String nom;

        public DomainResponse() {}

        public DomainResponse(Integer id, String nom) {
            this.id = id;
            this.nom = nom;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getNom() {
            return nom;
        }

        public void setNom(String nom) {
            this.nom = nom;
        }

        @Override
        public String toString() {
            return "DomainResponse{" +
                    "id=" + id +
                    ", nom='" + nom + '\'' +
                    '}';
        }
    }
}
