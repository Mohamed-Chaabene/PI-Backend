package t.esprit.arctic.jobmatch.dto;

public class FormationSuggestion {
<<<<<<< HEAD
    private String playlistId;      // ✅ ID de la playlist (remplace youtubeId)
=======
    private String playlistId;
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0
    private String titre;
    private String thumbnail;
    private String chaineYoutube;
    private String writtenUrl;
    private String categorie;
    private String niveau;
<<<<<<< HEAD
    private int    nbVideos;        // ✅ Nombre de vidéos dans la playlist
=======
    private int    nbVideos;
>>>>>>> a46eeda7bd9a43913441aa8fcae79c5a5f2e16e0

    public FormationSuggestion(String playlistId, String titre, String thumbnail,
                               String chaineYoutube, String writtenUrl,
                               String categorie, String niveau, int nbVideos) {
        this.playlistId    = playlistId;
        this.titre         = titre;
        this.thumbnail     = thumbnail;
        this.chaineYoutube = chaineYoutube;
        this.writtenUrl    = writtenUrl;
        this.categorie     = categorie;
        this.niveau        = niveau;
        this.nbVideos      = nbVideos;
    }

    public String getPlaylistId()    { return playlistId; }
    public String getTitre()         { return titre; }
    public String getThumbnail()     { return thumbnail; }
    public String getChaineYoutube() { return chaineYoutube; }
    public String getWrittenUrl()    { return writtenUrl; }
    public String getCategorie()     { return categorie; }
    public String getNiveau()        { return niveau; }
    public int    getNbVideos()      { return nbVideos; }
}