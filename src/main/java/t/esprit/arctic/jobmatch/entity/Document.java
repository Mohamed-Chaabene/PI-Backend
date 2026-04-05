package t.esprit.arctic.jobmatch.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;

    @Enumerated(EnumType.STRING)
    private TypeDocument type;

    @Column(columnDefinition = "TEXT")
    private String contenu;

    private String template;

    @Column(name = "compatibleats")
    private Boolean compatibleATS;

    @Column(name = "ajouter_photo")
    private Boolean ajouterPhoto = false;

    @JsonIgnore
    @OneToOne(mappedBy = "document")
    private Candidature candidature;


    // Constructeurs
    public Document() {}

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public TypeDocument getType() { return type; }
    public void setType(TypeDocument type) { this.type = type; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public String getTemplate() { return template; }
    public void setTemplate(String template) { this.template = template; }

    public Boolean getCompatibleATS() { return compatibleATS; }
    public void setCompatibleATS(Boolean compatibleATS) { this.compatibleATS = compatibleATS; }

    public Boolean getAjouterPhoto() { return ajouterPhoto; }
    public void setAjouterPhoto(Boolean ajouterPhoto) { this.ajouterPhoto = ajouterPhoto; }

}