package t.esprit.arctic.jobmatch.repository;

import t.esprit.arctic.jobmatch.entity.Candidature;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface CandidatureRepository extends JpaRepository<Candidature, Long> {

    List<Candidature> findByCandidatId(Long candidatId);
    List<Candidature> findByStatut(String statut);
    List<Candidature> findAllByOrderByDateEnvoiDesc();
    List<Candidature> findByCandidatIdAndStatut(Long candidatId, String statut);


    //  Trouve les candidatures en attente depuis avant une certaine date

    List<Candidature> findByStatutAndDateEnvoiBefore(String statut, LocalDateTime date);


    //Trouve les candidatures envoyées avant une certaine date

    List<Candidature> findByDateEnvoiBefore(LocalDateTime date);

     // Trouve les candidatures par offre et statut
    List<Candidature> findByOffreEmploiIdAndStatut(Long offreId, String statut);

     // Compte les candidatures par statut après une certaine date
    long countByStatutAndDateEnvoiAfter(String statut, LocalDateTime date);

     // Compte les candidatures par statut entre deux dates
    long countByStatutAndDateEnvoiBetween(String statut, LocalDateTime debut, LocalDateTime fin);

     // Compte les candidatures par candidat, statut et avant une date
    long countByCandidatIdAndStatutAndDateEnvoiBefore(Long candidatId, String statut, LocalDateTime date);

     // Compte les candidatures entre deux dates pour un candidat
    long countByCandidatIdAndDateEnvoiBetween(Long candidatId, LocalDateTime debut, LocalDateTime fin);

     // Trouve les candidatures à relancer (en attente depuis plus de X jours)
    List<Candidature> findByCandidatIdAndStatutAndDateEnvoiBefore(Long candidatId, String statut, LocalDateTime date);

     //Trouve les candidatures sans réponse (en attente, avant date, nbRelances < seuil)
    List<Candidature> findByCandidatIdAndStatutAndDateEnvoiBeforeAndNbRelancesLessThan(Long candidatId, String statut, LocalDateTime date, Integer nbRelances);

     //Compte les candidatures par email pour détecter les doublons
   List<Candidature> findByCandidatIdAndEmail(Long candidatId, String email);

// Trouve les candidatures archivées
  List<Candidature> findByArchiveTrue();

    // Trouve les candidatures non archivées
   List<Candidature> findByArchiveFalseOrArchiveIsNull();


     // Trouve les candidatures qui nécessitent une attention particulière
     List<Candidature> findByCandidatIdAndNecessiteAttentionTrue(Long candidatId);

    //Archive les anciennes candidatures (update via @Modifying)

    List<Candidature> findByDateEnvoiBeforeAndArchiveFalse(LocalDateTime date);
}