package t.esprit.arctic.jobmatch.dto;

import java.util.Date;

public class CandidatureDTO {
    private Long id;
    private Date dateEnvoi;
    private String statut;
    private String lettreGeneree;
    private Long candidatId;
    private String candidatNom;
    private String entreprise;
    private String poste;

    // ===== NOUVEAUX CHAMPS POUR LES RELATIONS =====
    private Long documentId;
    private String documentType;
    private Long offreId;
    private String offreTitre;
    //private Long entretienId;

    public CandidatureDTO() {}

    // Getters et Setters
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

    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }

    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }

    public Long getOffreId() { return offreId; }
    public void setOffreId(Long offreId) { this.offreId = offreId; }

    public String getOffreTitre() { return offreTitre; }
    public void setOffreTitre(String offreTitre) { this.offreTitre = offreTitre; }

   /* public Long getEntretienId() { return entretienId; }
    public void setEntretienId(Long entretienId) { this.entretienId = entretienId; }*/
}