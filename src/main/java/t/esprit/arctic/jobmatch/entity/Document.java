package t.esprit.arctic.jobmatch.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TypeDocument type;  // CV, LETTRE_DE_MOTIVATION, PORTFOLIO, AUTRE

    @Column(columnDefinition = "TEXT")
    private String contenu;

    private String template;

    private Boolean compatibleATS;

    // Relation inverse avec Candidature
    @OneToOne(mappedBy = "document")
    private Candidature candidature;

    public Document() {}

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TypeDocument getType() { return type; }
    public void setType(TypeDocument type) { this.type = type; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public String getTemplate() { return template; }
    public void setTemplate(String template) { this.template = template; }

    public Boolean getCompatibleATS() { return compatibleATS; }
    public void setCompatibleATS(Boolean compatibleATS) { this.compatibleATS = compatibleATS; }

    public Candidature getCandidature() { return candidature; }
    public void setCandidature(Candidature candidature) { this.candidature = candidature; }
}