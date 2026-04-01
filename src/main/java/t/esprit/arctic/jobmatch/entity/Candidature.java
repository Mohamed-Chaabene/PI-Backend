package t.esprit.arctic.jobmatch.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "candidatures")
public class Candidature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dateEnvoi;

    private String statut;

    @Column(columnDefinition = "TEXT")
    private String lettreGeneree;

    // ==================== CHAMPS DU FORMULAIRE  ====================

    private String nomComplet;
    private String email;
    private String telephone;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String formation;

    @Column(columnDefinition = "TEXT")
    private String experience;

    @Column(columnDefinition = "TEXT")
    private String competences;

    @Column(columnDefinition = "TEXT")
    private String lettreMotivation;

    private String dateDisponibilite;
    private String preavis;
    private Boolean acceptContact;
    private Boolean acceptRGPD;

    // Relations
    @ManyToOne
    @JoinColumn(name = "candidat_id")
    private Candidat candidat;

    @OneToOne
    @JoinColumn(name = "document_id")
    private Document document;

    @ManyToOne
    @JoinColumn(name = "offre_id")
    private OffreEmploi offreEmploi;

    public Candidature() {}

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Date getDateEnvoi() { return dateEnvoi; }
    public void setDateEnvoi(Date dateEnvoi) { this.dateEnvoi = dateEnvoi; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getLettreGeneree() { return lettreGeneree; }
    public void setLettreGeneree(String lettreGeneree) { this.lettreGeneree = lettreGeneree; }

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

    public Boolean getAcceptRGPD() { return acceptRGPD; }
    public void setAcceptRGPD(Boolean acceptRGPD) { this.acceptRGPD = acceptRGPD; }

    public Candidat getCandidat() { return candidat; }
    public void setCandidat(Candidat candidat) { this.candidat = candidat; }

    public Document getDocument() { return document; }
    public void setDocument(Document document) { this.document = document; }

    public OffreEmploi getOffreEmploi() { return offreEmploi; }
    public void setOffreEmploi(OffreEmploi offreEmploi) { this.offreEmploi = offreEmploi; }
}