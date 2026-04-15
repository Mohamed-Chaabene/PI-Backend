package t.esprit.arctic.jobmatch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO pour la recherche avancée d'offres d'emploi
 * Contient les résultats avec un score de pertinence
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OffreSearchDTO {
    
    private Long offreId;
    private String titrOffre;
    private String description;
    private String entreprise;
    private String recruteurNom;
    private String recruteurEmail;
    private String location;
    private String typeContrat;
    private String salaire;
    private LocalDateTime datePublication;
    private Integer nombreCandidatures;
    private Integer nombreCandidaturesAcceptees;
    
    // Score de pertinence pour le classement des résultats (0-100)
    private Double relevanceScore;
    
    // Champs de recherche utilisés pour le surlignage du résultat
    private String matchedFields; // Ex: "titre, description, compétences"
    private String highlightedText; // Extrait du texte avec les mots-clés mis en avant
}
