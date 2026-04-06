package t.esprit.arctic.jobmatch.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CandidatureDTO {

    // Identifiants
    private Long id;
    private Long candidatId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date dateEnvoi;
    private String statut;
    private String lettreGeneree;
    private String candidatNom;
    private String entreprise;
    private String poste;

    // ==================== CHAMPS OBLIGATOIRES ====================
    @NotBlank(message = "Le nom complet est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    @Pattern(regexp = "^[\\p{L}\\s'-]+$", message = "Le nom ne doit contenir que des lettres, espaces, tirets ou apostrophes")
    private String nomComplet;

    @Email(message = "Format d'email invalide")
    private String email;

    @Pattern(regexp = "^[+]?[0-9\\s-]{8,20}$|^$",
            message = "Format de téléphone invalide. Exemples: +215 55 555 555, 55 555 555, 55555555")
    private String telephone;

    // ==================== CHAMPS TEXTES ====================
    @Size(max = 1000, message = "La description ne peut pas dépasser 1000 caractères")
    private String description;

    @Size(max = 500, message = "La formation ne peut pas dépasser 500 caractères")
    private String formation;

    @Size(max = 500, message = "L'expérience ne peut pas dépasser 500 caractères")
    private String experience;

    @Size(max = 500, message = "Les compétences ne peuvent pas dépasser 500 caractères")
    private String competences;

    @Size(max = 5000, message = "La lettre de motivation ne peut pas dépasser 5000 caractères")
    private String lettreMotivation;

    // ==================== DISPONIBILITÉ ====================
    @JsonFormat(pattern = "yyyy-MM-dd")
    private String dateDisponibilite;
    private String preavis;

    // ==================== CONSENTEMENT ====================
    private Boolean acceptContact;

    @AssertTrue(message = "Vous devez accepter les conditions RGPD")
    private boolean acceptRGPD;

    // ==================== RELATIONS ====================
    private Long documentId;
    private String documentType;
    private Long offreId;
    private String offreTitre;
}