package t.esprit.arctic.jobmatch.dto;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CandidatureDTO {

    // Identifiants
    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date dateEnvoi;
    private String statut;
    private String lettreGeneree;
    private Long candidatId;
    private String candidatNom;


    private String entreprise;
    private String poste;

    // ==================== CHAMPS OBLIGATOIRES ====================
    @NotBlank(message = "Le nom complet est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s'-]+$", message = "Le nom ne doit contenir que des lettres, espaces, tirets ou apostrophes")
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

    // Constructeurs
    public CandidatureDTO() {}

    // ==================== GETTERS ET SETTERS  ====================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Date getDateEnvoi() { return dateEnvoi; }
    public void setDateEnvoi(Date dateEnvoi) { this.dateEnvoi = dateEnvoi; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getLettreGeneree() { return lettreGeneree; }
    public void setLettreGeneree(String lettreGeneree) { this.lettreGeneree = lettreGeneree; }

    public Long getCandidatId() { return candidatId; }
    public void setCandidatId(Long candidatId) { this.candidatId = candidatId; }

    public String getCandidatNom() { return candidatNom; }
    public void setCandidatNom(String candidatNom) { this.candidatNom = candidatNom; }

    public String getEntreprise() { return entreprise; }
    public void setEntreprise(String entreprise) { this.entreprise = entreprise; }

    public String getPoste() { return poste; }
    public void setPoste(String poste) { this.poste = poste; }

    public String getNomComplet() { return nomComplet; }
    public void setNomComplet(String nomComplet) { this.nomComplet = nomComplet; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getFormation() { return formation; }
    public void setFormation(String formation) { this.formation = formation; }

    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }

    public String getCompetences() { return competences; }
    public void setCompetences(String competences) { this.competences = competences; }

    public String getLettreMotivation() { return lettreMotivation; }
    public void setLettreMotivation(String lettreMotivation) { this.lettreMotivation = lettreMotivation; }

    public String getDateDisponibilite() { return dateDisponibilite; }
    public void setDateDisponibilite(String dateDisponibilite) { this.dateDisponibilite = dateDisponibilite; }

    public String getPreavis() { return preavis; }
    public void setPreavis(String preavis) { this.preavis = preavis; }

    public Boolean getAcceptContact() { return acceptContact; }
    public void setAcceptContact(Boolean acceptContact) { this.acceptContact = acceptContact; }

    public boolean isAcceptRGPD() { return acceptRGPD; }
    public void setAcceptRGPD(boolean acceptRGPD) { this.acceptRGPD = acceptRGPD; }

    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }

    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }

    public Long getOffreId() { return offreId; }
    public void setOffreId(Long offreId) { this.offreId = offreId; }

    public String getOffreTitre() { return offreTitre; }
    public void setOffreTitre(String offreTitre) { this.offreTitre = offreTitre; }

    // ==================== METHODE TO STRING POUR DEBUG ====================
    @Override
    public String toString() {
        return "CandidatureDTO{" +
                "id=" + id +
                ", nomComplet='" + nomComplet + '\'' +
                ", email='" + email + '\'' +
                ", telephone='" + telephone + '\'' +
                ", formation='" + formation + '\'' +
                ", experience='" + experience + '\'' +
                ", competences='" + competences + '\'' +
                ", lettreMotivation='" + lettreMotivation + '\'' +
                ", dateDisponibilite='" + dateDisponibilite + '\'' +
                ", preavis='" + preavis + '\'' +
                ", acceptContact=" + acceptContact +
                ", acceptRGPD=" + acceptRGPD +
                '}';
    }
}