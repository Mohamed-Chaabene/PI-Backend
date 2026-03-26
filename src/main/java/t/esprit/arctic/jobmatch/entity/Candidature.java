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

    private String entreprise;
    private String poste;

    // ===== RELATIONS =====

    // Relation avec Candidat (Many-to-One)
    @ManyToOne
    @JoinColumn(name = "candidat_id")
    private Candidat candidat;

    // Relation avec Document (One-to-One) - CV ou lettre de motivation
    @OneToOne
    @JoinColumn(name = "document_id")
    private Document document;

    // Relation avec OffreEmploi (Many-to-One) - optionnel
    @ManyToOne
    @JoinColumn(name = "offre_id")
    private OffreEmploi offreEmploi;

    /* Relation avec Entretien (One-to-One) - si une candidature peut avoir un entretien
    @OneToOne(mappedBy = "candidature")
    private Entretien entretien;
*/
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

    public String getEntreprise() { return entreprise; }
    public void setEntreprise(String entreprise) { this.entreprise = entreprise; }

    public String getPoste() { return poste; }
    public void setPoste(String poste) { this.poste = poste; }

    public Candidat getCandidat() { return candidat; }
    public void setCandidat(Candidat candidat) { this.candidat = candidat; }

    public Document getDocument() { return document; }
    public void setDocument(Document document) { this.document = document; }

    public OffreEmploi getOffreEmploi() { return offreEmploi; }
    public void setOffreEmploi(OffreEmploi offreEmploi) { this.offreEmploi = offreEmploi; }

   /* public Entretien getEntretien() { return entretien; }
    public void setEntretien(Entretien entretien) { this.entretien = entretien; }*/
}